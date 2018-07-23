package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.fitpay.android.utils.FPLog;

import java.util.UUID;

/**
 * Write Gatt characteristic operation
 */
class GattCharacteristicWriteOperation extends GattOperation {

    private final byte[] mValue;

    public GattCharacteristicWriteOperation(UUID service, UUID characteristic, byte[] value) {
        mService = service;
        mCharacteristic = characteristic;
        mValue = value;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        FPLog.d("writing to " + mCharacteristic);
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        characteristic.setValue(mValue);
        gatt.writeCharacteristic(characteristic);
    }
}
