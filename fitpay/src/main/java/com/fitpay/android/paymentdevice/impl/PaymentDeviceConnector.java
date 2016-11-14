package com.fitpay.android.paymentdevice.impl;

import android.content.Context;
import android.util.Log;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.TopOfWallet;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.CommitHandler;
import com.fitpay.android.paymentdevice.callbacks.ApduExecutionListener;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSkipped;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.TimestampUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import rx.Observable;

/**
 * Base model for wearable payment device
 * <p>
 * This component is designed to handle one operation at a time.  It is not thread safe.
 */
public abstract class PaymentDeviceConnector implements IPaymentDeviceConnector {

    private final static String TAG = PaymentDeviceConnector.class.getSimpleName();

    private static final int MAX_REPEATS = 0;

    protected Context mContext;
    protected String mAddress;
    @Connection.State
    protected int state;
    protected Map<String, CommitHandler> commitHandlers;
    protected ApduExecutionListener apduExecutionListener;
    protected Commit currentCommit;
    private ErrorPair mErrorRepeats;

    protected User user;

    public PaymentDeviceConnector() {
        state = States.NEW;
        addDefaultCommitHandlers();
    }

    public PaymentDeviceConnector(Context context) {
        this();
        mContext = context;
    }

    public PaymentDeviceConnector(Context context, String address) {
        this(context);
        mAddress = address;
    }

    @Override
    public void init(Properties props) {
        // null implementation - override concrete class as needed
    }

    @Override
    public void setContext(Context context) {
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
        Log.d(TAG, "connection state changed: " + state);
        this.state = state;
        RxBus.getInstance().post(new Connection(state));
    }

    @Override
    public String getMacAddress() {
        return mAddress;
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
    public void addCommitHandler(String commitType, CommitHandler handler) {
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
    public void processCommit(Commit commit) {
        Log.d(TAG, "processing commit on Thread: " + Thread.currentThread() + ", " + Thread.currentThread().getName());
        currentCommit = commit;
        if (null == commitHandlers) {
            return;
        }
        CommitHandler handler = commitHandlers.get(commit.getCommitType());
        if (null != handler) {
            handler.processCommit(commit);
        } else {
            Log.d(TAG, "No action taken for commit.  No handler defined for commit: " + commit);
            // still need to signal that processing of the commit has completed
            RxBus.getInstance().post(new CommitSuccess(commit));
        }
    }

    @Override
    public void syncInit() {
        if (null == apduExecutionListener) {
            apduExecutionListener = getApduExecutionListener();
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
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    protected void getTopOfWalletData() {
        user.getAllCreditCards().flatMap(creditCardCollection -> {
            List<TopOfWallet> tow = new ArrayList<>();
            if (creditCardCollection.getResults() != null) {
                for (CreditCard card : creditCardCollection.getResults()) {
                    if (card.getTOW() != null) {
                        tow.add(card.getTOW());
                    }
                }
            }
            return Observable.just(tow);
        }).subscribe(
                topOfWallets -> {
                    if (topOfWallets.size() > 0) {
                        executeTopOfWallet(topOfWallets);
                        RxBus.getInstance().post(topOfWallets);
                    }
                },
                throwable -> Log.i(TAG, "Something goes wrong"));
    }

    private class ApduCommitHandler implements CommitHandler {

        @Override
        public void processCommit(Commit commit) {
            Object payload = commit.getPayload();
            if (payload instanceof ApduPackage) {
                ApduPackage pkg = (ApduPackage) payload;

                long validUntil = TimestampUtils.getDateForISO8601String(pkg.getValidUntil()).getTime();
                long currentTime = System.currentTimeMillis();

                if (validUntil > currentTime) {
                    PaymentDeviceConnector.this.executeApduPackage(pkg);
                } else {
                    ApduExecutionResult result = new ApduExecutionResult(pkg.getPackageId());
                    result.setExecutedDuration(0);
                    result.setExecutedTsEpoch(currentTime);
                    result.setState(ResponseState.EXPIRED);

                    RxBus.getInstance().post(result);
                }
            } else {
                Log.e(TAG, "ApduCommitHandler called for non-adpu commit.  THIS IS AN APPLICTION DEFECT " + commit);
            }
        }
    }

    private ApduExecutionListener getApduExecutionListener() {
        return new ApduExecutionListener() {

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

        };
    }


    /**
     * Send apdu execution result to the server
     *
     * @param result apdu execution result
     */
    private void sendApduExecutionResult(final ApduExecutionResult result) {
        if (null != currentCommit) {
            currentCommit.confirm(result, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result2) {
                    @ResponseState.ApduState String state = result.getState();

                    switch (state) {
                        case ResponseState.PROCESSED:
                            RxBus.getInstance().post(new CommitSuccess(currentCommit));
                            break;
                        case ResponseState.EXPIRED:
                            RxBus.getInstance().post(new CommitSkipped(currentCommit));
                            break;
                        case ResponseState.FAILED:
                            // TODO: determine if apdu package is retryable or not, right now the
                            // FitPay API doesn't support this attribute, but it will!
                            RxBus.getInstance().post(new CommitSkipped(currentCommit));
                            break;
                        case ResponseState.ERROR:
                            // TODO: determine what to do here, retry immediatly?
                            RxBus.getInstance().post(new CommitFailed.Builder()
                                    .commit(currentCommit)
                                    .errorCode(999)         //TODO create enum for errors
                                    .errorMessage("apdu command failure")
                                    .build());
                            break;
                    }
                }

                @Override
                public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                    // TODO: determine what to do here, a failure reporting the confirmation to
                    // FitPay isn't really a commit failure, it's something that needs to be
                    // managed properly... i.e. the commit was applied, it was successful, it was
                    // the reporting to FitPay that wasn't, is that part of the commit?
                    Log.e(TAG, "Could not post apduExecutionResult. " + errorCode + ": " + errorMessage);
                    RxBus.getInstance().post(new CommitFailed.Builder()
                            .commit(currentCommit)
                            .errorCode(errorCode)
                            .errorMessage("Could not send adpu confirmation.  cause: " + errorMessage)
                            .build());
                }
            });
        } else {
            Log.w(TAG, "Unexpected state - current commit is null but should be populated");
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
