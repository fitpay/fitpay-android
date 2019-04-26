package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class VerificationState {

    /**
     * Supported by issuer and valid, display if app supports it
     */
    public static final String AVAILABLE_FOR_SELECTION = "AVAILABLE_FOR_SELECTION";

    /**
     * User previously selected method. Waiting for user to take additional action (enter otp, call issuer, etc.)
     */
    public static final String AWAITING_VERIFICATION = "AWAITING_VERIFICATION";

    /**
     * OTP code that was requested has expired.
     */
    public static final String EXPIRED = "EXPIRED";

    /**
     * This is the verification method the user successfully used.
     * This may be removed in the future as we don't return verificationMethods unless card is in pending user verification.
     */
    public static final String VERIFIED = "VERIFIED";

    /**
     * Verification was valid at some point in the card's lifecycle but no longer valid (ie: user submitted wrong code too many times).
     * Don't display.
     */
    public static final String ERROR = "ERROR";


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            AVAILABLE_FOR_SELECTION,
            AWAITING_VERIFICATION,
            EXPIRED,
            VERIFIED,
            ERROR
    })
    public @interface Type {
    }
}
