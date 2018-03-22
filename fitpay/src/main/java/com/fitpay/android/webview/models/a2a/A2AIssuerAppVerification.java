package com.fitpay.android.webview.models.a2a;

/**
 * RTM response data for {@value com.fitpay.android.webview.enums.RtmType#SUPPORTS_ISSUER_APP_VERIFICATION}
 */
public class A2AIssuerAppVerification {

    private boolean supportsIssuerAppVerification;

    public A2AIssuerAppVerification(boolean supportsIssuerAppVerification) {
        this.supportsIssuerAppVerification = supportsIssuerAppVerification;
    }
}
