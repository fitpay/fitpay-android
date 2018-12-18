package com.fitpay.android.api.services;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.TestConstants;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.HttpLogging;

import java.lang.reflect.ParameterizedType;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Current file used in tests only. It overloads original GenericClient
 *
 * @param <T>
 */
public abstract class GenericClient<T> extends BaseClient {

    protected abstract Interceptor getInterceptor();

    protected T client;

    public GenericClient(String baseUrl) {
        OkHttpClient.Builder clientBuilder = getOkHttpClient();
        clientBuilder.addInterceptor(getInterceptor());
        if (TestConstants.testConfig.useRealTests() && TestConstants.testConfig.saveRealTests()) {
            clientBuilder.addInterceptor(new HttpLogging());
        }
        client = constructClient(baseUrl, clientBuilder.build());
    }

    protected Request.Builder getRequestBuilder(Interceptor.Chain chain){
        return chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cache-Control", "no-transform, no-cache")
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