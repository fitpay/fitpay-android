package com.fitpay.android.paymentdevice.events;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.utils.Constants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Push notification event
 */
public class PushNotificationRequest {

    public final static String SYNC = "SYNC";
    public final static String CREDITCARD_DELETED = "CREDITCARD_DELETED";
    public final static String CREDITCARD_ACTIVATED = "CREDITCARD_ACTIVATED";
    public final static String CREDITCARD_REACTIVATED = "CREDITCARD_REACTIVATED";
    public final static String CREDITCARD_SUSPENDED = "CREDITCARD_SUSPENDED";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SYNC, CREDITCARD_ACTIVATED, CREDITCARD_REACTIVATED, CREDITCARD_SUSPENDED, CREDITCARD_DELETED})
    public @interface Type {
    }

    private final SyncInfo syncInfo;

    /**
     * @param syncData data payload (JSON string) passed from the push notification source
     */
    public PushNotificationRequest(@NonNull String syncData) {
        this.syncInfo = Constants.getGson().fromJson(syncData, SyncInfo.class);
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