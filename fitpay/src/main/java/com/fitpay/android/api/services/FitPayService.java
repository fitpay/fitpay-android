package com.fitpay.android.api.services;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.api.models.security.AccessDenied;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.KeysManager;
import com.fitpay.android.utils.RxBus;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


final public class FitPayService extends BaseClient {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_BEARER = "Bearer";
    private static final String FP_KEY_ID = "fp-key-id";

    private FitPayClient mAPIClient;
    private OAuthToken mAuthToken;

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
                    if (mAuthToken.isExpired()) {
                        FPLog.w("current access token is expired, using anyways");
                        RxBus.getInstance().post(AccessDenied.builder()
                                .reason(AccessDenied.Reason.EXPIRED_TOKEN)
                                .build());
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
                    if (response.code() == AccessDenied.INVALID_TOKEN_RESPONSE_CODE) {
                        RxBus.getInstance().post(AccessDenied.builder()
                                .reason(AccessDenied.Reason.UNAUTHORIZED)
                                .build());
                    }

                    return response;
                } finally {
                    FPLog.d(chain.request().method() + " " + chain.request().url() + " " + response.code() + " " + (System.currentTimeMillis() - startTime) + "ms");
                }
            }
        };

        OkHttpClient.Builder clientBuilder = getOkHttpClient();
        clientBuilder.addInterceptor(interceptor);

        mAPIClient = constructClient(apiBaseUrl, clientBuilder.build());
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

    public FitPayClient getClient() {
        return mAPIClient;
    }

    public void updateToken(OAuthToken token) {
        mAuthToken = token;
    }

    public String getUserId() {
        return mAuthToken.getUserId();
    }

    public boolean isAuthorized() {
        return mAuthToken != null;
    }

}
