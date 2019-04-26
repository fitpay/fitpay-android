package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Card initiators enum
 */
public final class CardInitiators {
    public static final String INITIATOR_CARDHOLDER = "CARDHOLDER";
    public static final String INITIATOR_ISSUER = "ISSUER";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({INITIATOR_CARDHOLDER, INITIATOR_ISSUER})
    public @interface Initiator {
    }
}
