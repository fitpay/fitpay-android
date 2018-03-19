package com.fitpay.android.paymentdevice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.events.NotificationSyncRequest;
import com.fitpay.android.paymentdevice.impl.PaymentDeviceConnector;
import com.fitpay.android.paymentdevice.impl.ble.BluetoothPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;

import static com.fitpay.android.utils.StringUtils.convertCommaSeparatedList;
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

    @Deprecated
    private User user;
    @Deprecated
    private Device device;

    @Deprecated
    private IPaymentDeviceConnector paymentDeviceConnector;

    @Deprecated
    private String paymentDeviceConnectorType;

    @Deprecated
    private String configParams;

    @Deprecated
    private Executor executor = Constants.getExecutor();

    private DeviceSyncManager syncManager;

    private final IBinder mBinder = new LocalBinder();

    private MessageListener mSyncListener = new MessageListener();

    public static void run(Context context) {
        context.startService(new Intent(context, DeviceService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, DeviceService.class));
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

        syncManager = new DeviceSyncManager(this);
        syncManager.onCreate();

        NotificationManager.getInstance().addListenerToCurrentThread(mSyncListener);
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

        if (syncManager != null) {
            syncManager.onDestroy();
            syncManager = null;
        }

        NotificationManager.getInstance().removeListener(mSyncListener);
    }

    /**
     * @deprecated Create {@link PaymentDeviceConnector} via constructor
     *  Set properties via {@link PaymentDeviceConnector#init(Properties)}
     *
     * Used for retrieving connector config information
     *
     * @return name of connector class
     */
    @Deprecated
    public String getPaymentServiceType() {
        if (null == paymentDeviceConnectorType) {
            if (null != paymentDeviceConnector) {
                paymentDeviceConnectorType = paymentDeviceConnector.getClass().getName();
            }
        }
        return paymentDeviceConnectorType;
    }

    /**
     * @deprecated Create {@link PaymentDeviceConnector} via constructor
     *  Set properties via {@link PaymentDeviceConnector#init(Properties)}
     *
     * @param intent
     */
    @Deprecated
    protected void configure(Intent intent) {
        if (null == intent) {
            FPLog.e(TAG, "DeviceService can not be configured with a null Intent.  Current connector: " + paymentDeviceConnector);
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
                        FPLog.w(TAG, "payment service type is not one of the known types.  type: " + paymentDeviceConnectorType);
                    }
                }
                if (null == paymentDeviceConnector) {
                    try {
                        Class paymentDeviceConnectorClass = forName(paymentDeviceConnectorType);
                        paymentDeviceConnector = (IPaymentDeviceConnector) paymentDeviceConnectorClass.newInstance();
                        paymentDeviceConnector.setContext(this);
                    } catch (Exception e) {
                        FPLog.e(TAG, e);
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
                FPLog.e(TAG, "unable to load properties. Reason: " + e.getMessage());
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
     * @deprecated Create {@link PaymentDeviceConnector} via constructor
     *  Set properties via {@link PaymentDeviceConnector#init(Properties)}
     *
     * Set paired payment device connector
     *
     * @param paymentDeviceConnector the payment device
     */
    @Deprecated
    public void setPaymentDeviceConnector(IPaymentDeviceConnector paymentDeviceConnector) {
        // check to see if device has changed, if so close the existing connection
        //TODO should test on device config - more general than MacAddress which is BLE specific (or at least pertinent to Mac devices)
        if (this.paymentDeviceConnector != null && this.paymentDeviceConnector.getState() == States.CONNECTED
                && this.paymentDeviceConnector != paymentDeviceConnector) {
            this.paymentDeviceConnector.disconnect();
            this.paymentDeviceConnector.close();
            this.paymentDeviceConnector = null;
        }
        this.paymentDeviceConnector = paymentDeviceConnector;
    }


    /**
     * @deprecated
     *
     * Get device connector
     *
     * @return interface of payment device
     */
    @Deprecated
    public IPaymentDeviceConnector getPaymentDeviceConnector() {
        return paymentDeviceConnector;
    }

    /**
     * @deprecated Use {@link PaymentDeviceConnector#connect()}
     */
    @Deprecated
    public void connectToDevice() {

        if (null == paymentDeviceConnector) {
            throw new IllegalStateException("Payment device connector has not been configured");
        }
        executor.execute(() -> {
            FPLog.d(TAG, "Starting execution of connectToDevice");
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
                    FPLog.e(TAG, "Can't connect to device.  Current device state does not support the connect operation.  State: " + paymentDeviceConnector.getState());
                    break;
            }
        });

    }

    /**
     * @deprecated Use {@link PaymentDeviceConnector#readDeviceInfo()}
     *
     * read info from your payment device
     */
    @Deprecated
    public void readDeviceInfo() {
        if (null == paymentDeviceConnector) {
            //TODO post an error
            FPLog.e(TAG, "payment device service is not defined.  Can not do operation: readDeviceInfo");
            return;
        }
        if (States.CONNECTED != paymentDeviceConnector.getState()) {
            //TODO post an error
            FPLog.e(TAG, "payment device service is not connected.  Can not do operation: readDeviceInfo");
            return;
        }
        executor.execute(() -> {
            FPLog.d(TAG, "Starting execution of readDeviceInfo");
            paymentDeviceConnector.readDeviceInfo();
        });
    }


    /**
     * @deprecated Use {@link PaymentDeviceConnector#disconnect()}
     *
     * Disconnect from payment device
     */
    @Deprecated
    public void disconnect() {
        executor.execute(() -> {
            FPLog.d(TAG, "Starting execution of disconnect");
            if (null != paymentDeviceConnector) {
                paymentDeviceConnector.disconnect();
                paymentDeviceConnector = null;
            }
        });
    }

    /**
     * @deprecated Please send {@link SyncRequest} via {@link RxBus} or {@link PaymentDeviceConnector#createSyncRequest(SyncInfo)}
     *
     * Sync data between FitPay server and payment device
     * <p>
     * This is an asynchronous operation.
     *
     * @param user   current user with hypermedia data
     * @param device device object with hypermedia data
     */
    @Deprecated
    public void syncData(@NonNull User user, @NonNull Device device) {
        this.user = user;
        this.device = device;
        syncData(user, device, paymentDeviceConnector, new NotificationSyncRequest());
    }

    /**
     * @deprecated Please send {@link SyncRequest} via {@link RxBus} or {@link PaymentDeviceConnector#createSyncRequest(SyncInfo)}
     *
     * Sync data between FitPay server and payment device
     *
     * This is an asynchronous operation.
     */
    @Deprecated
    public void syncData(@NonNull User user, @NonNull Device device, @NonNull IPaymentDeviceConnector connector) {
        syncData(user, device, connector, new NotificationSyncRequest());
    }

    /**
     * @deprecated Please send {@link SyncRequest} via {@link RxBus} or {@link PaymentDeviceConnector#createSyncRequest(SyncInfo)}
     *
     * Sync data between FitPay server and payment device
     *
     * @param user        current user with hypermedia data
     * @param device      device object with hypermedia data
     * @param syncRequest data provided in sync request
     * Sync data between FitPay server and payment device
     * <p>
     * This is an asynchronous operation.
     */
    @Deprecated
    public void syncData(@NonNull User user, @NonNull Device device, @NonNull NotificationSyncRequest syncRequest) {
        syncData(user, device, paymentDeviceConnector, syncRequest);
    }

    /**
     * @deprecated Please send {@link SyncRequest} via {@link RxBus} or {@link PaymentDeviceConnector#createSyncRequest(SyncInfo)}
     *
     * Sync data between FitPay server and payment device
     *
     * @param user        current user with hypermedia data
     * @param device      device object with hypermedia data
     * @param connector   payment device connector
     * @param syncRequest data provided in sync request
     * Sync data between FitPay server and payment device
     * <p>
     * This is an asynchronous operation.
     */
    @Deprecated
    public void syncData(@NonNull User user, @NonNull Device device, @NonNull IPaymentDeviceConnector connector, @NonNull NotificationSyncRequest syncRequest) {
        if (null == syncRequest.getSyncInfo()) {
            FPLog.d(TAG, "NotificationSyncRequest did not contain sync info.");
        }

        if (syncManager != null) {
            SyncRequest request = new SyncRequest.Builder()
                    .setUser(user)
                    .setDevice(device)
                    .setConnector(connector)
                    .setSyncInfo(syncRequest.getSyncInfo())
                    .build();
            syncManager.add(request);
        } else {
            Log.e(TAG, "syncManager is null");
        }
    }

    /**
     * @deprecated Use {@link PaymentDeviceConnector#getProperties()}
     *
     * @return config params
     */
    @Deprecated
    public String getConfigString() {
        return configParams;
    }

    /**
     * @deprecated Use {@link PaymentDeviceConnector#getProperties()}
     *
     * @return config params
     */
    @Deprecated
    public Properties getConfig() {
        Properties props = null;
        try {
            convertCommaSeparatedList(configParams);
        } catch (IOException e) {
            FPLog.e(TAG, "can not convert config to properties.  Reason: " + e.getMessage());
        }
        return props;
    }

    /**
     * Listen to Apdu and Sync callbacks
     */
    private class MessageListener extends Listener {

        private MessageListener() {
            super();
            mCommands.put(SyncRequest.class, data -> {
                if (syncManager != null) {
                    syncManager.add((SyncRequest) data);
                } else {
                    Log.e(TAG, "syncManager is null");
                }
            });
        }
    }
}
