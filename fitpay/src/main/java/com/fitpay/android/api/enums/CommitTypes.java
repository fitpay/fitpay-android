package com.fitpay.android.api.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Commit types enum
 */
public final class CommitTypes {
    public static final String CREDITCARD_CREATED = "CREDITCARD_CREATED";
    public static final String CREDITCARD_DEACTIVATED = "CREDITCARD_DEACTIVATED";
    public static final String CREDITCARD_REACTIVATED = "CREDITCARD_REACTIVATED";
    public static final String CREDITCARD_ACTIVATED = "CREDITCARD_ACTIVATED";
    public static final String CREDITCARD_DELETED = "CREDITCARD_DELETED";
    public static final String RESET_DEFAULT_CREDITCARD = "RESET_DEFAULT_CREDITCARD";
    public static final String SET_DEFAULT_CREDITCARD = "SET_DEFAULT_CREDITCARD";
    public static final String APDU_PACKAGE = "APDU_PACKAGE";
    public static final String CREDITCARD_METADATA_UPDATED = "CREDITCARD_METADATA_UPDATED";
    public static final String CREDITCARD_PROVISION_FAILED = "CREDITCARD_PROVISION_FAILED";
    public static final String CREDITCARD_PROVISION_SUCCESS = "CREDITCARD_PROVISION_SUCCESS";
    public static final String CREDITCARD_PENDING_VERIFICATION = "CREDITCARD_PENDING_VERIFICATION";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String APDU_RESPONSE = "APDU_RESPONSE";
    public static final String SYNC = "SYNC";
    public static final String HEARTBEAT = "HEARTBEAT";
    public static final String DEVICE_CREATED = "DEVICE_CREATED";
    public static final String DEVICE_STATE_UPDATED = "DEVICE_STATE_UPDATED";
    public static final String DEVICE_DELETED = "DEVICE_DELETED";
    public static final String TRANSFER_UPDATE = "TRANSFER_UPDATE";
    public static final String SE_OPERATION_UPDATE = "SE_OPERATION_UPDATE";
    public static final String RESET_DEFAULT_CREDENTIAL = "RESET_DEFAULT_CREDENTIAL";
    public static final String CREDENTIAL_DEACTIVATED = "CREDENTIAL_DEACTIVATED";
    public static final String CREDENTIAL_REACTIVATED = "CREDENTIAL_REACTIVATED";
    public static final String STREAM_CONNECTED = "STREAM_CONNECTED";
    public static final String STREAM_DISCONNECTED = "STREAM_DISCONNECTED";
    public static final String STREAM_HEARTBEAT = "STREAM_HEARTBEAT";


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            CREDITCARD_CREATED,
            CREDITCARD_ACTIVATED,
            CREDITCARD_DEACTIVATED,
            CREDITCARD_REACTIVATED,
            CREDITCARD_DELETED,
            RESET_DEFAULT_CREDITCARD,
            SET_DEFAULT_CREDITCARD,
            APDU_PACKAGE,
            CREDITCARD_METADATA_UPDATED,
            CREDITCARD_PROVISION_FAILED,
            CREDITCARD_PROVISION_SUCCESS,
            CREDITCARD_PENDING_VERIFICATION,
            USER_CREATE,
            USER_DELETED,
            APDU_RESPONSE,
            SYNC,
            HEARTBEAT,
            DEVICE_CREATED,
            DEVICE_STATE_UPDATED,
            DEVICE_DELETED,
            TRANSFER_UPDATE,
            SE_OPERATION_UPDATE,
            RESET_DEFAULT_CREDENTIAL,
            CREDENTIAL_DEACTIVATED,
            CREDENTIAL_REACTIVATED,
            CREDENTIAL_REACTIVATED,
            STREAM_CONNECTED,
            STREAM_DISCONNECTED,
            STREAM_HEARTBEAT

    })
    public @interface Type {
    }
}
