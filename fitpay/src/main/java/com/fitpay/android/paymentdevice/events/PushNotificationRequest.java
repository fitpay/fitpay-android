package com.fitpay.android.paymentdevice.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.paymentdevice.enums.NotificationType;
import com.fitpay.android.paymentdevice.models.NotificationDetail;
import com.fitpay.android.paymentdevice.models.NotificationPayload;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.utils.Constants;

/**
 * Push notification event
 */
public class PushNotificationRequest {

    private final NotificationPayload notificationPayload;

    /**
     * @param data data payload (JSON string) passed from the push notification source
     */
    public PushNotificationRequest(@NonNull @NotificationType.Value String type, @NonNull String data) {
        switch (type) {
            case NotificationType.SYNC:
                this.notificationPayload = Constants.getGson().fromJson(data, SyncInfo.class);
                break;

            default:
                this.notificationPayload = Constants.getGson().fromJson(data, NotificationDetail.class);
                this.notificationPayload.setType(type); //type is missing in payload
                break;
        }
    }

    /**
     * Get sync info
     *
     * @return sync info or null
     */
    @Nullable
    public SyncInfo getSyncInfo() {
        return notificationPayload.asSyncInfo();
    }

    /**
     * Get notification detail
     *
     * @return notification detail or null
     */
    @Nullable
    public NotificationDetail getNotificationDetail() {
        return notificationPayload.asNotificationDetail();
    }
}