package com.fitpay.android.paymentdevice.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.api.models.apdu.ApduCommandResult;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.card.TopOfWallet;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.CommitHandler;
import com.fitpay.android.paymentdevice.callbacks.ApduExecutionListener;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.CommitResult;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSkipped;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.paymentdevice.utils.ApduExecException;
import com.fitpay.android.utils.EventCallback;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Hex;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.TimestampUtils;

import java.io.SyncFailedException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static com.fitpay.android.paymentdevice.constants.ApduConstants.APDU_CONTINUE_COMMAND_DATA;
import static com.fitpay.android.paymentdevice.constants.ApduConstants.NORMAL_PROCESSING;
import static com.fitpay.android.utils.Constants.APDU_DATA;

/**
 * Base model for wearable payment device
 * <p>
 * This component is designed to handle one operation at a time.  It is not thread safe.
 */
public abstract class PaymentDeviceConnector implements IPaymentDeviceConnector {

    protected final String TAG;

    protected final String connectorId;

    private static final int MAX_REPEATS = 0;

    protected Context mContext;
    @Connection.State
    protected int state;
    private Map<String, CommitHandler> commitHandlers;
    private ErrorPair mErrorRepeats;

    private ApduExecutionListener apduExecutionListener;
    private ApduCommandListener apduCommandListener;

    private Commit currentCommit;

    private boolean apduExecutionInProgress = false;
    private long curApduPgkNumber;
    private ApduPackage curApduPackage;
    private ApduCommand curApduCommand;
    private ApduExecutionResult apduExecutionResult;

    private User user;
    private Device device;

    private Properties properties;

    public PaymentDeviceConnector() {
        this(UUID.randomUUID().toString());
    }

    public PaymentDeviceConnector(@NonNull final String id) {
        init();
        connectorId = id;
        TAG = "PaymentDeviceConnector-" + connectorId;
    }

    public PaymentDeviceConnector(@NonNull Context context) {
        this();
        mContext = context;
    }

    public PaymentDeviceConnector(@NonNull Context context, @NonNull final String id) {
        this(id);
        mContext = context;
    }

    private void init() {
        state = States.NEW;
        addDefaultCommitHandlers();
    }

    public String id() {
        return connectorId;
    }

    public String deviceId() {
        return device != null ? device.getDeviceIdentifier() : null;
    }

    @Override
    public void init(@NonNull Properties props) {
        this.properties = props;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setContext(@NonNull Context context) {
        this.mContext = context;
    }

    /**
     * All connectors should handle apdu processing.
     */
    protected void addDefaultCommitHandlers() {
        addCommitHandler(CommitTypes.APDU_PACKAGE, new ApduCommitHandler());
    }

    @Override
    public
    @Connection.State
    int getState() {
        return state;
    }

    @Override
    public void setState(@Connection.State int state) {
        FPLog.d(TAG, "connection state changed: " + state);
        this.state = state;
        postData(new Connection(state));
    }

    @Override
    public void close() {
        disconnect();

        if (null != apduExecutionListener) {
            NotificationManager.getInstance().removeListener(apduExecutionListener);
            apduExecutionListener = null;
        }
    }

    @Override
    public void reset() {
        // subclasses to implement as needed
    }

    @Override
    public void reconnect() {
        connect();
    }

    @Override
    public void addCommitHandler(String commitType, @NonNull final CommitHandler handler) {
        if (null == commitHandlers) {
            commitHandlers = new HashMap<>();
        }
        commitHandlers.put(commitType, handler);
    }

    @Override
    public void removeCommitHandler(String commitType) {
        if (null == commitHandlers) {
            return;
        }
        commitHandlers.remove(commitType);
    }

    @Override
    public void processCommit(@NonNull final Commit commit) {
        // Don't switch the current commit until we are ready to execute the new one. PGR-1240
        if (apduExecutionInProgress && currentCommit.getPayload() instanceof ApduPackage) {
            FPLog.w(TAG, "apduPackage processing is already in progress");
            return;
        }
        FPLog.d(TAG, "processing commit on Thread: " + Thread.currentThread() + ", " + Thread.currentThread().getName());
        currentCommit = commit;
        if (null == commitHandlers) {
            FPLog.w(TAG, "No action taken for commit.  No handlers defined for commit: " + commit);
            commitProcessed(CommitResult.SKIPPED, null);
            return;
        }
        CommitHandler handler = commitHandlers.get(commit.getCommitType());
        if (null != handler) {
            handler.processCommit(commit);
        } else {
            FPLog.w(TAG, "No action taken for commit.  No handler defined for commit: " + commit);
            // still need to signal that processing of the commit has completed
            commitProcessed(CommitResult.SKIPPED, null);
        }
    }

    @Override
    public void syncInit() {
        if (null == apduExecutionListener) {
            apduExecutionListener = new ApduPackageListener(connectorId);
            NotificationManager.getInstance().addListenerToCurrentThread(this.apduExecutionListener);
        }
        mErrorRepeats = null;
    }

    @Override
    public void syncComplete() {
        if (null != apduExecutionListener) {
            NotificationManager.getInstance().removeListener(this.apduExecutionListener);
            apduExecutionListener = null;
        }

        if (apduExecutionInProgress) {
            FPLog.w("syncComplete() called with apduExecutionInProgress still set to true, this is unexpected and implies a potential integration issue handling APDU commits");
            apduExecutionInProgress = false;
        }
    }

    @Override
    public void setUser(@NonNull User user) {
        this.user = user;
    }

    public final User getUser() {
        return user;
    }

    @Override
    public void setDevice(@NonNull Device device) {
        this.device = device;
    }

    @Override
    public final Device getDevice() {
        return device;
    }

    /**
     * If you want to process full apduPackage on your own,
     * you must override this function and follow these steps:
     * 1) create ApduExecutionResult
     * 2) apduExecutionResult.setExecutedTsEpoch(currentTimestamp)
     * 3) openGate (if required)
     * 4) process each command in the package. apduExecutionResult.addResponse(apduCommandResult);
     * 5) closeGate (if required)
     * 6) apduExecutionResult.setExecutedDurationTilNow();
     * 7) deviceConnector.sendApduExecutionResult(apduExecutionResult)
     * <p>
     * in case of error:
     * 1) apduExecutionResult.setState(apduError.getResponseState());
     * 2) apduExecutionResult.setErrorReason(apduError.getMessage());
     * 3) apduExecutionResult.setErrorCode(apduError.getResponseCode());
     * 4) apduExecutionResult.setExecutedDurationTilNow();
     * 5) deviceConnector.sendApduExecutionResult(apduExecutionResult)
     */
    @Override
    public void executeApduPackage(@NonNull final ApduPackage apduPackage) {
        if (apduExecutionInProgress) {
            FPLog.w(TAG, "apduPackage processing is already in progress");
            return;
        }

        apduExecutionInProgress = true;

        apduExecutionResult = new ApduExecutionResult(apduPackage.getPackageId());
        apduExecutionResult.setExecutedTsEpoch(System.currentTimeMillis());

        curApduPackage = apduPackage;
        curApduPgkNumber = System.currentTimeMillis();

        apduCommandListener = new ApduCommandListener(connectorId);
        NotificationManager.getInstance().addListener(apduCommandListener);

        onPreExecuteApdu();
    }

    @Override
    public void onPreExecuteApdu() {
        executeNextApduCommand();
    }

    @Override
    public void onPostExecuteApdu() {
        completeApduPackageExecution();
    }

    /**
     * Create sync request
     */
    @Override
    public void createSyncRequest(@Nullable SyncInfo syncInfo) {
        RxBus.getInstance().post(new SyncRequest.Builder()
                .setSyncId(syncInfo != null ? syncInfo.getSyncId() : null)
                .setUser(user)
                .setDevice(device)
                .setConnector(this)
                .setSyncInfo(syncInfo)
                .build());
    }

    /**
     * Post data for everyone who is listening for current {@link #id()}
     * @param data data
     */
    public void postData(@NonNull Object data) {
        RxBus.getInstance().post(connectorId, data);
    }

    /**
     * Execution has finished
     */
    protected void completeApduPackageExecution() {
        FPLog.i(APDU_DATA, "\\ApduPackageResult\\: " + apduExecutionResult);

        NotificationManager.getInstance().removeListener(apduCommandListener);

        curApduCommand = null;
        curApduPackage = null;

        apduExecutionResult.setExecutedDurationTilNow();
        sendApduExecutionResult(apduExecutionResult);

        apduExecutionResult = null;

        apduExecutionInProgress = false;
    }

    /**
     * get next apdu command
     */
    protected void executeNextApduCommand() {
        ApduCommand nextCommand = curApduPackage.getNextCommand(curApduCommand);

        if (nextCommand != null) {
            FPLog.i(APDU_DATA, "\\ProcessNextCommand\\: " + nextCommand.toString());

            curApduCommand = nextCommand;
            executeApduCommand(curApduPgkNumber, nextCommand);
        } else {
            onPostExecuteApdu();
        }
    }

    @Override
    public void commitProcessed(@CommitResult.Type int type, @Nullable final Throwable error) {
        switch (type) {
            case CommitResult.SUCCESS:
                CommitSuccess.Builder success = new CommitSuccess.Builder().commit(currentCommit);
                postData(success.build());
                break;

            case CommitResult.SKIPPED:
                CommitSkipped.Builder skipped = new CommitSkipped.Builder().commit(currentCommit);
                if (error != null) {
                    skipped.errorMessage(error.getMessage());
                }
                postData(skipped.build());
                break;

            case CommitResult.FAILED:
                CommitFailed.Builder failed = new CommitFailed.Builder().commit(currentCommit);
                if (error != null) {
                    failed.errorMessage(error.getMessage());
                }
                postData(failed.build());
                break;
        }
    }

    /**
     * @deprecated the SDK handling have TOW as commits is no longer supported, TOW data can still be pulled
     * from the CreditCard API resource when needed.
     * <p>
     * Get TOW data from the server
     */
    protected final void getTopOfWalletData(List<String> cardOrder) {
    }

    /**
     * @param towPackages
     * @deprecated See {@link IPaymentDeviceConnector} - providing a do-nothing implementation to help clean up OEM integrations
     */
    @Override
    public void executeTopOfWallet(List<TopOfWallet> towPackages) {
        FPLog.w(TAG, "deprecated executeTopOfWallet() still being called, please refactor to remove usage of PaymentDeviceConnect#executeTopOfWallet()");
    }

    /**
     * Send apdu command execution result
     *
     * @param apduCommandResult
     */
    public void sendApduCommandResult(@NonNull final ApduCommandResult apduCommandResult) {
        postData(apduCommandResult);
    }

    /**
     * Send apdu execution result to the server
     *
     * @param apduExecutionResult apdu execution result
     */
    public void sendApduExecutionResult(@NonNull final ApduExecutionResult apduExecutionResult) {

        EventCallback.Builder builder = new EventCallback.Builder()
                .setCommand(EventCallback.APDU_COMMANDS_SENT)
                .setStatus(EventCallback.STATUS_OK)
                .setTimestamp(apduExecutionResult.getExecutedTsEpoch());

        if (!apduExecutionResult.getState().equals(ResponseState.PROCESSED)) {
            builder.setReason(apduExecutionResult.getErrorReason());
            builder.setStatus(EventCallback.STATUS_FAILED);
        }

        builder.build().send(connectorId);

        if (apduExecutionResult.getState().equals(ResponseState.NOT_PROCESSED)) {
            commitProcessed(CommitResult.FAILED, new Exception("apdu command doesn't executed"));
            return;
        }

        if (null != currentCommit) {
            currentCommit.confirm(apduExecutionResult, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void serverResult) {
                    @ResponseState.ApduState String state = apduExecutionResult.getState();

                    switch (state) {
                        case ResponseState.PROCESSED:
                            commitProcessed(CommitResult.SUCCESS, null);
                            break;
                        case ResponseState.EXPIRED:
                        case ResponseState.FAILED:
                        case ResponseState.ERROR:
                            commitProcessed(CommitResult.FAILED, new Throwable(apduExecutionResult.getErrorReason()));
                            break;
                    }
                }

                @Override
                public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                    // TODO: determine what to do here, a failure reporting the confirmation to
                    // FitPay isn't really a commit failure, it's something that needs to be
                    // managed properly... i.e. the commit was applied, it was successful, it was
                    // the reporting to FitPay that wasn't, is that part of the commit?
                    FPLog.e(TAG, "Could not post apduExecutionResult. " + errorCode + ": " + errorMessage);
                    commitProcessed(CommitResult.FAILED, new SyncFailedException("Could not send adpu confirmation.  cause: " + errorMessage));
                }
            });
        } else {
            FPLog.e(TAG, "Unexpected state - current commit is null but should be populated");
        }
    }

    /**
     * Listen to the result of apdu package execution
     */
    private class ApduPackageListener extends ApduExecutionListener {

        public ApduPackageListener(@NonNull final String connectorId) {
            super(connectorId);
        }

        @Override
        public void onApduPackageResultReceived(ApduExecutionResult result) {
            sendApduExecutionResult(result);
        }

        @Override
        public void onApduPackageErrorReceived(ApduExecutionResult result) {

            final String id = result.getPackageId();

            switch (result.getState()) {
                case ResponseState.EXPIRED:
                    sendApduExecutionResult(result);
                    break;

                default:  //retry error and failure
                    if (mErrorRepeats == null || !mErrorRepeats.id.equals(id)) {
                        mErrorRepeats = new ErrorPair(id, 0);
                    }

                    if (mErrorRepeats.count++ >= MAX_REPEATS) {
                        sendApduExecutionResult(result);
                    } else {
                        // retry
                        processCommit(currentCommit);
                    }
                    break;
            }
        }
    }

    /**
     * Listen to the result of apdu command execution
     */
    private class ApduCommandListener extends Listener {
        final String normalResponseCode = Hex.bytesToHexString(NORMAL_PROCESSING);
        final StringBuilder longApduResponseStr = new StringBuilder();

        ApduCommandListener(@NonNull final String connectorId) {
            super(connectorId);
            mCommands.put(ApduCommandResult.class, data -> onApduCommandReceived((ApduCommandResult) data));
            mCommands.put(ApduExecException.class, data -> onApduExecErrorReceived((ApduExecException) data));
        }

        private void onApduCommandReceived(ApduCommandResult apduCommandResult) {
            FPLog.i(APDU_DATA, "\\CommandProcessed\\: " + apduCommandResult);

            if (apduCommandResult.isLongResponse()) {
                String responseStr = apduCommandResult.getResponseData().substring(0, apduCommandResult.getResponseData().length() - 4);
                longApduResponseStr.append(responseStr);

                String commandStr = Hex.bytesToHexString(APDU_CONTINUE_COMMAND_DATA) + apduCommandResult.getResponseCode().substring(2);

                ApduCommand apduContinueCommand = new ApduCommand.Builder()
                        .setCommand(commandStr)
                        .setCommandId(curApduCommand.getCommandId())
                        .setContinueOnFailure(curApduCommand.isContinueOnFailure())
                        .setGroupId(curApduCommand.getGroupId())
                        .setInjected(curApduCommand.isInjected())
                        .setSequence(curApduCommand.getSequence())
                        .setType(curApduCommand.getType())
                        .build();

                executeApduCommand(curApduPgkNumber, apduContinueCommand);

                return;
            }

            if (longApduResponseStr.length() > 0) {
                longApduResponseStr.append(apduCommandResult.getResponseData());

                apduCommandResult = new ApduCommandResult.Builder()
                        .setCommandId(apduCommandResult.getCommandId())
                        .setContinueOnFailure(apduCommandResult.canContinueOnFailure())
                        .setResponseData(longApduResponseStr.toString())
                        .setResponseCode(apduCommandResult.getResponseCode())
                        .build();

                longApduResponseStr.setLength(0);
            }

            apduExecutionResult.addResponse(apduCommandResult);

            String responseCode = apduCommandResult.getResponseCode();
            if (responseCode.equals(normalResponseCode) || curApduCommand.isContinueOnFailure()) {
                executeNextApduCommand();
            } else {
                ApduExecException execException = new ApduExecException(
                        ResponseState.FAILED,
                        "Device provided invalid response code: " + responseCode,
                        apduCommandResult.getCommandId(),
                        apduCommandResult.getResponseCode());
                onApduExecErrorReceived(execException);
            }
        }

        private void onApduExecErrorReceived(ApduExecException apduError) {
            apduExecutionResult.setState(apduError.getResponseState());
            apduExecutionResult.setErrorReason(apduError.getMessage());
            apduExecutionResult.setErrorCode(apduError.getResponseCode());
            onPostExecuteApdu();
        }
    }

    /**
     * Process apdu commit
     */
    private class ApduCommitHandler implements CommitHandler {

        @Override
        public void processCommit(Commit commit) {
            Object payload = commit.getPayload();
            if (payload instanceof ApduPackage) {
                ApduPackage pkg = (ApduPackage) payload;

                long currentTime = System.currentTimeMillis();
                try {
                    long validUntil = TimestampUtils.getDateForISO8601String(pkg.getValidUntil()).getTime();

                    FPLog.i(APDU_DATA, "\\ApduPackage\\: " + pkg);

                    if (validUntil > currentTime) {
                        executeApduPackage(pkg);
                    } else {
                        ApduExecutionResult result = new ApduExecutionResult(pkg.getPackageId());
                        result.setExecutedDuration(0);
                        result.setErrorReason(String.format(Locale.getDefault(),
                                "expired APDU package, validUntil: %s, validUtil Parsed: %d, currentTime: %d",
                                pkg.getValidUntil(),
                                validUntil,
                                currentTime));
                        result.setExecutedTsEpoch(currentTime);
                        result.setState(ResponseState.EXPIRED);

                        sendApduExecutionResult(result);
                    }
                } catch (ParseException e) {
                    FPLog.e(e);

                    ApduExecutionResult result = new ApduExecutionResult(pkg.getPackageId());
                    result.setExecutedDuration(0);
                    result.setExecutedTsEpoch(currentTime);
                    result.setErrorReason("failed to parse validUntil date from APDU package: " + pkg.getValidUntil());
                    result.setState(ResponseState.FAILED);

                    sendApduExecutionResult(result);
                }
            } else {
                FPLog.e(TAG, "ApduCommitHandler called for non-adpu commit. THIS IS AN APPLICATION DEFECT " + commit);
            }
        }
    }

    private class ErrorPair {
        String id;
        int count;

        ErrorPair(String id, int count) {
            this.id = id;
            this.count = count;
        }
    }

}
