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
            CREDITCARD_PROVISION_SUCCESS
    })
    public @interface Type {
    }
}
