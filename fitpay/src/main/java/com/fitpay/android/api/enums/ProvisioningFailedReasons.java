package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Reason a provisioning attempt failed
 */
public class ProvisioningFailedReasons {

    /**
     * the provision request was declined
     */
    public static final String PAYMENT_NETWORK_REJECTION = "PAYMENT_NETWORK_REJECTION";

    /**
     * card network didn't have all of the information it needed to provision. might be because device didn't sync.
     */
    public static final String NO_SECURITY_DOMAIN_AVAILABLE = "NO_SECURITY_DOMAIN_AVAILABLE";

    /**
     * the perso wasn't processed in time or resulted in an error
     */
    public static final String FAILED_CREDENTIAL_PERSONALIZATION = "FAILED_CREDENTIAL_PERSONALIZATION";

    /**
     * unexpected error, similar to a 400 http response
     */
    public static final String SD_CREATE_EXCEPTION = "SD_CREATE_EXCEPTION";

    /**
     * max number of credentials for the pan has been reached
     */
    public static final String PROVISIONING_LIMIT_REACHED = "PROVISIONING_LIMIT_REACHED";

    /**
     * user declined the issuer's terms and conditions document
     */
    public static final String USER_DECLINED_TERMS = "USER_DECLINED_TERMS";

    /**
     * unexpected error, similar to a 400 http response
     */
    public static final String FAILED_RETRIEVING_KEYS = "FAILED_RETRIEVING_KEYS";

    /**
     * card network didn't provide a valid decision on the request
     */
    public static final String INVALID_DIGITIZATION_DECISION = "INVALID_DIGITIZATION_DECISION";

    /**
     * provisioning request was approved with additional verification but no verification methods were provided
     */
    public static final String MISSING_ACTIVATION_METHODS = "MISSING_ACTIVATION_METHODS";

    /**
     * card network didn't respond to the provision request
     */
    public static final String TOKEN_STATUS_COMPLETION_TIMEOUT = "TOKEN_STATUS_COMPLETION_TIMEOUT";

    /**
     * card network responded with an error during the provisioning process
     */
    public static final String TOKEN_STATUS_ERROR = "TOKEN_STATUS_ERROR";

    /**
     * unknown
     */
    public static final String UNKNOWN = "UNKNOWN";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PAYMENT_NETWORK_REJECTION, NO_SECURITY_DOMAIN_AVAILABLE, FAILED_CREDENTIAL_PERSONALIZATION,
            SD_CREATE_EXCEPTION, PROVISIONING_LIMIT_REACHED, USER_DECLINED_TERMS, FAILED_RETRIEVING_KEYS,
            INVALID_DIGITIZATION_DECISION, MISSING_ACTIVATION_METHODS, TOKEN_STATUS_COMPLETION_TIMEOUT,
            TOKEN_STATUS_ERROR, UNKNOWN})
    public @interface Reason {
    }

}
