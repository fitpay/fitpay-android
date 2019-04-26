package com.fitpay.android.api.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Response states enum
 */
public final class ResponseState {

    public static final String PROCESSED = "PROCESSED"; // all apdu commands were processed
    public static final String FAILED = "FAILED"; // at least one apdu command was not successful
    public static final String ERROR = "ERROR"; // processing was stopped due to a condition other than apdu failure (no response to apdu, etc.)
    public static final String EXPIRED = "EXPIRED"; // adpu package expired prior to being executed
    public static final String NOT_PROCESSED = "NOT_PROCESSED"; //none of apdu commands has been processed
    public static final String SKIPPED = "SKIPPED";
    public static final String SUCCESS = "SUCCESS";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PROCESSED, FAILED, ERROR, EXPIRED, NOT_PROCESSED})
    public @interface ApduState {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SUCCESS, FAILED, SKIPPED})
    public @interface CommitState {
    }
}
