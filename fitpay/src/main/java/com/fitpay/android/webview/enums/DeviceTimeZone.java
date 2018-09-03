package com.fitpay.android.webview.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Device time zone set by
 */

public class DeviceTimeZone {
    public static final int SET_BY_NETWORK = 1;
    public static final int SET_BY_USER = 2;
    public static final int SET_BY_DEVICE_LOCATION = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SET_BY_NETWORK, SET_BY_USER, SET_BY_DEVICE_LOCATION})
    public @interface SetBy {
    }
}
