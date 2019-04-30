package com.fitpay.android.utils;

import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.api.models.Links;
import com.fitpay.android.api.models.Payload;
import com.fitpay.android.api.models.Transaction;
import com.fitpay.android.api.models.card.CreditCardInfo;
import com.fitpay.android.api.models.card.OfflineSeActions;
import com.fitpay.android.api.models.collection.CountryCollection;
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

    public static final String CONFIG_WV_URL = "https://webapp.fit-pay.com";
    public static final String CONFIG_API_BASE_URL = "https://api.fit-pay.com";
    public static final String CONFIG_AUTH_BASE_URL = "https://auth.fit-pay.com";
    public static final String CONFIG_REDIRECT_URL = "https://webapp.fit-pay.com";
    public static final String CONFIG_CSS_URL = "https://fitpaycss.github.io/pagare.css";

    public final static String SYNC_DATA = "SYNC_DATA";
    public final static String APDU_DATA = "APDU_DATA";
    public final static String WV_DATA = "WV_DATA";

    public static final int INTENT_TAKE_PHOTO_REQUEST = 41280;
    public static final int INTENT_TAKE_PHOTO_PERMISSION_REQUEST = 41290;
    public static final int INTENT_A2A_VERIFICATION_REQUEST = 41300;

    public final static String A2A_STEP_UP_AUTH_CODE = "STEP_UP_AUTH_CODE";
    public final static String A2A_STEP_UP_AUTH_RESPONSE = "STEP_UP_RESPONSE";

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
                    .registerTypeAdapter(Transaction.EncryptedTransaction.class, new ModelAdapter.DataSerializer<>())
                    .registerTypeAdapter(OAuthToken.class, new ModelAdapter.OauthTokenDeserializer())
                    .registerTypeAdapter(ErrorResponse.ErrorMessage.class, new ModelAdapter.ErrorMessageDeserializer())
                    .registerTypeAdapter(CountryCollection.class, new ModelAdapter.CountryCollectionDeserializer())
                    .registerTypeAdapter(OfflineSeActions.class, new ModelAdapter.OfflineSeActionsDeserializer())
                    .create();
        }
        return gson;
    }

}

