package com.fitpay.android.paymentdevice.models;

import androidx.annotation.Nullable;

import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.paymentdevice.enums.NotificationType;

/**
 * Notification payload model (receiving it from push notification or webhook)
 */
public class NotificationPayload extends BaseModel {

    //common data
    private String deviceId;
    private String clientId;

    @NotificationType.Value
    private String type;

    //sync request data
    private String id;
    private String syncId;

    //card request data
    private String userId;
    private String creditCardId;

    protected NotificationPayload() {
    }

    @Nullable
    public SyncInfo asSyncInfo() {
        return this instanceof SyncInfo ? (SyncInfo) this : null;
    }

    @Nullable
    public NotificationDetail asNotificationDetail() {
        return this instanceof NotificationDetail ? (NotificationDetail) this : null;
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
     * Set push notification type {@link NotificationType.Value}
     *
     * @param type type
     */
    public void setType(@NotificationType.Value String type) {
        this.type = type;
    }

    @Nullable
    @NotificationType.Value
    public String getType() {
        return type;
    }

    @Nullable
    public String getCreditCardId() {
        return creditCardId;
    }

}
