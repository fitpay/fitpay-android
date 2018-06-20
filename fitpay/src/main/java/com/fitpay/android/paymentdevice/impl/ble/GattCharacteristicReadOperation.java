package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.fitpay.android.utils.FPLog;

import java.util.UUID;

/**
 * Read Gatt characteristic operation
 */
class GattCharacteristicReadOperation extends GattBaseReadOperation {

    public GattCharacteristicReadOperation(UUID service, UUID characteristic, OnReadCallback callback) {
        super(service, characteristic, callback);
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        FPLog.d("read characateristic to " + mCharacteristic);
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        gatt.readCharacteristic(characteristic);
    }

    @Override
    public void onRead(byte[] data) {
        if(mCallback != null) {
            mCallback.call(data);
        }
    }
}
