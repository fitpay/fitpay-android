package com.fitpay.android.api.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Verification method types for {@link com.fitpay.android.api.models.card.VerificationMethodModel}
 */
public class VerificationMethod {
    public static final String TEXT_TO_CARDHOLDER_NUMBER = "TEXT_TO_CARDHOLDER_NUMBER";
    public static final String EMAIL_TO_CARDHOLDER_ADDRESS = "EMAIL_TO_CARDHOLDER_ADDRESS";
    public static final String CARDHOLDER_TO_VISIT_WEBSITE = "CARDHOLDER_TO_VISIT_WEBSITE";
    public static final String CARDHOLDER_TO_CALL_AUTOMATED_NUMBER = "CARDHOLDER_TO_CALL_AUTOMATED_NUMBER";
    public static final String CARDHOLDER_TO_CALL_MANNED_NUMBER = "CARDHOLDER_TO_CALL_MANNED_NUMBER";
    public static final String CARDHOLDER_TO_CALL_FOR_AUTOMATED_OTP_CODE = "CARDHOLDER_TO_CALL_FOR_AUTOMATED_OTP_CODE";
    public static final String AGENT_TO_CALL_CARDHOLDER_NUMBER = "AGENT_TO_CALL_CARDHOLDER_NUMBER";
    public static final String CARDHOLDER_TO_USE_MOBILE_APP = "CARDHOLDER_TO_USE_MOBILE_APP";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            TEXT_TO_CARDHOLDER_NUMBER,
            EMAIL_TO_CARDHOLDER_ADDRESS,
            CARDHOLDER_TO_VISIT_WEBSITE,
            CARDHOLDER_TO_CALL_AUTOMATED_NUMBER,
            CARDHOLDER_TO_CALL_MANNED_NUMBER,
            CARDHOLDER_TO_CALL_FOR_AUTOMATED_OTP_CODE,
            AGENT_TO_CALL_CARDHOLDER_NUMBER,
            CARDHOLDER_TO_USE_MOBILE_APP
    })
    public @interface Type {
    }
}
