package com.fitpay.android.webview.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Rtm request &#064; response types
 */
public class RtmType {

    public static final int RTM_VERSION = 5;

    public static final String VERSION = "version";
    public static final String SYNC = "sync";
    public static final String USER_DATA = "userData";
    public static final String NO_HISTORY = "noHistory";
    public static final String SCAN_REQUEST = "scanRequest";
    public static final String CARD_SCANNED = "cardScanned";
    public static final String SDK_VERSION = "sdkVersion";
    public static final String SDK_VERSION_REQUEST = "sdkVersionRequest";
    public static final String ID_VERIFICATION = "idVerification";
    public static final String ID_VERIFICATION_REQUEST = "idVerificationRequest";
    public static final String SUPPORTS_ISSUER_APP_VERIFICATION = "supportsIssuerAppVerification";
    public static final String APP_TO_APP_VERIFICATION = "appToAppVerification";
    public static final String API_ERROR_DETAILS = "apiErrorDetails";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({VERSION, SYNC, USER_DATA, NO_HISTORY, SCAN_REQUEST, SDK_VERSION_REQUEST, ID_VERIFICATION_REQUEST,
            SUPPORTS_ISSUER_APP_VERIFICATION, APP_TO_APP_VERIFICATION, API_ERROR_DETAILS})
    public @interface Request {
    }

    public static final String DEVICE_STATUS = "deviceStatus";
    public static final String LOGOUT = "logout";
    public static final String RESOLVE = "resolve";
    public static final String UNRECOGNIZED = "unrecognized";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({DEVICE_STATUS, LOGOUT, RESOLVE, VERSION, CARD_SCANNED, SDK_VERSION, UNRECOGNIZED, ID_VERIFICATION,
            SUPPORTS_ISSUER_APP_VERIFICATION, APP_TO_APP_VERIFICATION})
    public @interface Response {
    }
}
