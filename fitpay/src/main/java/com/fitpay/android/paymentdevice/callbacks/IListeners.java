package com.fitpay.android.paymentdevice.callbacks;

import com.fitpay.android.api.models.UserStreamEvent;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSkipped;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.events.PaymentDeviceOperationFailed;

/**
 * Collection of payment device callbacks
 */
public final class IListeners {

    public interface UserEventStreamListener {
        void onUserEvent(final UserStreamEvent event);
    }

    public interface ApduListener {
        void onApduPackageResultReceived(final ApduExecutionResult result);
        void onApduPackageErrorReceived(final ApduExecutionResult result);
    }

    public interface ConnectionListener {
        void onDeviceStateChanged(final @Connection.State int state);
    }

    public interface SyncListener {
        void onSyncStateChanged(final Sync syncEvent);
        void onCommitSuccess(final CommitSuccess commitSuccess);
        void onCommitFailed(final CommitFailed commitFailed);
        void onCommitSkipped(final CommitSkipped commitSkipped);
    }

    public interface PaymentDeviceListener extends ConnectionListener {
        void onDeviceInfoReceived(final Device device);
        void onDeviceOperationFailed(final PaymentDeviceOperationFailed failure);
        void onNFCStateReceived(final boolean isEnabled, final byte errorCode);
        void onNotificationReceived(final byte[] data);
        void onApplicationControlReceived(final byte[] data);
    }

    public interface CommitListener {
        void onCommitFailed(CommitFailed commitFailed);
        void onCommitSuccess(CommitSuccess commitSuccess);
    }
}
