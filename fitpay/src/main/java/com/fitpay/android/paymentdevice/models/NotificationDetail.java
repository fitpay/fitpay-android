package com.fitpay.android.paymentdevice.models;

import androidx.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.enums.NotificationType;

/**
 * Notification detail.
 * For all types except {@link NotificationType#SYNC}
 */
public class NotificationDetail extends NotificationPayload {

    private static final String CREDIT_CARD = "creditCard";
    private static final String DEVICE = "device";
    private static final String USER = "user";

    private NotificationDetail() {
        super();
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
