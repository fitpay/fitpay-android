package com.fitpay.android.paymentdevice.models;

import android.support.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.api.models.sync.SyncMetricsData;
import com.fitpay.android.paymentdevice.enums.NotificationType;

/**
 * Sync notification model (receiving it from push notification or webhook)
 * For type {@link NotificationType#SYNC}
 */
public final class SyncInfo extends NotificationPayload {
    private static final String ACK_SYNC = "ackSync";
    private static final String COMPLETE_SYNC = "completeSync";

    @SyncInitiator.Initiator
    private String initiator;

    private SyncInfo() {
        super();
        initiator = SyncInitiator.PLATFORM;
    }

    public String getInitiator() {
        return initiator;
    }

    /**
     * Set sync initiator {@link SyncInitiator}
     *
     * @param initiator initiator
     */
    public void setInitiator(@SyncInitiator.Initiator String initiator) {
        this.initiator = initiator;
    }

    /**
     * Send ack sync data
     *
     * @param syncId   sync id
     * @param callback result callback
     * @deprecated as of v1.1.0 - call sendAckSync without syncId
     */
    @Deprecated
    public void sendAckSync(@NonNull String syncId, @NonNull ApiCallback<Void> callback) {
        makeNoResponsePostCall(ACK_SYNC, syncId, callback);
    }

    /**
     * Send ack sync data
     *
     * @param callback result callback
     */
    public void sendAckSync(@NonNull ApiCallback<Void> callback) {
        makeNoResponsePostCall(ACK_SYNC, null, callback);
    }

    /**
     * Send metrics data
     *
     * @param data     metrics data
     * @param callback result callback
     */
    public void sendSyncMetrics(@NonNull SyncMetricsData data, @NonNull ApiCallback<Void> callback) {
        makeNoResponsePostCall(COMPLETE_SYNC, data, callback);
    }
}
