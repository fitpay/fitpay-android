package com.fitpay.android.api.services;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public abstract class FitPayInterceptor implements Interceptor {

    public Response getResponse(Chain chain, Request request) throws IOException {
        return chain.proceed(request);
    }
}
