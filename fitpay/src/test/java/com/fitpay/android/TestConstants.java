package com.fitpay.android;

import com.fitpay.android.configs.FitpayConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Test constants
 */
public final class TestConstants {

    static Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(FitpayConfig.PROPERTY_API_BASE_URL, System.getProperty(FitpayConfig.PROPERTY_API_BASE_URL, "https://api.fit-pay.com"));
        config.put(FitpayConfig.PROPERTY_AUTH_BASE_URL, System.getProperty(FitpayConfig.PROPERTY_AUTH_BASE_URL, "https://auth.fit-pay.com"));
        config.put(FitpayConfig.PROPERTY_CLIENT_ID, System.getProperty(FitpayConfig.PROPERTY_CLIENT_ID, "fp_webapp_pJkVp2Rl"));
        config.put(FitpayConfig.PROPERTY_REDIRECT_URL, System.getProperty(FitpayConfig.PROPERTY_REDIRECT_URL, "https://webapp.fit-pay.com"));

        System.out.println("test configuration: " + config);

        return config;
    }

    static void waitSomeActionsOnServer() throws InterruptedException {
        Thread.sleep(1000);
    }
}
