package com.fitpay.android.a2averification;

import com.fitpay.android.configs.FitpayConfig;

/**
 * RTM response data for {@value com.fitpay.android.webview.enums.RtmType#SUPPORTS_ISSUER_APP_VERIFICATION}
 */
public class A2AIssuerAppVerification {

    private boolean supportsIssuerAppVerification;

    public A2AIssuerAppVerification() {
        supportsIssuerAppVerification = FitpayConfig.supportApp2App;
    }
}
