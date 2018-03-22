package com.fitpay.android.webview.enums;

import android.support.annotation.StringDef;

/**
 * Stepup result response enum. Used in {@link com.fitpay.android.webview.models.a2a.A2AIssuerResponse}
 */
public class A2AStepupResult {

    public static final String APPROVED = "approved";
    public static final String DECLINED = "declined";
    public static final String FAILURE = "failure";

    @StringDef({APPROVED, DECLINED, FAILURE})
    public @interface Response {
    }
}
