package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class OfflineSeActionTypes {
    public static final String TOP_OF_WALLET = "topOfWallet";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TOP_OF_WALLET})
    public @interface Type {
    }
}
