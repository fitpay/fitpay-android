package com.fitpay.android.api.services;

import com.fitpay.android.BuildConfig;

import okhttp3.Interceptor;
import okhttp3.Request;


final public class AuthService extends GenericClient<AuthClient> {

    public AuthService(String baseUrl) {
        super(baseUrl);
    }

    @Override
    protected Interceptor getInterceptor() {
        return chain -> {
            Request.Builder builder = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(FP_KEY_SDK_VER, BuildConfig.SDK_VERSION);

            return chain.proceed(builder.build());
        };
    }
}
