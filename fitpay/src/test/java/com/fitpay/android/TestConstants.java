package com.fitpay.android;

import com.fitpay.android.api.ApiManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Test constants
 */
public final class TestConstants {

    static Map<String, String> getConfig() {
        String baseURL = System.getenv(ApiManager.PROPERTY_API_BASE_URL);
        String authURL = System.getenv(ApiManager.PROPERTY_AUTH_BASE_URL);
        String clientID = System.getenv(ApiManager.PROPERTY_CLIENT_ID);
        String redirectURI = System.getenv(ApiManager.PROPERTY_REDIRECT_URI);

        System.out.print("clientID:" + clientID);

        Map<String, String> config = new HashMap<>();
        config.put(ApiManager.PROPERTY_API_BASE_URL, baseURL != null ? baseURL : "https://api.fit-pay.com");
        config.put(ApiManager.PROPERTY_AUTH_BASE_URL, authURL != null ? authURL : "https://auth.fit-pay.com");
        config.put(ApiManager.PROPERTY_CLIENT_ID, clientID != null ? clientID : "fp_webapp_pJkVp2Rl");
        config.put(ApiManager.PROPERTY_REDIRECT_URI, redirectURI != null ? redirectURI : "https://webapp.fit-pay.com");
        return config;
    }

    static void waitSomeActionsOnServer() throws InterruptedException {
        Thread.sleep(1000);
    }
}
