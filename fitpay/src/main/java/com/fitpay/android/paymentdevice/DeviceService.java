package com.fitpay.android.paymentdevice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fitpay.android.R;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.callbacks.IListeners;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.AppMessage;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSkipped;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.impl.ble.BluetoothPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.utils.DevicePreferenceData;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.events.DeviceStatusMessage;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import me.alexrs.prefs.lib.Prefs;

import static java.lang.Class.forName;

/**
 * Connection and synchronization service
 * <p>
 * Allows for service binding or start
 */
public final class DeviceService extends Service {

    private final static String TAG = DeviceService.class.getSimpleName();

    public final static String EXTRA_PAYMENT_SERVICE_TYPE = "PAYMENT_SERVICE_TYPE";
    public final static String EXTRA_PAYMENT_SERVICE_CONFIG = "PAYMENT_SERVICE_CONFIG";
    public final static String PAYMENT_SERVICE_TYPE_MOCK = "PAYMENT_SERVICE_TYPE_MOCK";
    public final static String PAYMENT_SERVICE_TYPE_FITPAY_BLE = "PAYMENT_SERVICE_TYPE_FITPAY_BLE";

    public final static String SYNC_PROPERTY_DEVICE_ID = "syncDeviceId";

    private IPaymentDeviceConnector paymentDeviceConnector;
    private String paymentDeviceConnectorType;

    private String configParams;

    @Sync.State
    private Integer mSyncEventState;

    private List<Commit> mCommits;
    private Device device;
    private User user;

    private Executor executor = Constants.getExecutor();

    private CustomListener mSyncListener = new CustomListener();

    private final IBinder mBinder = new LocalBinder();

    public static void run(Context context) {
        context.startService(new Intent(context, DeviceService.class));
    }

    public class LocalBinder extends Binder {
        public DeviceService getService() {
            return DeviceService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        configure(intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        stopSelf();
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != paymentDeviceConnector) {
            paymentDeviceConnector.close();
            paymentDeviceConnector = null;
        }

        NotificationManager.getInstance().removeListener(mSyncListener);
    }

    /**
     * Used for retrieving connector config information
     *
     * @return name of connector class
     */
    public String getPaymentServiceType() {
        if (null == paymentDeviceConnectorType) {
            if (null != paymentDeviceConnector) {
                paymentDeviceConnectorType = paymentDeviceConnector.getClass().getName();
            }
        }
        return paymentDeviceConnectorType;
    }


    protected void configure(Intent intent) {
        if (null == intent) {
            Log.d(TAG, "DeviceService can not be configured with a null Intent.  Current connector: " + paymentDeviceConnector);
            return;
        }
        if (null != intent.getExtras() && intent.hasExtra(EXTRA_PAYMENT_SERVICE_TYPE)) {
            paymentDeviceConnectorType = intent.getExtras().getString(EXTRA_PAYMENT_SERVICE_TYPE);
            if (null != paymentDeviceConnectorType) {
                switch (paymentDeviceConnectorType) {
                    case PAYMENT_SERVICE_TYPE_MOCK: {
                        paymentDeviceConnector = new MockPaymentDeviceConnector();
                        break;
                    }
                    case PAYMENT_SERVICE_TYPE_FITPAY_BLE: {
                        String bluetoothAddress = intent.getExtras().getString(BluetoothPaymentDeviceConnector.EXTRA_BLUETOOTH_ADDRESS);
                        paymentDeviceConnector = new BluetoothPaymentDeviceConnector(this, bluetoothAddress);
                        break;
                    }
                    default: {
                        Log.d(TAG, "payment service type is not one of the known types.  type: " + paymentDeviceConnectorType);
                    }
                }
                if (null == paymentDeviceConnector) {

                    try {
                        Class paymentDeviceConnectorClass = forName(paymentDeviceConnectorType);
                        paymentDeviceConnector = (IPaymentDeviceConnector) paymentDeviceConnectorClass.newInstance();
                        paymentDeviceConnector.setContext(this);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (null != paymentDeviceConnector && intent.hasExtra(EXTRA_PAYMENT_SERVICE_CONFIG)) {
            configParams = intent.getExtras().getString(EXTRA_PAYMENT_SERVICE_CONFIG);
            Properties props = null;
            try {
                props = convertCommaSeparatedList(configParams);
            } catch (IOException e) {
                Log.e(TAG, "unable to load properties.   Reason: " + e.getMessage());
            }
            if (null != props) {
                paymentDeviceConnector.init(props);
            }
        }
        if (null != paymentDeviceConnector) {
            paymentDeviceConnector.reset();
        }
    }

    /**
     * Get paired payment device
     *
     * @param paymentDeviceConnector the payment device
     */
    public void setPaymentDeviceConnector(IPaymentDeviceConnector paymentDeviceConnector) {
        // check to see if device has changed, if so close the existing connection
        //TODO should test on device config - more general than MacAddress which is BLE specific (or at least pertinent to Mac devices)
        if (this.paymentDeviceConnector != null
                && ((this.paymentDeviceConnector.getMacAddress() == null && paymentDeviceConnector.getMacAddress() != null)
                || null != this.paymentDeviceConnector.getMacAddress() && !this.paymentDeviceConnector.getMacAddress().equals(paymentDeviceConnector.getMacAddress()))
                && this.paymentDeviceConnector.getState() == States.CONNECTED) {
            this.paymentDeviceConnector.disconnect();
            this.paymentDeviceConnector.close();
            this.paymentDeviceConnector = null;
        }
        this.paymentDeviceConnector = paymentDeviceConnector;
    }


    /**
     * Get device connector
     *
     * @return interface of payment device
     */
    public IPaymentDeviceConnector getPaymentDeviceConnector() {
        return paymentDeviceConnector;
    }

    public void connectToDevice() {

        if (null == paymentDeviceConnector) {
            throw new IllegalStateException("Payment device connector has not been configured");
        }
        executor.execute(() -> {
            Log.d(TAG, "Starting execution of connectToDevice");
            switch (paymentDeviceConnector.getState()) {
                case States.CONNECTED:
                    break;

                case States.INITIALIZED:
                    paymentDeviceConnector.connect();
                    break;

                case States.DISCONNECTED:
                    paymentDeviceConnector.reconnect();
                    break;

                default:
                    //TODO - why not let device decide if it can connect from this state?
                    Logger.e("Can't connect to device.  Current device state does not support the connect operation.  State: " + paymentDeviceConnector.getState());
                    break;
            }
        });

    }

    public void readDeviceInfo() {
        if (null == paymentDeviceConnector) {
            //TODO post an error
            Log.e(TAG, "payment device service is not defined.  Can not do operation: readDeviceInfo");
            return;
        }
        if (States.CONNECTED != paymentDeviceConnector.getState()) {
            //TODO post an error
            Log.e(TAG, "payment device service is not connected.  Can not do operation: readDeviceInfo");
            return;
        }
        executor.execute(() -> {
            Log.d(TAG, "Starting execution of readDeviceInfo");
            paymentDeviceConnector.readDeviceInfo();
        });
    }


    /**
     * Disconnect from payment device
     */
    public void disconnect() {
        executor.execute(() -> {
            Log.d(TAG, "Starting execution of disconnect");
            if (null != paymentDeviceConnector) {
                paymentDeviceConnector.disconnect();
                paymentDeviceConnector = null;
            }
            NotificationManager.getInstance().removeListener(mSyncListener);
        });
    }

    /**
     * Sync data between FitPay server and payment device
     * <p>
     * This is an asynchronous operation.
     *
     * @param user   current user with hypermedia data
     * @param device device object with hypermedia data
     */
    public void syncData(@NonNull User user, @NonNull Device device) {

        Log.d(TAG, "starting device sync.  device: " + device.getDeviceIdentifier());
        Log.d(TAG, "sync initiated from thread: " + Thread.currentThread() + ", " + Thread.currentThread().getName());

        this.user = user;
        this.device = device;

        if (paymentDeviceConnector == null) {
            //throw new RuntimeException("You should pair with a payment device at first");
            Logger.e("No payment device connector configured");
            throw new IllegalStateException("No payment device connector configured");
        }

        if (paymentDeviceConnector.getState() != States.CONNECTED) {
            //throw new RuntimeException("You should pair with a payment device at first");
            Logger.e("No payment device connection");
            throw new IllegalStateException("No payment device connection");
        }

        if (mSyncEventState != null && (mSyncEventState == States.STARTED || mSyncEventState == States.IN_PROGRESS)) {
            Logger.w("Sync already in progress. Try again later");
            throw new IllegalStateException("Another sync is currently active.  Please try again later");
        }

        paymentDeviceConnector.setUser(user);

        executor.execute(() -> {
            Log.d(TAG, "Starting execution of syncDevice");
            syncDevice();
        });
    }

    private void syncDevice() {
        RxBus.getInstance().post(new DeviceStatusMessage(getString(R.string.sync_started), DeviceStatusMessage.PROGRESS));

        if (paymentDeviceConnector.getState() == States.DISCONNECTED || paymentDeviceConnector.getState() == States.DISCONNECTING) {
            RxBus.getInstance().post(new DeviceStatusMessage(getString(R.string.disconnected), DeviceStatusMessage.PENDING));
            RxBus.getInstance().post(new Sync(States.FAILED));
            return;
        }

        Log.d(TAG, "sync running on thread: " + Thread.currentThread() + ", " + Thread.currentThread().getName());

        String devId = device.getDeviceIdentifier();

        // provide sync specific data to device connector
        Properties syncProperties = new Properties();
        syncProperties.put(SYNC_PROPERTY_DEVICE_ID, devId);
        paymentDeviceConnector.init(syncProperties);
        paymentDeviceConnector.syncInit();

        NotificationManager.getInstance().addListenerToCurrentThread(mSyncListener);

        RxBus.getInstance().post(new Sync(States.STARTED));

        /*
         * In case of another account force update our wallet
         */
        final AtomicBoolean forceWalletUpdate = new AtomicBoolean(false);
        final String prevDeviceId = Prefs.with(this).getString(SYNC_PROPERTY_DEVICE_ID, null);
        if (StringUtils.isEmpty(prevDeviceId) || !prevDeviceId.equals(devId)) {
            Prefs.with(this).save(SYNC_PROPERTY_DEVICE_ID, devId);
            forceWalletUpdate.set(true);
        }

        DevicePreferenceData deviceData = DevicePreferenceData.load(this, devId);

        device.getAllCommits(deviceData.getLastCommitId())
                .compose(RxBus.applySchedulersExecutorThread())
                .subscribe(
                        commitsCollection -> {
                            mCommits = commitsCollection.getResults();

                            if (mCommits != null && mCommits.size() > 0) {
                                Log.d(TAG, "processing commits.  count: " + mCommits.size());
                                RxBus.getInstance().post(new Sync(States.IN_PROGRESS, mCommits.size()));
                                processNextCommit();
                            } else {
                                RxBus.getInstance().post(new Sync(forceWalletUpdate.get() ? States.COMPLETED : States.COMPLETED_NO_UPDATES));
                            }
                        },
                        throwable -> {
                            if (throwable instanceof DeviceOperationException) {
                                DeviceOperationException doe = (DeviceOperationException) throwable;
                                Log.e(TAG, "get commits failed.  reasonCode: " + doe.getErrorCode() + ",  " + doe.getMessage());
                            } else {
                                Log.e(TAG, "get commits failed. " + throwable.getMessage());
                            }

                            RxBus.getInstance().post(new Sync(States.FAILED));
                        });
    }

    /**
     * process next commit
     */
    private void processNextCommit() {
        if (mCommits != null && mCommits.size() > 0) {
            RxBus.getInstance().post(new Sync(States.INC_PROGRESS, mCommits.size()));
            Commit commit = mCommits.get(0);
            Log.d(TAG, "process commit: " + commit + " on thread: " + Thread.currentThread());
            paymentDeviceConnector.processCommit(commit);
            // expose the commit out to others who may want to take action
            RxBus.getInstance().post(commit);
        } else {
            RxBus.getInstance().post(new Sync(States.COMPLETED));
        }
    }

    /**
     * Apdu and Sync callbacks
     */
    private class CustomListener extends Listener implements IListeners.SyncListener {

        private CustomListener() {
            super();
            mCommands.put(Sync.class, data -> onSyncStateChanged((Sync) data));
            mCommands.put(CommitSuccess.class, data -> onCommitSuccess((CommitSuccess) data));
            mCommands.put(CommitFailed.class, data -> onCommitFailed((CommitFailed) data));
            mCommands.put(CommitSkipped.class, data -> onCommitSkipped((CommitSkipped) data));
            mCommands.put(AppMessage.class, data -> {
                try {
                    syncData(user, device);
                } catch (Exception e) {
                    //don't remove try/catch. syncData can throw an exception when it busy.
                }
            });
        }

        @Override
        public void onSyncStateChanged(Sync syncEvent) {
            Log.d(TAG, "received on sync state changed event: " + syncEvent);
            mSyncEventState = syncEvent.getState();

            if (mSyncEventState == States.COMPLETED || mSyncEventState == States.FAILED || mSyncEventState == States.COMPLETED_NO_UPDATES) {
                //At this time, the sync listener is no longer needed and can be unregisered.
                //However, a listener should not unregister itself so we will leave it
                //NotificationManager.getInstance().removeListener(mSyncListener);
                Log.d(TAG, "sync has ended");
                paymentDeviceConnector.syncComplete();
            }

            if (mSyncEventState == States.COMMIT_COMPLETED) {
                processNextCommit();
            }
        }

        @Override
        public void onCommitFailed(CommitFailed commitFailed) {
            Log.d(TAG, "received commit failed event: " + commitFailed.getCommitId());
            RxBus.getInstance().post(new Sync(States.FAILED));
        }

        @Override
        public void onCommitSuccess(CommitSuccess commitSuccess) {
            Log.d(TAG, "received commit success event.  moving last commit pointer to: " + commitSuccess.getCommitId());
            DevicePreferenceData deviceData = DevicePreferenceData.load(DeviceService.this, DeviceService.this.device.getDeviceIdentifier());
            deviceData.setLastCommitId(commitSuccess.getCommitId());
            DevicePreferenceData.store(DeviceService.this, deviceData);
            mCommits.remove(0);
            processNextCommit();
        }

        @Override
        public void onCommitSkipped(CommitSkipped commitSkipped) {
            Log.d(TAG, "received commit skipped event.  moving last commit pointer to: " + commitSkipped.getCommitId());
            DevicePreferenceData deviceData = DevicePreferenceData.load(DeviceService.this, DeviceService.this.device.getDeviceIdentifier());
            deviceData.setLastCommitId(commitSkipped.getCommitId());
            DevicePreferenceData.store(DeviceService.this, deviceData);
            mCommits.remove(0);
            processNextCommit();
        }
    }

    private Properties convertCommaSeparatedList(String input) throws IOException {
        if (null == input) {
            return null;
        }
        String propertiesFormat = input.replaceAll(",", "\n");
        Properties properties = new Properties();
        properties.load(new StringReader(propertiesFormat));
        return properties;
    }

    public String getConfigString() {
        return configParams;
    }

    public Properties getConfig() {
        Properties props = null;
        try {
            convertCommaSeparatedList(configParams);
        } catch (IOException e) {
            Log.e(TAG, "can not convert config to properties.  Reason: " + e.getMessage());
        }
        return props;
    }
}
