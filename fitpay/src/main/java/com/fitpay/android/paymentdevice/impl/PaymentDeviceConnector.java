package com.fitpay.android.paymentdevice.impl;

import android.content.Context;
import android.util.Log;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.paymentdevice.CommitHandler;
import com.fitpay.android.paymentdevice.callbacks.ApduExecutionListener;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.TimestampUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Base model for wearable payment device
 *
 * This component is designed to handle one operation at a time.  It is not thread safe.
 */
public abstract class PaymentDeviceConnector implements IPaymentDeviceConnector {

    private final static String TAG = PaymentDeviceConnector.class.getSimpleName();

    private static final int MAX_REPEATS = 3;

    protected Context mContext;
    protected String mAddress;
    protected @Connection.State int state;
    protected Map<String, CommitHandler> commitHandlers;
    protected ApduExecutionListener apduExecutionListener;
    protected Commit currentCommit;
    private ErrorPair mErrorRepeats;


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
    public @Connection.State int getState() {
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
            commitHandlers =  new HashMap<>();
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
            RxBus.getInstance().post(new CommitSuccess(commit.getCommitId()));
        }
    }


    @Override
    public void syncInit() {
        if (null == apduExecutionListener) {
            apduExecutionListener = getApduExecutionListener();
            NotificationManager.getInstance().removeListener(this.apduExecutionListener);
        }
    }

    @Override
    public void syncComplete() {
        if (null != apduExecutionListener) {
            NotificationManager.getInstance().removeListener(this.apduExecutionListener);
        }
    }

    private class ApduCommitHandler implements CommitHandler {

        @Override
        public void processCommit(Commit commit) {
            Object payload = commit.getPayload();
            if (payload instanceof ApduPackage) {
                mErrorRepeats = null;
                ApduPackage pkg = (ApduPackage) payload;

                long validUntil = TimestampUtils.getDateForISO8601String(pkg.getValidUntil()).getTime();
                long currentTime = System.currentTimeMillis();

                if(validUntil > currentTime){
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
                        if (mErrorRepeats == null || !mErrorRepeats.first.equals(id)) {
                            mErrorRepeats = new ErrorPair(id, 0);
                        }

                        if (++mErrorRepeats.second == MAX_REPEATS) {
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
     * @param result apdu execution result
     */
    private void sendApduExecutionResult(ApduExecutionResult result){
        if(null != currentCommit){

            currentCommit.confirm(result, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result2) {
                    if (ResponseState.PROCESSED == result.getState()) {
                        RxBus.getInstance().post(new CommitSuccess(currentCommit.getCommitId()));
                    } else {
                        RxBus.getInstance().post(new CommitFailed(currentCommit.getCommitId()));
                    }
                }

                @Override
                public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                    Log.e(TAG, "Could not post apduExecutionResult. " + errorCode + ": " + errorMessage);
                    RxBus.getInstance().post(new CommitFailed(currentCommit.getCommitId()));
                }
            });
        } else {
            Log.w(TAG, "Unexpected state - current commit is null but should be populated");
        }
    }


    private class ErrorPair{
        String first;
        int second;

        ErrorPair(String first, int second){
            this.first = first;
            this.second = second;
        }
    }

}
