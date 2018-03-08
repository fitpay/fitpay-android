package com.fitpay.android.utils;

import com.fitpay.android.api.models.Links;
import com.fitpay.android.api.models.Payload;
import com.fitpay.android.api.models.card.CreditCardInfo;
import com.fitpay.android.api.models.security.ECCKeyPair;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.api.models.user.UserAuthInfo;
import com.fitpay.android.api.models.user.UserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class Constants {

    public static final String FIT_PAY_TAG = "FitPay";

    public final static String SYNC_DATA = "SYNC_DATA";
    public final static String APDU_DATA = "APDU_DATA";
    public final static String WV_DATA = "WV_DATA";

    public final static String A2A_STEP_UP_AUTH_CODE = "STEP_UP_AUTH_CODE";
    public final static String A2A_STEP_UP_AUTH_RESPONSE = "STEP_UP_RESPONSE";
    public final static String A2A_STEP_UP_AUTH_ERROR = "STEP_UP_ERROR";

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    static final String DATE_FORMAT_SIMPLE = "yyyy-MM-dd";

    private static Gson gson;

    private static Executor executor = Executors.newSingleThreadExecutor();

    public static Executor getExecutor() {
        return executor;
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setDateFormat(Constants.DATE_FORMAT)
                    .registerTypeAdapter(ECCKeyPair.class, new ModelAdapter.KeyPairSerializer())
                    .registerTypeAdapter(Links.class, new ModelAdapter.LinksDeserializer())
                    .registerTypeAdapter(UserInfo.class, new ModelAdapter.DataSerializer<>())
                    .registerTypeAdapter(CreditCardInfo.class, new ModelAdapter.DataSerializer<>())
                    .registerTypeAdapter(Payload.class, new ModelAdapter.PayloadDeserializer())
                    .registerTypeAdapter(UserAuthInfo.class, new ModelAdapter.DataSerializer<>())
                    .registerTypeAdapter(OAuthToken.class, new ModelAdapter.OauthTokenDeserializer())
                    .create();
        }
        return gson;
    }

    @Deprecated
    public static void printError(Throwable error) {
        FPLog.e(error);
    }

    @Deprecated
    public static void printError(String error) {
        FPLog.e(error);
    }
}

