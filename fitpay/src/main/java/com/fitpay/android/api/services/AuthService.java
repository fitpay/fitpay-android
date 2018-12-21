package com.fitpay.android.api.services;

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
                Request.Builder builder = getRequestBuilder(chain);
                return getResponse(chain, builder.build());
            }
        };
    }
}
