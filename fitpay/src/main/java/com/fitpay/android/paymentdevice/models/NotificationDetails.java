package com.fitpay.android.paymentdevice.models;

import android.support.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.enums.PushNotification;

/**
 * Notification details
 */
public class NotificationDetails extends BaseModel {

    private static final String USER = "user";
    private static final String CREDIT_CARD = "creditCard";
    private static final String DEVICE = "device";

    private String deviceId;
    private String userId;
    private String clientId;
    private String creditCardId;
    @PushNotification.Type
    private String type;

    private NotificationDetails() {
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

    public String getCreditCardId() {
        return creditCardId;
    }

    @PushNotification.Type
    public String getType() {
        return type;
    }

    /**
     * Set notification type
     *
     * @param type type
     */
    public void setType(@PushNotification.Type String type) {
        this.type = type;
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
