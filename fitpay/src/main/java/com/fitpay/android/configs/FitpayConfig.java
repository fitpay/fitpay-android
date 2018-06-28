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
     * Implicit allows you to get a single user token
     */
    public static String clientId;

    /**
     * Used for web calls
     */
    public static String webURL;

    /**
     * Used for redirects
     */
    public static String redirectURL;

    /**
     * Used for API calls
     */
    public static String apiURL;

    /**
     * Used during login
     */
    public static String authURL;

    /**
     * Used for storing app push notification token
     */
    public static String pushNotificationToken;

    /**
     * Determines if the SDK will perform and initial health check
     */
    public static boolean skipHealthCheck;

    static {
        configure();
    }

    /**
     * Setup FitpaySDK with default params
     */
    public static void configure() {
        configure(new FitpayConfigModel());
    }

    /**
     * Setup FitpaySDK with default params and custom clientId
     *
     * @param clientId clientId
     */
    public static void configure(@NonNull String clientId) {
        configure(new FitpayConfigModel(clientId));
    }

    /**
     * Setup FitpaySDK with data from other source: file or web.
     * Internal use only
     *
     * @param configModel parsed config model
     */
    private static void configure(@NonNull FitpayConfigModel configModel) {
        clientId = configModel.getClientId();
        webURL = configModel.getWebUrl();
        redirectURL = configModel.getRedirectUrl();
        apiURL = configModel.getApiURL();
        authURL = configModel.getAuthURL();
        skipHealthCheck = configModel.skipHealthCheck();
        Web.demoMode = configModel.getWebConfig().demoMode;
        Web.demoCardGroup = configModel.getWebConfig().demoCardGroup;
        Web.cssURL = configModel.getWebConfig().cssURL;
        Web.baseLanguageURL = configModel.getWebConfig().baseLanguageURL;
        Web.supportCardScanner = configModel.getWebConfig().supportCardScanner;
        Web.automaticallySubscribeToUserEventStream = configModel.getWebConfig().automaticallySubscribeToUserEventStream;
        Web.automaticallySyncFromUserEventStream = configModel.getWebConfig().automaticallySyncFromUserEventStream;


        ApiManager.clean();
    }

    /**
     * Setup FitpaySDK with params from file.
     *
     * @param context        app context
     * @param assetsFileName json file name in assets folder
     */
    public static void configure(@NonNull Context context, @NonNull String assetsFileName) {
        try {
            InputStream json = context.getAssets().open(assetsFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            FitpayConfigModel configModel = Constants.getGson().fromJson(in, FitpayConfigModel.class);
            if (configModel == null) {
                configModel = new FitpayConfigModel();
            }
            configure(configModel);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            configure();
        }
    }

    /**
     * Web config. Configuration options related to the Web specifically
     */
    public static class Web {
        /**
         * Shows autofill options on the add card page when enabled
         */
        public static boolean demoMode = false;

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
        public static boolean supportCardScanner = false;

        /**
         * Turn off SSE connection to reduce overhead if not in use
         */
        public static boolean automaticallySubscribeToUserEventStream = true;

        /**
         * Trigger syncs from an SSE connection automatically established.
         * {@link #automaticallySubscribeToUserEventStream} must also be on to sync
         */
        public static boolean automaticallySyncFromUserEventStream = true;
    }

}
