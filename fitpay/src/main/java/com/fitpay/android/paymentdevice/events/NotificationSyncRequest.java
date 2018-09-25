package com.fitpay.android.paymentdevice.events;

import android.support.annotation.NonNull;

import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.utils.Constants;

/**
 * @deprecated Use {@link PushNotificationRequest}
 * Request to sync from push notification.
 */
@Deprecated
public class NotificationSyncRequest {
    private final SyncInfo syncInfo;

    /**
     *
     * Initiates a sync process without sync data or initator
     */
    public NotificationSyncRequest() {
        this.syncInfo = null;
    }

    /**
     *
     * @param syncData  sync data payload (JSON string) passed from the notfication source
     * Initiates a sync process and defaults to PLATFORM initator
     */
    public NotificationSyncRequest(@NonNull String syncData) {
        this.syncInfo = Constants.getGson().fromJson(syncData, SyncInfo.class);
        if (null != this.syncInfo) {
            this.syncInfo.setInitiator(SyncInitiator.PLATFORM);
        }
    }

    /**
     *
     * @param syncData  sync data payload (JSON string) passed from the notfication source
     * @param initiator what initiated the sync
     * Initiates a sync process
     */
    public NotificationSyncRequest(@NonNull String syncData, @NonNull String initiator) {
        this.syncInfo = Constants.getGson().fromJson(syncData, SyncInfo.class);
        if (null != this.syncInfo) {
            this.syncInfo.setInitiator(initiator);
        }
    }

    /**
     * Get sync info
     *
     * @return sync info
     */
    public SyncInfo getSyncInfo() {
        return syncInfo;
    }
}
