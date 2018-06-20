package com.fitpay.android.a2averification;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Verification errors. Used in {@link A2AVerificationFailed}
 */
public class A2AVerificationError {

    public static final String CANT_PROCESS = "can not process verification request";
    public static final String NO_ACTIVITY_TO_HANDLE = "no Activity found to handle Intent";
    public static final String NOT_SUPPORTED = "a2a auth is not supported";
    public static final String UNKNOWN = "unknown";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CANT_PROCESS, NO_ACTIVITY_TO_HANDLE, NOT_SUPPORTED, UNKNOWN})
    public @interface Reason {
    }
}
