package com.fitpay.android.a2averification;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Verification errors. Used in {@link A2AVerificationFailed}
 */
public class A2AVerificationError {

    public static final String CANT_PROCESS = "cantProcessVerification";
    public static final String DECLINED = "appToAppDeclined";
    public static final String FAILURE = "appToAppFailure";
    public static final String NOT_SUPPORTED = "appToAppNotSupported";
    public static final String UNKNOWN = "unknown";
    public static final String SILENT_UNKNOWN = "silentUnknown";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CANT_PROCESS, DECLINED, FAILURE, NOT_SUPPORTED, UNKNOWN, SILENT_UNKNOWN})
    public @interface Reason {
    }
}
