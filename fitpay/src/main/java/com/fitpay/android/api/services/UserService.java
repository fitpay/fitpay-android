package com.fitpay.android.api.services;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.KeysManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


final public class UserService extends BaseClient {

    private UserClient mClient;

    public UserService(String apiBaseUrl) {

        Interceptor interceptor = chain -> {
            Request.Builder builder = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(FP_KEY_SDK_VER, BuildConfig.SDK_VERSION);

            String keyId = KeysManager.getInstance().getKeyId(KeysManager.KEY_API);
            if (keyId != null) {
                builder.header(FP_KEY_ID, keyId);
            }

            return chain.proceed(builder.build());
        };

        OkHttpClient.Builder clientBuilder = getOkHttpClient();
        clientBuilder.addInterceptor(interceptor);

        mClient = constructClient(apiBaseUrl, clientBuilder.build());

    }

    private UserClient constructClient(String apiBaseUrl, OkHttpClient okHttpClient) {
        UserClient client = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(Constants.getGson()))
                .client(okHttpClient)
                .build()
                .create(UserClient.class);
        return client;
    }

    public UserClient getClient() {
        return mClient;
    }
}
