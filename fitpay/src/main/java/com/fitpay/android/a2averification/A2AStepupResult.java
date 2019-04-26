package com.fitpay.android.a2averification;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Stepup result response enum. Used in {@link A2AIssuerResponse}
 */
public class A2AStepupResult {

    public static final String APPROVED = "approved";
    public static final String DECLINED = "declined";
    public static final String FAILURE = "failure";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({APPROVED, DECLINED, FAILURE})
    public @interface Response {
    }
}
