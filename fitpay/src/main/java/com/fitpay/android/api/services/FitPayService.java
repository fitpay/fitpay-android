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
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final public class FitPayService extends GenericClient<FitPayClient> {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_BEARER = "Bearer";
    private static final String FP_KEY_ID = "fp-key-id";

    private OAuthToken mAuthToken;
    private boolean expiredNotificationSent;

    private PlatformConfig platformConfig = new PlatformConfig();

    public FitPayService(String apiBaseUrl) {
        super(apiBaseUrl);
        constructPlatformConfig();
    }

    @Override
    public Interceptor getInterceptor() {
        return new FitPayInterceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder()
                        .header("Content-Type", "application/json")
                        .header(FP_KEY_SDK_VER, BuildConfig.SDK_VERSION);

                String acceptHeader = chain.request().header("Accept");
                if (acceptHeader == null) { // don't override accept header if already present
                    builder.header("Accept", "application/json");
                }

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

                    final String value = AUTHORIZATION_BEARER + " " + mAuthToken.getAccessToken();

                    builder.header(HEADER_AUTHORIZATION, value);
                }

                long startTime = System.currentTimeMillis();
                Response response = null;
                try {
                    response = getResponse(chain, builder.build());

                    if (response != null && response.code() == AccessDenied.INVALID_TOKEN_RESPONSE_CODE) {
                        RxBus.getInstance().post(AccessDenied.builder()
                                .reason(AccessDenied.Reason.UNAUTHORIZED)
                                .build());
                    }

                    return response;
                } finally {
                    printLog(String.format(Locale.US, "%s %s %s %dms",
                            chain.request().method(),
                            chain.request().url(),
                            response != null ? response.code() : "null",
                            System.currentTimeMillis() - startTime));
                }
            }
        };
    }

    private void constructPlatformConfig() {
        if (client == null) {
            throw new IllegalStateException("invalid state, not okhttp client is currently set");
        }

        Completable.fromAction(() -> {
            try {
                retrofit2.Response<JsonElement> response = client.getPlatformConfig().execute();

                if (response.isSuccessful() && response.errorBody() == null) {
                    JsonObject body = response.body().getAsJsonObject();
                    if (body.has("android")) {
                        platformConfig = Constants.getGson().fromJson(body.getAsJsonObject("android"), PlatformConfig.class);
                    }
                } else {
                    FPLog.e("error getting platform configuration from platform, using defaults");
                }
            } catch (Exception e) {
                FPLog.e("error getting platform configuration from platform, using defaults", e);
            }
        }).subscribeOn(Schedulers.io()).blockingAwait();

        FPLog.d("platformConfiguration: " + platformConfig);
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
