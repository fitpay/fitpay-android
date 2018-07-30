package com.fitpay.android.configs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Main Configuration Object
 * Set variables before instantiating other Fitpay objects
 */
public final class FitpayConfig {

    private static final String TAG = FitpayConfig.class.getSimpleName();

    /**
     * Application context
     */
    public static Context appContext;

    /**
     * Implicit allows you to get a single user token
     */
    public static String clientId;

    /**
     * Used for web calls
     */
    @NonNull
    public static String webURL = Constants.CONFIG_WV_URL;

    /**
     * Used for redirects
     */
    @NonNull
    public static String redirectURL = Constants.CONFIG_REDIRECT_URL;

    /**
     * Used for API calls
     */
    @NonNull
    public static String apiURL = Constants.CONFIG_API_BASE_URL;

    /**
     * Used during login
     */
    @NonNull
    public static String authURL = Constants.CONFIG_AUTH_BASE_URL;

    /**
     * By default the app-to-app verification capability should be disabled.
     * Called by the parser when it receives {#value {@link com.fitpay.android.webview.enums.RtmType#SUPPORTS_ISSUER_APP_VERIFICATION}} event
     *
     * Mark whether you support app-to-app verifications.
     */
    public static boolean supportApp2App;

    /**
     * Setup FitpaySDK with default params and custom clientId
     *
     * @param context app context
     * @param clientId   clientId
     */
    public static void configure(@NonNull Context context, @NonNull String clientId) {
        configure(context, new FitpayConfigModel(clientId));
    }

    /**
     * Setup FitpaySDK with data from other source: file or web.
     * Internal use only
     *
     * @param context app context
     * @param configModel parsed config model
     */
    private static void configure(@NonNull Context context, @NonNull FitpayConfigModel configModel) {
        appContext = context;
        clientId = configModel.getClientId();
        webURL = configModel.getWebUrl();
        redirectURL = configModel.getRedirectUrl();
        apiURL = configModel.getApiURL();
        authURL = configModel.getAuthURL();
        supportApp2App = configModel.isSupportA2A();
        Web.demoMode = configModel.getWebConfig().demoMode;
        Web.demoCardGroup = configModel.getWebConfig().demoCardGroup;
        Web.cssURL = configModel.getWebConfig().cssURL;
        Web.baseLanguageURL = configModel.getWebConfig().baseLanguageURL;
        Web.supportCardScanner = configModel.getWebConfig().supportCardScanner;
        Web.automaticallySubscribeToUserEventStream = configModel.getWebConfig().automaticallySubscribeToUserEventStream;
        Web.automaticallySyncFromUserEventStream = configModel.getWebConfig().automaticallySyncFromUserEventStream;
        Web.accessToken = configModel.getWebConfig().accessToken;

        ApiManager.clean();
    }

    /**
     * Setup FitpaySDK with params from file.
     *
     * @param context app context
     * @param stream  input stream
     */
    public static void configure(@NonNull Context context, @NonNull InputStream stream) {
        FitpayConfigModel configModel = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            configModel = Constants.getGson().fromJson(in, FitpayConfigModel.class);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        if (configModel == null) {
            throw new NullPointerException("Can't load FitpayConfig from file. Check the logs or try to use #configure(clientId)");
        }

        configure(context, configModel);
    }

    /**
     * Web config. Configuration options related to the Web specifically
     */
    public static class Web {
        /**
         * Shows autofill options on the add card page when enabled
         */
        public static boolean demoMode;

        /**
         * Changes autofill options to include a default and auto-verify version of one card type
         */
        public static String demoCardGroup;

        /**
         * Overrides the default CSS
         */
        public static String cssURL;

        /**
         * [Getting Started with Translations]: https://support.fit-pay.com/hc/en-us/articles/115003060672-Getting-Started-with-Translations
         * <p>
         * Base URL to language files used in conjuction with language parameter in RTM
         * <p>
         * More info at [Getting Started with Translations]
         */
        public static String baseLanguageURL;

        /**
         * Turn on when you are ready to implement card scanning methods
         */
        public static boolean supportCardScanner;

        /**
         * Turn off SSE connection to reduce overhead if not in use
         */
        public static boolean automaticallySubscribeToUserEventStream;

        /**
         * Trigger syncs from an SSE connection automatically established.
         * {@link #automaticallySubscribeToUserEventStream} must also be on to sync
         */
        public static boolean automaticallySyncFromUserEventStream;

        /**
         * skips the pin screen if valid
         */
        public static String accessToken;
    }

    /**
     * User config. Configuration options related to the user
     */
    public static class User {
        /**
         * Used for storing app push notification token
         */
        public static String pushNotificationToken;
    }
}
