package com.fitpay.android.paymentdevice.impl.mock;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.enums.DeviceTypes;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.api.models.apdu.ApduCommandResult;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.CreditCardCommit;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.CommitHandler;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.impl.PaymentDeviceConnector;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.google.gson.Gson;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

@SuppressLint({"SupportAnnotationUsage", "CheckResult"})
public class MockPaymentDeviceConnector extends PaymentDeviceConnector {

    private final String TAG = MockPaymentDeviceConnector.class.getSimpleName();

    public static final String CONFIG_DEFAULT_DELAY_TIME = "DEFAULT_DELAY_TIME";
    public static final String CONFIG_CONNECTING_RESPONSE_TIME = "CONNECTING_RESPONSE_TIME";
    public static final String CONFIG_CONNECTED_RESPONSE_TIME = "CONNECTED_RESPONSE_TIME";
    public static final String CONFIG_DISCONNECTING_RESPONSE_TIME = "DISCONNECTING_RESPONSE_TIME";
    public static final String CONFIG_DISCONNECTED_RESPONSE_TIME = "DISCONNECTED_RESPONSE_TIME";
    public static final String CONFIG_DEVICE_SERIAL_NUMBER = "DEVICE_SERIAL_NUMBER";
    private static final int DEFAULT_DELAY = 2000;

    private int delay = DEFAULT_DELAY;

    private Properties config;

    private final SyncCompleteListener syncCompleteListener;

    public MockPaymentDeviceConnector(Context context) {
        super(context);

        syncCompleteListener = new SyncCompleteListener(id());

        state = States.INITIALIZED;

        // configure commit handlers
        addCommitHandler(CommitTypes.CREDITCARD_CREATED, new MockWalletUpdateCommitHandler());
        addCommitHandler(CommitTypes.CREDITCARD_ACTIVATED, new MockWalletUpdateCommitHandler());
        addCommitHandler(CommitTypes.CREDITCARD_DEACTIVATED, new MockWalletUpdateCommitHandler());
        addCommitHandler(CommitTypes.CREDITCARD_REACTIVATED, new MockWalletUpdateCommitHandler());
        addCommitHandler(CommitTypes.RESET_DEFAULT_CREDITCARD, new MockWalletUpdateCommitHandler());
        addCommitHandler(CommitTypes.SET_DEFAULT_CREDITCARD, new MockWalletUpdateCommitHandler());
        addCommitHandler(CommitTypes.CREDITCARD_DELETED, new MockWalletDeleteCommitHandler());
    }

    @Override
    public void init(@NonNull Properties props) {
        if (null == config) {
            config = props;
        } else {
            config.putAll(props);
        }
        if (config.contains(CONFIG_DEFAULT_DELAY_TIME)) {
            delay = getIntValue(config.getProperty(CONFIG_DEFAULT_DELAY_TIME));
        }
    }

    @Override
    public void connect() {
        FPLog.d(TAG, "payment device connect requested.   current state: " + getState());

        NotificationManager.getInstance().addListenerToCurrentThread(syncCompleteListener);

        if (getState() == States.CONNECTED) {
            return;
        }

        setStateWithDelay(CONFIG_CONNECTING_RESPONSE_TIME, States.CONNECTING)
                .flatMap(o -> setStateWithDelay(CONFIG_CONNECTED_RESPONSE_TIME, States.CONNECTED))
                .toMaybe()
                .subscribe(
                        x -> {
                            FPLog.d(TAG, "connect successful");
                            readDeviceInfo();
                        },
                        throwable -> FPLog.e(TAG, "connect error:" + throwable.toString()));
    }

    @Override
    public void disconnect() {
        FPLog.d(TAG, "payment device disconnect requested.  current state: " + getState());

        if (null != syncCompleteListener) {
            NotificationManager.getInstance().removeListener(syncCompleteListener);
        }

        if (state != States.DISCONNECTING && state != States.DISCONNECTED) {
            setStateWithDelay(CONFIG_DISCONNECTING_RESPONSE_TIME, States.DISCONNECTING)
                    .flatMap(x -> setStateWithDelay(CONFIG_DISCONNECTED_RESPONSE_TIME, States.DISCONNECTED))
                    .subscribe(
                            x -> FPLog.d(TAG, "disconnect success"),
                            throwable -> FPLog.e(TAG, "disconnect error:" + throwable.toString()));
        }
    }

    @Override
    public void readDeviceInfo() {
        FPLog.d(TAG, "payment device readDeviceInfo requested");

        getDelayObservable()
                .map(o -> loadDefaultDevice())
                .subscribe(deviceInfo -> {
                    FPLog.d(TAG, "device info has been read.  device: " + deviceInfo);
                    postData(deviceInfo);
                }, throwable -> FPLog.e(TAG, "read device info error:" + throwable.toString()));
    }

    @Override
    public void executeApduPackage(@NonNull ApduPackage apduPackage) {
        ApduExecutionResult apduExecutionResult = new ApduExecutionResult(apduPackage.getPackageId());
        apduExecutionResult.setExecutedTsEpoch(System.currentTimeMillis());

        getDelayObservable().subscribe(getApduObserver(apduPackage, apduExecutionResult, 0));
    }

    @Override
    public void executeApduCommand(long apduPkgNumber, @NonNull ApduCommand apduCommand) {

    }

    private int getIntValue(String value) {
        int intValue = delay;
        try {
            intValue = Integer.parseInt(value);
        } catch (Exception ex) {
            FPLog.e(TAG, "could not convert string to int: " + value);
        }
        return intValue;
    }

    private int getTimeValueFromConfig(String key) {
        if (null == config || null == config.getProperty(key)) {
            return delay;
        }
        return getIntValue(config.getProperty(key));
    }

    private Device loadDefaultDevice() {
        String serialNumber;

        if (config != null) {
            serialNumber = config.getProperty(CONFIG_DEVICE_SERIAL_NUMBER, UUID.randomUUID().toString());
        } else {
            serialNumber = UUID.randomUUID().toString();
        }

        return new Device.Builder()
                .setDeviceType(DeviceTypes.WATCH)
                .setManufacturerName("Fitpay")
                .setDeviceName("PSPS")
                .setSerialNumber(serialNumber)
                .setModelNumber("FB404")
                .setHardwareRevision("1.0.0.0")
                .setFirmwareRevision("1030.6408.1309.0001")
                .setSoftwareRevision("2.0.242009.6")
                .setSystemId("0x123456FFFE9ABCDE")
                .setOSName("fitpayOS")
                .setLicenseKey("6b413f37-90a9-47ed-962d-80e6a3528036")
                .setBdAddress("00:00:00:00:00:00")
                .build();
    }

    private Single<Long> getDelayObservable() {
        return getDelayObservable(delay);
    }

    private Single<Long> getDelayObservable(int responseDelay) {
        return Single.timer(responseDelay, TimeUnit.MILLISECONDS)
                .compose(RxBus.applySchedulersExecutorThread(Single.class));
    }

    private Single<Long> setStateWithDelay(final String timeValue, final @Connection.State int targetState) {
        return getDelayObservable(getTimeValueFromConfig(timeValue))
                .doOnSuccess(o -> setState(targetState));
    }

    private <T> SingleObserver<T> getApduObserver(final ApduPackage apduPackage, final ApduExecutionResult apduExecutionResult, int apduCommandNumber) {
        return new SingleObserver<T>() {

            @Override
            public void onSubscribe(Disposable d) {
                FPLog.i(TAG, "apdu Observer subscribed");
            }

            @Override
            public void onSuccess(T aBoolean) {
                FPLog.d(TAG, "Get response for apduCommand: " + apduCommandNumber);
                ApduCommand apduCommand = apduPackage.getApduCommands().get(apduCommandNumber);
                ApduCommandResult apduCommandResult = getMockResultForApduCommand(apduCommand);
                apduExecutionResult.addResponse(apduCommandResult);
                FPLog.d(TAG, "apduExecutionResult: " + apduExecutionResult);

                if (apduCommandNumber + 1 < apduPackage.getApduCommands().size() && apduExecutionResult.getState().equals(ResponseState.PROCESSED)) {
                    getDelayObservable(100)
                            .subscribe(getApduObserver(apduPackage, apduExecutionResult, apduCommandNumber + 1));
                } else {
                    FPLog.d(TAG, "apduExecutionResult: " + apduExecutionResult);
                    int duration = (int) ((System.currentTimeMillis() - apduExecutionResult.getExecutedTsEpoch()) / 1000);
                    apduExecutionResult.setExecutedDuration(duration);
                    FPLog.d(TAG, "apdu processing is complete.  Result: " + new Gson().toJson(apduExecutionResult));
                    postData(apduExecutionResult);
                }
            }

            @Override
            public void onError(Throwable e) {
                FPLog.e(TAG, "apdu observer error: " + e.getMessage());
            }
        };
    }

    private ApduCommandResult getMockResultForApduCommand(ApduCommand apduCommand) {
        String responseData = "9000";

        switch (apduCommand.getType()) {
            case "GET_CPLC":
                responseData = "9F7F2A" + SecureElementDataProvider.generateRandomSecureElementId() + "9000";
                break;

            case "GET_CASD_P1":
                responseData = SecureElementDataProvider.generateCasd() + "9000";
                break;

            case "GET_CASD_P3":
                responseData = "6D00";
                break;

            case "SELECT_ALA":
                responseData = "6A82";
                break;

            default:
                break;
        }

        String responseCode = responseData.substring(responseData.length() - 4);

        return new ApduCommandResult.Builder()
                .setCommandId(apduCommand.getCommandId())
                .setContinueOnFailure(apduCommand.isContinueOnFailure())
                .setResponseData(responseData)
                .setResponseCode(responseCode)
                .build();
    }

    private class SyncCompleteListener extends Listener {

        private SyncCompleteListener(String connectorId) {
            super(connectorId);
            mCommands.put(Sync.class, data -> onSyncStateChanged((Sync) data));
        }

        void onSyncStateChanged(Sync syncEvent) {
            FPLog.d(TAG, "received on sync state changed event: " + syncEvent);

        }
    }

    private class MockWalletDeleteCommitHandler implements CommitHandler {

        @Override
        public void processCommit(Commit commit) {
            Object payload = commit.getPayload();

            // case where payload is null
            if (payload == null) {
                reportPayloadNull(commit);
                return;
            }

            if (!(payload instanceof CreditCardCommit)) {
                reportInvalidPayload(commit);
                return;
            }
            // process with a delay to mock device response time
            getDelayObservable(100).toMaybe()
                    .subscribe(
                            o -> {
                                FPLog.d(TAG, "processCommit " + commit.getCommitType());
                                CreditCardCommit card = (CreditCardCommit) commit.getPayload();
                                FPLog.d(TAG, "Mock wallet has been updated. Card removed: " + card.getCreditCardId());
                                postData(new CommitSuccess.Builder().commit(commit).build());
                            },
                            throwable -> {
                                FPLog.e(TAG, String.format("processCommit %s error:%s", commit.getCommitType(), throwable.toString()));
                                postData(new CommitFailed.Builder().commit(commit).build());
                            }
                    );
        }
    }

    private class MockWalletUpdateCommitHandler implements CommitHandler {

        @Override
        public void processCommit(Commit commit) {
            Object payload = commit.getPayload();

            // case where payload is null
            if (payload == null) {
                reportPayloadNull(commit);
                return;
            }

            if (!(payload instanceof CreditCardCommit)) {
                reportInvalidPayload(commit);
                return;
            }

            // process with a delay to mock device response time
            getDelayObservable(100).toMaybe()
                    .subscribe(o -> {
                                FPLog.d(TAG, "processCommit " + commit.getCommitType());
                                CreditCardCommit card = (CreditCardCommit) commit.getPayload();
                                FPLog.d(TAG, "Mock wallet has been updated. Card updated: " + card.getCreditCardId());
                                postData(new CommitSuccess.Builder().commit(commit).build());
                            },
                            throwable -> {
                                FPLog.e(TAG, String.format("processCommit %s error:%s", commit.getCommitType(), throwable.toString()));
                                postData(new CommitFailed.Builder().commit(commit).build());
                            });
        }
    }

    private void reportPayloadNull(Commit commit) {
        FPLog.e(TAG, "Mock Wallet received a commit with null payload data, Commit: " + commit);
        postData(new CommitFailed.Builder()
                .commit(commit)
                .errorCode(99)
                .errorMessage("Commit commit payload is null")
                .build());
    }

    private void reportInvalidPayload(Commit commit) {
        FPLog.e(TAG, "Mock Wallet received a commit to process that was not a credit card commit.  Commit: " + commit);
        postData(new CommitFailed.Builder()
                .commit(commit)
                .errorCode(999)
                .errorMessage("Commit does not contain a credit card")
                .build());
    }
}
