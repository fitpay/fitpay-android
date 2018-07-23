package com.fitpay.android.api.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class APDUPackageOperation {

    public static final String BOARDING = "BOARDING";
    public static final String SD_CREATE = "SD_CREATE";
    public static final String SD_DELETE = "SD_DELETE";
    public static final String APPLET_INSTALL = "APPLET_INSTALL";
    public static final String APPLET_DELETE = "APPLET_DELETE";
    public static final String SD_PERSO = "SD_PERSO";
    public static final String PASSTHROUGH = "PASSTHROUGH";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({BOARDING, SD_CREATE, SD_DELETE, APPLET_INSTALL, APPLET_DELETE, SD_PERSO, PASSTHROUGH })
    public @interface Operation {
    }
}