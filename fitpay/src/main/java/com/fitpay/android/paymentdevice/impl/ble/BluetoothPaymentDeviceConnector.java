package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.card.TopOfWallet;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.NFC;
import com.fitpay.android.paymentdevice.enums.SecureElement;
import com.fitpay.android.paymentdevice.impl.PaymentDeviceConnector;
import com.fitpay.android.paymentdevice.impl.ble.message.SecurityStateMessage;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;

import java.util.List;

/**
 * BLE implementation
 */
public final class BluetoothPaymentDeviceConnector extends PaymentDeviceConnector {

    private final static String TAG = BluetoothPaymentDeviceConnector.class.getSimpleName();
    public final static String EXTRA_BLUETOOTH_ADDRESS = "BLUETOOTH_ADDRESS";

    private BluetoothAdapter mBluetoothAdapter;
    private GattManager mGattManager;

    private String mAddress;

    public BluetoothPaymentDeviceConnector(Context context) {
        super(context);
        initBluetooth();
    }

    public BluetoothPaymentDeviceConnector(Context context, String deviceAddress) {
        super(context);
        mAddress = deviceAddress;
        initBluetooth();
    }

    protected void initBluetooth() {

        BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

        if (mBluetoothManager == null) {
            FPLog.e(TAG, "unable to initialize bluetooth manager");
            return;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            FPLog.e(TAG, "unable to obtain bluetooth adapter");
            return;
        }

        if (null != mAddress) {
            setState(States.INITIALIZED);
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    @Override
    public void connect() {
        FPLog.d(TAG, "initiate connect to device: " + mAddress);
        if (mBluetoothAdapter == null || StringUtils.isEmpty(mAddress)) {
            FPLog.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mAddress);
        if (device == null) {
            FPLog.w(TAG, "Device not found.  Unable to connect.");
            return;
        }

        mGattManager = new GattManager(this, mContext, device);
        mGattManager.queue(new GattSubscribeOperation());

    }

    @Override
    public void disconnect() {
        FPLog.d(TAG, "initiate disconnect from to device: " + mAddress);
        if (mGattManager != null) {
            mGattManager.disconnect();
        } else {
            FPLog.w(TAG, "GattManager is null");
        }
    }

    @Override
    public void reconnect() {
        if (mGattManager != null) {
            mGattManager.reconnect();
        } else {
            FPLog.w(TAG, "GattManager is null");
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    @Override
    public void close() {
        mGattManager.close();
        mGattManager = null;
    }

    @Override
    public void readDeviceInfo() {
        FPLog.d(TAG, "initiate readDeviceInfo request");
        GattOperation readDeviceInfoOperation = new GattDeviceCharacteristicsOperation(mAddress);
        mGattManager.queue(readDeviceInfoOperation);
    }

    public void readNFCState() {
        FPLog.d(TAG, "initiate readNFCState request");
        GattOperation getNFCOperation = new GattCharacteristicReadOperation(
                PaymentServiceConstants.SERVICE_UUID,
                PaymentServiceConstants.CHARACTERISTIC_SECURITY_STATE,
                data -> RxBus.getInstance().post(new SecurityStateMessage().withData(data)));
        mGattManager.queue(getNFCOperation);
    }

    public void setNFCState(@NFC.Action byte state) {
        FPLog.d(TAG, "initiate setNFCState request.  Target state: " + state);
        GattOperation setNFCOperation = new GattCharacteristicWriteOperation(
                PaymentServiceConstants.SERVICE_UUID,
                PaymentServiceConstants.CHARACTERISTIC_SECURITY_WRITE,
                new byte[]{state}
        );
        mGattManager.queue(setNFCOperation);
    }

    @Override
    public void executeApduPackage(ApduPackage apduPackage) {
        FPLog.d(TAG, "initiate executeApduPackage request");
        GattOperation sendApduOperation = new GattApduOperation(id(), apduPackage);
        mGattManager.queue(sendApduOperation);
    }

    @Override
    public void executeApduCommand(long apduPkgNumber, ApduCommand apduCommand) {
    }

    @Override
    public void executeTopOfWallet(List<TopOfWallet> towPackage) {
    }

    public void sendNotification(byte[] data) {
        FPLog.d(TAG, "initiate sendNotification request.  data: " + data);
        GattOperation setTransactionOperation = new GattCharacteristicWriteOperation(
                PaymentServiceConstants.SERVICE_UUID,
                PaymentServiceConstants.CHARACTERISTIC_NOTIFICATION,
                data
        );
        mGattManager.queue(setTransactionOperation);
    }

    public void setSecureElementState(@SecureElement.Action byte state) {
        FPLog.d(TAG, "initiate setSecureElementState request.  Target state: " + state);
        GattOperation resetOperation = new GattCharacteristicWriteOperation(
                PaymentServiceConstants.SERVICE_UUID,
                PaymentServiceConstants.CHARACTERISTIC_DEVICE_RESET,
                new byte[]{state}
        );
        mGattManager.queue(resetOperation);
    }
}
