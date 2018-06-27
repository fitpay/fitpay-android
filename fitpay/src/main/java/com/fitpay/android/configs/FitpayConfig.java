package com.fitpay.android.configs;

import android.support.annotation.NonNull;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Configuration Object
 * Set variables before instantiating other Fitpay objects
 */
public class FitpayConfig {

    public static final String PROPERTY_WV_URL = "wvUrl";
    public static final String PROPERTY_API_BASE_URL = "apiBaseUrl";
    public static final String PROPERTY_AUTH_BASE_URL = "authBaseUrl";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_REDIRECT_URL = "redirectUrl";

    public static final String PROPERTY_PUSH_NOTIFICATION_TOKEN = "pushNotificationToken";

    public static final String PROPERTY_SKIP_HEALTH_CHECK = "skipHealthCheck";
    public static final String PROPERTY_TIMEOUT = "timeout";
    public static final String PROPERTY_SYNC_QUEUE_SIZE = "deviceSyncRequestQueueSize";
    public static final String PROPERTY_SYNC_THREADS_COUNT = "deviceSyncRequestThreadsCount";
    public static final String PROPERTY_COMMIT_TIMERS_ENABLED = "commitTimers";
    public static final String PROPERTY_COMMIT_WARNING_TIMEOUT = "commitWarningTimeout";
    public static final String PROPERTY_COMMIT_ERROR_TIMEOUT = "commitErrorTimeout";
    public static final String PROPERTY_DISABLE_SSL_VALIDATION = "disableSslTrustValidation";
    public static final String PROPERTY_HTTP_CONNECT_TIMEOUT = "httpConnectTimeout";
    public static final String PROPERTY_HTTP_READ_TIMEOUT = "httpReadTimeout";
    public static final String PROPERTY_HTTP_WRITE_TIMEOUT = "httpWriteTimeout";
    public static final String PROPERTY_AUTOMATICALLY_SYNC_FROM_USER_EVENT_STREAM = "syncFromUserEventStream";
    public static final String PROPERTY_AUTOMATICALLY_SUBSCRIBE_TO_USER_EVENT_STREAM = "subscribeToUserEventStream";

    private static volatile FitpayConfig sInstance;

    public static FitpayConfig getInstance() {
        if (sInstance == null) {
            synchronized (FitpayConfig.class) {
                if (sInstance == null) {
                    sInstance = new FitpayConfig();
                }
            }
        }
        return sInstance;
    }

    static {
        getInstance();
    }

    private Map<String, Object> config = new HashMap<>();

    private FitpayConfig() {
        initDefault();
    }

    /**
     * Init default params
     */
    private void initDefault() {
        config.clear();

        config.put(PROPERTY_WV_URL, Constants.CONFIG_WV_URL);
        config.put(PROPERTY_API_BASE_URL, Constants.CONFIG_API_BASE_URL);
        config.put(PROPERTY_AUTH_BASE_URL, Constants.CONFIG_AUTH_BASE_URL);
        config.put(PROPERTY_CLIENT_ID, Constants.CONFIG_CLIENT_ID);
        config.put(PROPERTY_REDIRECT_URL, Constants.CONFIG_REDIRECT_URL);

        config.put(PROPERTY_SKIP_HEALTH_CHECK, false);

        config.put(PROPERTY_TIMEOUT, 10);
        config.put(PROPERTY_SYNC_QUEUE_SIZE, 10);
        config.put(PROPERTY_SYNC_THREADS_COUNT, 4);
        config.put(PROPERTY_COMMIT_WARNING_TIMEOUT, 5000);
        config.put(PROPERTY_COMMIT_ERROR_TIMEOUT, 30000);
        config.put(PROPERTY_COMMIT_TIMERS_ENABLED, true);
        config.put(PROPERTY_DISABLE_SSL_VALIDATION, false);
        config.put(PROPERTY_HTTP_CONNECT_TIMEOUT, 60);
        config.put(PROPERTY_HTTP_READ_TIMEOUT, 60);
        config.put(PROPERTY_HTTP_WRITE_TIMEOUT, 60);
        config.put(PROPERTY_AUTOMATICALLY_SUBSCRIBE_TO_USER_EVENT_STREAM, true);
        config.put(PROPERTY_AUTOMATICALLY_SYNC_FROM_USER_EVENT_STREAM, true);
    }

    /**
     * Reinitialize with default params.
     */
    public void init() {
        initDefault();
        ApiManager.clean();
    }

    /**
     * Reinitialize with default params and custom clientId
     *
     * @param clientId clientId
     */
    public void init(@NonNull String clientId) {
        init();
        config.put(PROPERTY_CLIENT_ID, clientId);
    }

    /**
     * Reinitialize with custom params (from file or url).
     *
     * @param data converted json data
     */
    public void init(Map<String, Object> data) {
        init();
        config.putAll(data);
    }

    /**
     * Set config value
     *
     * @param key  config key
     * @param data data
     */
    public void set(String key, Object data) {
        config.put(key, data);
    }

    /**
     * Get config value
     *
     * @param key config key
     * @param <T> optional param. result class type
     * @return value
     */
    public <T extends Object> T get(String key) {
        Object data = config.get(key);
        return data != null ? (T) config.get(key) : null;
    }

    /**
     * Get config value or default if null
     *
     * @param key          config key
     * @param <T>          optional param. result class type
     * @param defaultValue default value
     * @return value
     */
    public <T extends Object> T get(String key, T defaultValue) {
        T result = get(key);
        return result != null ? result : defaultValue;
    }

    /**
     * Get base api url
     *
     * @return base api url
     */
    public String getApiUrl() {
        return get(PROPERTY_API_BASE_URL);
    }

    /**
     * Get base auth url
     *
     * @return base auth url
     */
    public String getAuthUrl() {
        return get(PROPERTY_AUTH_BASE_URL);
    }

    /**
     * Get client id
     *
     * @return client id
     */
    public String getClientId() {
        return get(PROPERTY_CLIENT_ID);
    }

    /**
     * Get webView url
     *
     * @return wv url
     */
    public String getWVUrl() {
        return get(PROPERTY_WV_URL);
    }

    /**
     * Get redirect url
     *
     * @return redirect url
     */
    public String getRedirectUrl() {
        return get(PROPERTY_REDIRECT_URL);
    }

    /**
     * Get push notification token
     *
     * @return token
     */
    public String getPushNotificationToken() {
        return get(PROPERTY_PUSH_NOTIFICATION_TOKEN);
    }

    /**
     * Set push notification token
     *
     * @param token token
     */
    public void setPushNotificationToken(String token) {
        set(PROPERTY_PUSH_NOTIFICATION_TOKEN, token);
    }
}
