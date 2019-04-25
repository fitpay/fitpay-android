package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothGatt;
import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * Base Gatt operation
 */
abstract class GattOperation {

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 10000;

    private GattOperation mParent;
    private OperationQueue mNestedQueue;

    protected UUID mService;
    protected UUID mCharacteristic;
    protected UUID mDescriptor;

    public abstract void execute(BluetoothGatt bluetoothGatt);

    public GattOperation() {
        mNestedQueue = new OperationQueue();
    }

    public int getTimeoutMs() {
        return DEFAULT_TIMEOUT_IN_MILLIS;
    }

    public void addNestedOperation(GattOperation operation) {
        operation.setParent(this);
        mNestedQueue.add(operation);
    }

    public OperationQueue getNestedQueue() {
        return mNestedQueue;
    }

    protected void clear() {
        if (mNestedQueue != null) {
            mNestedQueue.clear();
            mNestedQueue = null;
        }
    }

    public boolean hasNested() {
        return mNestedQueue != null && mNestedQueue.size() > 0;
    }

    public boolean canRunNextOperation() {
        return false;
    }

    private void setParent(GattOperation parent) {
        mParent = parent;
    }

    private GattOperation getParent() {
        return mParent;
    }

    public static GattOperation getRoot(@NonNull GattOperation operation) {
        GattOperation parent = operation.getParent();

        if (parent != null) {
            return getRoot(parent);
        }

        return operation;
    }
}
