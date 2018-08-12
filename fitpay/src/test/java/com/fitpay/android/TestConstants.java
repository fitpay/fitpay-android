package com.fitpay.android;

import android.content.Context;

import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.FPLog;

import org.mockito.Mockito;

import java.io.File;

/**
 * Test constants
 */
public final class TestConstants {
    private final static String PROPERTY_API_BASE_URL = "apiBaseUrl";
    private final static String PROPERTY_AUTH_BASE_URL = "authBaseUrl";
    private final static String PROPERTY_CLIENT_ID = "clientId";
    private final static String PROPERTY_REDIRECT_URL = "redirectUrl";

    static String getClientId() {
        return System.getProperty(PROPERTY_CLIENT_ID, "fp_webapp_pJkVp2Rl");
    }

    static void configureFitpay(Context context) {
        addLogs();

        Mockito.when(context.getCacheDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));

        FitpayConfig.configure(context, getClientId());
        FitpayConfig.apiURL = System.getProperty(PROPERTY_API_BASE_URL, "https://api.fit-pay.com");
        FitpayConfig.authURL = System.getProperty(PROPERTY_AUTH_BASE_URL, "https://auth.fit-pay.com");
        FitpayConfig.redirectURL = System.getProperty(PROPERTY_REDIRECT_URL, "https://webapp.fit-pay.com");
    }

    static void waitSomeActionsOnServer() throws InterruptedException {
        Thread.sleep(1000);
    }

    static void addLogs(){
        FPLog.clean(); //in tests only one log impl should be used
        FPLog.addLogImpl(new FPLog.ILog() {
            @Override
            public void v(String tag, String text) {
                System.out.println(tag + " VERBOSE (" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void d(String tag, String text) {
                System.out.println(tag + " DEBUG (" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void i(String tag, String text) {
                System.out.println(tag + " INFO(" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void w(String tag, String text) {
                System.out.println(tag + " WARN(" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void e(String tag, Throwable throwable) {
                System.out.println(tag + " ERROR (" + Thread.currentThread().getName() + "): " + tag);

                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }

            @Override
            public int logLevel() {
                return FPLog.VERBOSE;
            }
        });
        FPLog.setShowHTTPLogs(false);
    }
}
