package com.fitpay.android.api.models;

/**
 * Created by ssteveli on 4/2/18.
 */

/**
 * This model object represents the centralized platform configuration which FitPay can leverage
 * for dynamic SDK control of features/functionality.
 */
public class PlatformConfig {
    private boolean userEventStreamsEnabled;

    public boolean isUserEventStreamsEnabled() {
        return userEventStreamsEnabled;
    }

    public void setUserEventStreamsEnabled(boolean userEventStreamsEnabled) {
        this.userEventStreamsEnabled = userEventStreamsEnabled;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PlatformConfig{");
        sb.append("userEventStreamsEnabled=").append(userEventStreamsEnabled);
        sb.append('}');
        return sb.toString();
    }
}
