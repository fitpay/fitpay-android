package com.fitpay.android.api.services;

import com.fitpay.android.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final public class AuthService extends GenericClient<AuthClient> {

    public AuthService(String baseUrl) {
        super(baseUrl);
    }

    @Override
    protected Interceptor getInterceptor() {
        return new FitPayInterceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header(FP_KEY_SDK_VER, BuildConfig.SDK_VERSION);

                return getResponse(chain, builder.build());
            }
        };
    }
}
