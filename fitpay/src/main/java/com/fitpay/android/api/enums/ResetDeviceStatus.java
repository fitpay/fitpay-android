package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ResetDeviceStatus {
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String RESET_COMPLETE = "RESET_COMPLETE";
    public static final String DELETED = "DELETED";
    public static final String DELETE_FAILED = "DELETE_FAILED";
    public static final String RESET_FAILED = "RESET_FAILED";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({IN_PROGRESS, RESET_COMPLETE, DELETED, DELETE_FAILED, RESET_FAILED})
    public @interface Status {
    }
}
