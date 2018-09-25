package com.fitpay.android.paymentdevice.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.sync.SyncMetricsData;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.events.PushNotificationRequest;

/**
 * Sync notification model (receiving it from push notification or webhook)
 * <p>
 * Equivalent to NotificationDetail in iOS
 */
public final class SyncInfo extends BaseModel {

    private static final String ACK_SYNC = "ackSync";
    private static final String COMPLETE_SYNC = "completeSync";
    private static final String CREDIT_CARD = "creditCard";
    private static final String DEVICE = "device";
    private static final String USER = "user";

    //common data
    private String deviceId;
    private String clientId;

    @PushNotificationRequest.Type
    private String type;

    //sync request data
    private String id;
    private String syncId;
    @SyncInitiator.Initiator
    private String initiator;

    //card request data
    private String userId;
    private String creditCardId;

    private SyncInfo() {
    }

    @Nullable
    public String getSyncId() {
        return syncId != null ? syncId : id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    public String getClientId() {
        return clientId;
    }

    /**
     * Set push notification type {@link com.fitpay.android.paymentdevice.events.PushNotificationRequest.Type}
     * @param type type
     */
    public void setType(@PushNotificationRequest.Type String type) {
        this.type = type;
    }

    @Nullable
    @PushNotificationRequest.Type
    public String getType() {
        return type;
    }

    @Nullable
    public String getCreditCardId() {
        return creditCardId;
    }

    @Nullable
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

    /**
     * Get user
     *
     * @param callback user
     */
    public void getUser(@NonNull ApiCallback<User> callback) {
        makeGetCall(USER, null, null, User.class, callback);
    }

    /**
     * Get credit card
     *
     * @param callback credit card
     */
    public void getCreditCard(@NonNull ApiCallback<CreditCard> callback) {
        makeGetCall(CREDIT_CARD, null, null, CreditCard.class, callback);
    }

    /**
     * Get device
     *
     * @param callback device
     */
    public void getDevice(@NonNull ApiCallback<Device> callback) {
        makeGetCall(DEVICE, null, null, Device.class, callback);
    }
}
