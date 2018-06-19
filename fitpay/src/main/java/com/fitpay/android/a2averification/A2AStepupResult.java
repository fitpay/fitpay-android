package com.fitpay.android.a2averification;

import android.support.annotation.StringDef;

/**
 * Stepup result response enum. Used in {@link A2AIssuerResponse}
 */
public class A2AStepupResult {

    public static final String APPROVED = "approved";
    public static final String DECLINED = "declined";
    public static final String FAILURE = "failure";

    @StringDef({APPROVED, DECLINED, FAILURE})
    public @interface Response {
    }
}
