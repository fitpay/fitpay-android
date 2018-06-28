package com.fitpay.android;

import com.fitpay.android.configs.FitpayConfig;

/**
 * Test constants
 */
public final class TestConstants {
    private final static String PROPERTY_API_BASE_URL = "apiBaseUrl";
    private final static String PROPERTY_AUTH_BASE_URL = "authBaseUrl";
    private final static String PROPERTY_CLIENT_ID = "clientId";
    private final static String PROPROPERTY_REDIRECT_URLPERTY_CLIENT_ID = "redirectUrl";

    static String getClientId(){
        return System.getProperty(PROPERTY_CLIENT_ID, "fp_webapp_pJkVp2Rl");
    }

    static void configureFitpay() {
        FitpayConfig.apiURL = System.getProperty(PROPERTY_API_BASE_URL, "https://api.fit-pay.com");
        FitpayConfig.authURL = System.getProperty(PROPERTY_AUTH_BASE_URL, "https://auth.fit-pay.com");
        FitpayConfig.clientId = System.getProperty(PROPERTY_CLIENT_ID, getClientId());
        FitpayConfig.redirectURL = System.getProperty(PROPROPERTY_REDIRECT_URLPERTY_CLIENT_ID, "https://webapp.fit-pay.com");
    }

    static void waitSomeActionsOnServer() throws InterruptedException {
        Thread.sleep(1000);
    }
}
