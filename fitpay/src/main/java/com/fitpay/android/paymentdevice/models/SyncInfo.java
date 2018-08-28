package com.fitpay.android.paymentdevice.models;

import android.support.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.sync.SyncMetricsData;

/**
 * Sync notification model (receiving it from push notification or webhook)
 *
 * Equivalent to NotificationDetail in iOS
 */
public final class SyncInfo extends BaseModel {

    private static final String ACK_SYNC = "ackSync";
    private static final String COMPLETE_SYNC = "completeSync";
    private static final String CREDIT_CARD = "creditCard";
    private static final String DEVICE = "device";

    private String id;
    private String syncId;
    private String deviceId;
    private String userId;
    private String clientId;
    private String type;
    private String creditCardId;

    @SyncInitiator.Initiator
    private String initiator;

    private SyncInfo() {
    }

    public String getSyncId() {
        return syncId != null ? syncId : id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getType() {
        return type;
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
     * @param syncId sync id
     * @param callback result callback
     *
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
     * @param data metrics data
     * @param callback result callback
     */
    public void sendSyncMetrics(@NonNull SyncMetricsData data, @NonNull ApiCallback<Void> callback) {
        makeNoResponsePostCall(COMPLETE_SYNC, data, callback);
    }

    public void getCreditCard(@NonNull ApiCallback<CreditCard> callback) {
        makeGetCall(CREDIT_CARD, null, null, CreditCard.class, callback);
    }

    public void getDevice(@NonNull ApiCallback<Device> callback) {
        makeGetCall(DEVICE, null, null, Device.class, callback);
    }
}
