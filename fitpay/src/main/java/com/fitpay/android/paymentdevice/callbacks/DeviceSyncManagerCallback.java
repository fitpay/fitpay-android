package com.fitpay.android.paymentdevice.callbacks;

import com.fitpay.android.paymentdevice.models.SyncRequest;

/**
 * This interface is more about testing and allowing unit tests to inject hooks into the process making this
 * class more testable.
 */
public interface DeviceSyncManagerCallback {
    void syncRequestAdded(SyncRequest request);

    void syncTaskStarting(SyncRequest request);

    void syncTaskStarted(SyncRequest request);

    void syncTaskCompleted(SyncRequest request);

    void syncRequestFailed(SyncRequest request);
}