package com.fitpay.android;

import android.content.Context;

import com.fitpay.android.configs.FitpayConfig;

import org.mockito.Mockito;

import java.io.File;

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
        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getCacheDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));

        FitpayConfig.configure(context, getClientId());
        FitpayConfig.apiURL = System.getProperty(PROPERTY_API_BASE_URL, "https://api.fit-pay.com");
        FitpayConfig.authURL = System.getProperty(PROPERTY_AUTH_BASE_URL, "https://auth.fit-pay.com");
        FitpayConfig.redirectURL = System.getProperty(PROPROPERTY_REDIRECT_URLPERTY_CLIENT_ID, "https://webapp.fit-pay.com");
    }

    static void waitSomeActionsOnServer() throws InterruptedException {
        Thread.sleep(1000);
    }
}
