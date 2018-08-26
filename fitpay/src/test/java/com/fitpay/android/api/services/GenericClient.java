package com.fitpay.android.api.services;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.HttpLogging;

import java.lang.reflect.ParameterizedType;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class GenericClient<T> extends BaseClient {

    protected abstract Interceptor getInterceptor();

    protected T client;

    public GenericClient(String baseUrl) {
        OkHttpClient.Builder clientBuilder = getOkHttpClient();
        clientBuilder.addInterceptor(getInterceptor());
//        clientBuilder.addInterceptor(new FakeInterceptor());
        clientBuilder.addInterceptor(new HttpLogging());
        client = constructClient(baseUrl, clientBuilder.build());
    }

    private T constructClient(String apiBaseUrl, OkHttpClient okHttpClient) {

        Class<T> t = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];

        return new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(Constants.getGson()))
                .client(okHttpClient)
                .build()
                .create(t);
    }

    public T getClient() {
        return client;
    }
}