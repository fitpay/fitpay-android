package com.fitpay.android.api.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Source types enum
 */
public class SourceTypes {

    public static final String DEVICE_API = "device";
    public static final String TSM_GATEWAY_API = "tsm";
    public static final String G_AND_D_INTEGRATION_API = "gi_de";
    public static final String MDES_GATEWAY_API = "mdes";
    public static final String DISCOVER_GATEWAY_API = "discover";
    public static final String VDEP_GATEWAY_API = "vdep";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            DEVICE_API,
            TSM_GATEWAY_API,
            G_AND_D_INTEGRATION_API,
            MDES_GATEWAY_API,
            DISCOVER_GATEWAY_API,
            VDEP_GATEWAY_API
    })

    public @interface Type {
    }
}
