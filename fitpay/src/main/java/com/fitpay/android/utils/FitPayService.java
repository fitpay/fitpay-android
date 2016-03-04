package com.fitpay.android.utils;

import com.fitpay.android.api.models.CreditCard;
import com.fitpay.android.api.models.Device;
import com.fitpay.android.api.models.Links;
import com.fitpay.android.api.models.Payload;
import com.fitpay.android.api.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 */
final class FitPayService {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_BEARER = "Bearer";

    private FitPayClient mAPIClient;
    private OAuthToken mAuthToken;

    public FitPayService() {

        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request.Builder builder = chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json");

                String keyId = KeysManager.getInstance().getKeyId(KeysManager.KEY_API);
                if (keyId != null) {
                    builder.header("fp-key-id", keyId);
                }

                if (mAuthToken != null) {
                    builder.header(HEADER_AUTHORIZATION, String.format("%s %s", AUTHORIZATION_BEARER, mAuthToken.getAccessToken()));
                }

                return chain.proceed(builder.build());
            }
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addInterceptor(interceptor);
        clientBuilder.addInterceptor(logging);

        Gson gson = new GsonBuilder()
                .setDateFormat(Constants.DATE_FORMAT)
                .registerTypeAdapter(ECCKeyPair.class, new ModelAdapter.KeyPairSerializer())
                .registerTypeAdapter(Links.class, new ModelAdapter.LinksDeserializer())
//                .registerTypeAdapter(Device.class, new ModelAdapter.DeviceSerializer())
                .registerTypeAdapter(User.UserInfo.class, new ModelAdapter.DataSerializer<>())
                .registerTypeAdapter(CreditCard.CreditCardInfo.class, new ModelAdapter.DataSerializer<>())
                .registerTypeAdapter(Payload.class, new ModelAdapter.PayloadDeserializer())
                .create();

        mAPIClient = new Retrofit.Builder()
                .baseUrl(Constants.API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build()
                .create(FitPayClient.class);
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

    public boolean isAuthorized(){
        return mAuthToken != null;
    }
}