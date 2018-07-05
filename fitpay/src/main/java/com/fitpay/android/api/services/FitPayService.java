package com.fitpay.android.api.services;

import android.util.Log;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.api.models.PlatformConfig;
import com.fitpay.android.api.models.security.AccessDenied;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.KeysManager;
import com.fitpay.android.utils.RxBus;
import com.google.gson.JsonElement;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


final public class FitPayService extends BaseClient {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_BEARER = "Bearer";
    private static final String FP_KEY_ID = "fp-key-id";

    private FitPayClient mAPIClient;
    private OAuthToken mAuthToken;
    private boolean expiredNotificationSent;

    private static final String PLATFORM_CONFIG_URL = "http://s3.amazonaws.com/crypto-web-prod/mobile/config.json";
    private PlatformConfig platformConfig;

    public FitPayService(String apiBaseUrl) {

        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header(FP_KEY_SDK_VER, BuildConfig.SDK_VERSION);

                String keyId = KeysManager.getInstance().getKeyId(KeysManager.KEY_API);
                if (keyId != null) {
                    builder.header(FP_KEY_ID, keyId);
                }

                if (mAuthToken != null) {
                    if (!expiredNotificationSent && mAuthToken.isExpired()) {
                        FPLog.w("current access token is expired, using anyways");
                        RxBus.getInstance().post(AccessDenied.builder()
                                .reason(AccessDenied.Reason.EXPIRED_TOKEN)
                                .build());

                        expiredNotificationSent = true;
                    }

                    final String value = new StringBuilder()
                            .append(AUTHORIZATION_BEARER)
                            .append(" ")
                            .append(mAuthToken.getAccessToken())
                            .toString();

                    builder.header(HEADER_AUTHORIZATION, value);
                }

                long startTime = System.currentTimeMillis();
                Response response = null;
                try {
                    response = chain.proceed(builder.build());

                    if (response != null && response.code() == AccessDenied.INVALID_TOKEN_RESPONSE_CODE) {
                        RxBus.getInstance().post(AccessDenied.builder()
                                .reason(AccessDenied.Reason.UNAUTHORIZED)
                                .build());
                    }

                    return response;
                } finally {
                    FPLog.d(String.format("%s %s %s %dms",
                            chain.request().method(),
                            chain.request().url(),
                            response != null ? response.code() : "null",
                            System.currentTimeMillis() - startTime));
                }
            }
        };

        OkHttpClient.Builder clientBuilder = getOkHttpClient();
        clientBuilder.addInterceptor(interceptor);

        mAPIClient = constructClient(apiBaseUrl, clientBuilder.build());
        constructPlatformConfig();
    }

    private FitPayClient constructClient(String apiBaseUrl, OkHttpClient okHttpClient) {
        FitPayClient client = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(Constants.getGson()))
                .client(okHttpClient)
                .build()
                .create(FitPayClient.class);
        return client;
    }

    private void constructPlatformConfig() {
        if (mAPIClient == null) {
            throw new IllegalStateException("invalid state, not okhttp client is currently set");
        }

        rx.Observable.defer(() -> {
            try {
                retrofit2.Response<JsonElement> result = mAPIClient.get(PLATFORM_CONFIG_URL).execute();

                JsonElement body = result.body();

                if (body.getAsJsonObject().has("android")) {
                    platformConfig = Constants.getGson().fromJson(body.getAsJsonObject().getAsJsonObject("android"), PlatformConfig.class);
                } else {
                    FPLog.d("platformConfiguration " + body.toString() + " from " + PLATFORM_CONFIG_URL + " does not have an android section, using defaults");
                }
            } catch (Exception e) {
                FPLog.e("error getting platform configuration from " + PLATFORM_CONFIG_URL + ", using defaults", e);
                platformConfig = new PlatformConfig();
            }
            return rx.Observable.empty();
        }).subscribeOn(Schedulers.io()).toBlocking().subscribe();

        FPLog.d("platformConfiguration: " + platformConfig);
    }

    public FitPayClient getClient() {
        return mAPIClient;
    }

    public void updateToken(OAuthToken token) {
        mAuthToken = token;
        expiredNotificationSent = false;
    }

    public OAuthToken getToken() {
        return mAuthToken;
    }

    public String getUserId() {
        return mAuthToken.getUserId();
    }

    public boolean isAuthorized() {
        return mAuthToken != null;
    }

    public PlatformConfig getPlatformConfig() {
        return platformConfig;
    }
}
