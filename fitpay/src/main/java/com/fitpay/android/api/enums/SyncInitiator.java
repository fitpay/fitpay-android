package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Sync initiators enum
 */
public final class SyncInitiator {

    public static final String PLATFORM = "PLATFORM";
    public static final String WEB_VIEW = "WEB_VIEW";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PLATFORM, WEB_VIEW})
    public @interface Initiator {
    }
}
