package com.fitpay.android.api.services;

import com.fitpay.android.api.models.Relationship;
import com.fitpay.android.api.models.device.ResetDeviceResult;
import com.fitpay.android.api.models.issuer.Issuers;
import com.fitpay.android.api.models.security.ECCKeyPair;
import com.fitpay.android.api.models.user.User;
import com.google.gson.JsonElement;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface FitPayClient {

    /**
     * Retrieves the details of an existing user.
     * You need only supply the unique user identifier that was returned upon user creation.
     *
     * @param userId user id
     */
    @GET("users/{userId}")
    Call<User> getUser(@Path("userId") String userId);

    /**
     * Creates a relationship between a device and a creditCard.
     *
     * @param userId       user id
     * @param creditCardId credit card id
     * @param deviceId     device id
     */
    @PUT("users/{userId}/relationships")
    Call<Relationship> createRelationship(@Path("userId") String userId,
                                          @Query("creditCardId") String creditCardId,
                                          @Query("deviceId") String deviceId);

    /**
     * Creates a new encryption key pair
     *
     * @param clientPublicKey client public key
     */
    @POST("config/encryptionKeys")
    Call<ECCKeyPair> createEncryptionKey(@Body ECCKeyPair clientPublicKey);

    /**
     * Retrieve and individual key pair.
     *
     * @param keyId key id
     */
    @GET("config/encryptionKeys/{keyId}")
    Call<ECCKeyPair> getEncryptionKey(@Query("keyId") String keyId);

    /**
     * Delete and individual key pair.
     *
     * @param keyId key id
     */
    @DELETE("config/encryptionKeys/{keyId}")
    Call<Void> deleteEncryptionKey(@Query("keyId") String keyId);

    /**
     * Get webhook
     */
    @GET("config/webhook")
    Call<Object> getWebhook();

    /**
     * Sets the webhook endpoint you would like FitPay to send notifications to, must be a valid URL.
     *
     * @param webhookURL webhook URL
     */
    @PUT("config/webhook")
    Call<Object> setWebhook(@Body String webhookURL);

    /**
     * Removes the current webhook endpoint, unsubscribing you from all Fitpay notifications.
     *
     * @param webhookURL webhook URL
     */
    @DELETE("config/webhook")
    Call<Object> removeWebhook(@Body String webhookURL);

    /**
     * Retrieve issuers
     */
    @GET("issuers")
    Call<Issuers> getIssuers();

    /**
     * Reset of payment device back to a factory state.
     *
     * @param userId user id
     * @param deviceId device id
     */
    @POST("resetDeviceTasks")
    Call<ResetDeviceResult> resetPaymentDevice(@Query("userId") String userId,
                                               @Query("deviceId") String deviceId);

    /**
     * Get status for {@link FitPayClient#resetPaymentDevice(String, String)}
     *
     * @param resetId reset id
     */
    @GET("resetDeviceTasks/{resetId}")
    Call<ResetDeviceResult> getResetPaymentDeviceStatus(@Path("resetId") String resetId);

    @GET
    Call<JsonElement> get(@Url String url);

    @GET
    Call<JsonElement> get(@Url String url, @QueryMap Map<String, Object> queryMap);

    @POST
    Call<JsonElement> post(@Url String url);

    @POST
    Call<JsonElement> post(@Url String url, @Body Object data);

    @POST
    Call<Void> postNoResponse(@Url String url, @Body Object data);

    @POST
    Call<Void> postNoResponse(@Url String url);

    @PUT
    Call<JsonElement> put(@Url String url, @Body Object data);

    @PATCH
    Call<JsonElement> patch(@Url String url, @Body JsonElement data);

    @DELETE
    Call<Void> delete(@Url String url);

    @GET("health")
    Call<Object> health();
}