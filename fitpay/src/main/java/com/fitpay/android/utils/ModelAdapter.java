package com.fitpay.android.utils;


import com.fitpay.android.api.models.Links;
import com.fitpay.android.api.models.Payload;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.device.CreditCardCommit;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.security.ECCKeyPair;
import com.fitpay.android.api.models.security.OAuthToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 */
final class ModelAdapter {

    private static final String TAG = ModelAdapter.class.getSimpleName();

    public static final class DataSerializer<T> implements JsonSerializer<T>, JsonDeserializer<T> {

        @Override
        public JsonElement serialize(T data, Type typeOfSrc, JsonSerializationContext context) {

            final String encryptedString = StringUtils.getEncryptedString(KeysManager.KEY_API, new GsonBuilder().create().toJson(data));

            return new JsonParser().parse(encryptedString);
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if (!json.isJsonObject() && !StringUtils.isEmpty(json.getAsString())) {

                final String decryptedString = StringUtils.getDecryptedString(KeysManager.KEY_API, json.getAsString());

                if (!StringUtils.isEmpty(decryptedString)) {
                    Gson gson = new Gson();
                    return gson.fromJson(decryptedString, typeOfT);
                }
            }

            return null;
        }

    }

    public static final class PayloadDeserializer implements JsonDeserializer<Payload> {
        @Override
        public Payload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if (!json.isJsonObject() && !StringUtils.isEmpty(json.getAsString())) {

                final String decryptedString = StringUtils.getDecryptedString(KeysManager.KEY_API, json.getAsString());
                if (!StringUtils.isEmpty(decryptedString)) {

                    Payload payload = null;
                    Gson gson = new Gson();
                    // Deserialize to desired object type based on unique key field in each object type
                    if (decryptedString.contains("creditCardId")) {
                        CreditCardCommit creditCard = gson.fromJson(decryptedString, CreditCardCommit.class);
                        payload = new Payload(creditCard);
                    } else if (decryptedString.contains("packageId")) {
                        ApduPackage apduPackage = gson.fromJson(decryptedString, ApduPackage.class);
                        payload = new Payload(apduPackage);
                    } else {
                        FPLog.w(TAG, "commit payload type is not handled.  Application could be miss receiving important events");
                    }

                    return payload;
                }
            }

            return null;
        }
    }

    public static final class DeviceSerializer implements JsonSerializer<Device> {
        public JsonElement serialize(Device data, Type typeOfSrc, JsonSerializationContext context) {

            final String encryptedString = StringUtils.getEncryptedString(KeysManager.KEY_API, new GsonBuilder().create().toJson(data));

            JsonObject jo = new JsonObject();
            jo.addProperty("encryptedData", encryptedString);
            return jo;
        }
    }

    public static final class KeyPairSerializer implements JsonSerializer<ECCKeyPair> {
        public JsonElement serialize(ECCKeyPair data, Type typeOfSrc, JsonSerializationContext context) {

            JsonObject jo = new JsonObject();
            jo.addProperty("clientPublicKey", data.getPublicKey());
            return jo;
        }
    }

    public static final class LinksDeserializer implements JsonDeserializer<Links> {

        @Override
        public Links deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            Links links = new Links();

            Set<Map.Entry<String, JsonElement>> listsSet = json.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : listsSet) {
                links.setLink(entry.getKey(), entry.getValue().getAsJsonObject().get("href").getAsString());
            }

            return links;
        }
    }

    public static final class OauthTokenDeserializer implements JsonDeserializer<OAuthToken> {
        @Override
        public OAuthToken deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject jo = json.getAsJsonObject();
            return new OAuthToken.Builder()
                    .accessToken(jo.get("access_token").getAsString())
                    .expiresIn(jo.get("expires_in").getAsLong())
                    .tokenType(jo.get("token_type").getAsString())
                    .build();
        }
    }
}
