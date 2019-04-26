package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class APDUPackageCategory {

    public static final String PLATFORM = "PLATFORM";
    public static final String DSEMS = "DSEMS";
    public static final String LS = "LS";
    public static final String SEI_TSM = "SEI_TSM";
    public static final String SP_TSM = "SP_TSM";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PLATFORM, DSEMS, LS, SEI_TSM, SP_TSM })
    public @interface Category {
    }
}


