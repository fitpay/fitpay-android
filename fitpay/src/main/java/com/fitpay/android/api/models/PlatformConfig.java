package com.fitpay.android.api.models;

/**
 * This model object represents the centralized platform configuration which FitPay can leverage
 * for dynamic SDK control of features/functionality.
 */
public class PlatformConfig {
    private boolean userEventStreamsEnabled = true;

    /**
     * Can be used disable the user event stream from being initialized in the sdk
     * server defaults to true
     *
     * @return userEventStreamsEnabled
     */
    public boolean isUserEventStreamsEnabled() {
        return userEventStreamsEnabled;
    }

    /**
     * You should not set event streams enabled as it is returned from the platform
     *
     * @deprecated as of v1.0.1
     *
     * @param userEventStreamsEnabled sets userEventStreamsEnabled
     */
    @Deprecated
    public void setUserEventStreamsEnabled(boolean userEventStreamsEnabled) {
        this.userEventStreamsEnabled = userEventStreamsEnabled;
    }

    @Override
    public String toString() {
        return "PlatformConfig{" + "userEventStreamsEnabled=" + userEventStreamsEnabled + "}";
    }
}
