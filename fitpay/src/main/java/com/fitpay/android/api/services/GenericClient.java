package com.fitpay.android.api.services;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.utils.Constants;

import java.lang.reflect.ParameterizedType;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Abstract web client.
 *
 * @param <T>
 */
public abstract class GenericClient<T> extends BaseClient {

    protected abstract Interceptor getInterceptor();

    protected T client;

    public GenericClient(String baseUrl) {
        OkHttpClient.Builder clientBuilder = getOkHttpClient();
        Interceptor interceptor = getInterceptor();
        if (interceptor != null) {
            clientBuilder.addInterceptor(getInterceptor());
        }
        client = constructClient(baseUrl, clientBuilder.build());
    }

    public Request.Builder getRequestBuilder(Interceptor.Chain chain){
        return chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cache-Control", "max-age=300, no-transform, no-cache")
                .header(FP_KEY_SDK_VER, BuildConfig.SDK_VERSION);
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