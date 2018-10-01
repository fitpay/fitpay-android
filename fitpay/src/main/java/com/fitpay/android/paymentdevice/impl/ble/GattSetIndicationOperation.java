package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

/**
 * Subscribe to indication operation
 */
class GattSetIndicationOperation extends GattSetNotificationOperation {

    public GattSetIndicationOperation(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
        super(serviceUuid, characteristicUuid, descriptorUuid);
    }

    @Override
    protected byte[] getConfigurationValue() {
        return BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
    }
}
