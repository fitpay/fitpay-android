package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Verification method types for {@link com.fitpay.android.api.models.card.VerificationMethodModel}
 *
 * @see <a href="https://docs.fit-pay.com/creditCards/userVerification/#">User Verification Documentation</a>
 */
public class VerificationMethod {

    /**
     * Issuer sends one time passcode (OTP) to card holder via text message to a phone number on file with issuer
     */
    public static final String TEXT_TO_CARDHOLDER_NUMBER = "TEXT_TO_CARDHOLDER_NUMBER";

    /**
     * Issuer Sends OTP to card holder via email to an email on file with issuer
     */
    public static final String EMAIL_TO_CARDHOLDER_ADDRESS = "EMAIL_TO_CARDHOLDER_ADDRESS";

    /**
     * Card holder calls issuing bank's customer support to activate through an automated system
     */
    public static final String CARDHOLDER_TO_CALL_AUTOMATED_NUMBER = "CARDHOLDER_TO_CALL_AUTOMATED_NUMBER";

    /**
     * Card holder calls issuing bank's customer support to activate through a live agent
     */
    public static final String CARDHOLDER_TO_CALL_MANNED_NUMBER = "CARDHOLDER_TO_CALL_MANNED_NUMBER";

    /**
     * Card holder logs into issuer's website
     */
    public static final String CARDHOLDER_TO_VISIT_WEBSITE = "CARDHOLDER_TO_VISIT_WEBSITE";

    /**
     * Card holder calls issuing bank's customer support to get OTP
     */
    public static final String CARDHOLDER_TO_CALL_FOR_AUTOMATED_OTP_CODE = "CARDHOLDER_TO_CALL_FOR_AUTOMATED_OTP_CODE";

    /**
     * Issuer's customer service calls card holder to activate
     */
    public static final String AGENT_TO_CALL_CARDHOLDER_NUMBER = "AGENT_TO_CALL_CARDHOLDER_NUMBER";

    /**
     * Issuer's customer service calls card holder to activate
     */
    public static final String ISSUER_TO_CALL_CARDHOLDER_NUMBER = "ISSUER_TO_CALL_CARDHOLDER_NUMBER";

    /**
     * Card holder authenticates with issuer's mobile app
     */
    public static final String CARDHOLDER_TO_USE_MOBILE_APP = "CARDHOLDER_TO_USE_MOBILE_APP";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            TEXT_TO_CARDHOLDER_NUMBER,
            EMAIL_TO_CARDHOLDER_ADDRESS,
            CARDHOLDER_TO_CALL_AUTOMATED_NUMBER,
            CARDHOLDER_TO_CALL_MANNED_NUMBER,
            CARDHOLDER_TO_VISIT_WEBSITE,
            CARDHOLDER_TO_CALL_FOR_AUTOMATED_OTP_CODE,
            AGENT_TO_CALL_CARDHOLDER_NUMBER,
            ISSUER_TO_CALL_CARDHOLDER_NUMBER,
            CARDHOLDER_TO_USE_MOBILE_APP
    })
    public @interface Type {
    }
}
