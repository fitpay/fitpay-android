package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class OfflineSeActionTypes {
    public static final String TOP_OF_WALLET = "topOfWallet";
    public static final String ACTIVATE = "activate";
    public static final String DEACTIVATE = "deactivate";
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TOP_OF_WALLET, ACTIVATE, DEACTIVATE})
    public @interface Type {
    }
}
