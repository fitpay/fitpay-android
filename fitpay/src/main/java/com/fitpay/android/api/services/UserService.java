package com.fitpay.android.api.services;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.utils.KeysManager;

import okhttp3.Interceptor;
import okhttp3.Request;


final public class UserService extends GenericClient<UserClient> {

    public UserService(String apiBaseUrl) {
        super(apiBaseUrl);
    }

    @Override
    protected Interceptor getInterceptor() {
        return chain -> {
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
    }
}
