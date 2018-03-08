package com.fitpay.android.webview.enums;

import android.support.annotation.StringDef;

/**
 * Verification errors. Used in {@link com.fitpay.android.webview.events.a2a.A2AVerificationFailed}
 */
public class A2AVerificationError {

    public static final String CANT_PROCESS = "can not process verification request";
    public static final String NO_ACTIVITY_TO_HANDLE = "no Activity found to handle Intent";
    public static final String NOT_SUPPORTED = "a2a auth is not supported";
    public static final String UNKNOWN = "unknown";

    @StringDef({CANT_PROCESS, NO_ACTIVITY_TO_HANDLE, NOT_SUPPORTED, UNKNOWN})
    public @interface Reason {
    }
}
