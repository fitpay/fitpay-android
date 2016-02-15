package com.fitpay.android.api;


import com.fitpay.android.models.AuthenticatedUser;
import com.fitpay.android.models.CreditCard;
import com.fitpay.android.models.CreditCardsCollection;
import com.fitpay.android.models.Device;
import com.fitpay.android.models.Relationship;
import com.fitpay.android.models.User;
import com.fitpay.android.models.UsersCollection;
import com.fitpay.android.models.Verification;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FitPayService {

    @FormUrlEncoded
    @POST("users/login")
    Call<AuthenticatedUser> loginUser(@FieldMap Map<String, String> options);

    /**
     * Creates a new user within your organization.
     *
     * @param user user data (firstName, lastName, birthDate, email)
     */
    @POST("users")
    Call<User> createUser(@Body User user);

    /**
     * Returns a list of all users that belong to your organization.
     * The customers are returned sorted by creation date,
     * with the most recently created customers appearing first.
     *
     * @param limit Max number of profiles per page, default: 10
     * @param offset Start index position for list of entities returned
     */
    @GET("users")
    Call<UsersCollection> getUsers(@Query("limit") int limit, @Query("offset") int offset);

    /**
     * Delete a single user from your organization.
     *
     * @param userId user id
     */
    @DELETE("users/{userId}")
    Call<Object> deleteUser(@Path("userId") String userId);

    /**
     * Update the details of an existing user.
     *
     * @param userId user id
     * @param user user data to update:(firstName, lastName, birthDate, originAccountCreatedTs, termsAcceptedTs, termsVersion)
     */
    @PATCH("users/{userId}")
    Call<User> updateUser(@Path("userId") String userId, @Body User user);

    /**
     * Retrieves the details of an existing user.
     * You need only supply the unique user identifier that was returned upon user creation.
     *
     * @param userId user id
     */
    @GET("users/{userId}")
    Call<User> getUser(@Path("userId") String userId);


    /**
     * Get a single relationship.
     *
     * @param userId user id
     * @param creditCardId credit card id
     * @param deviceId device id
     */
    @GET("users/{userId}/relationships")
    Call<Relationship> getRelationship(@Path("userId") String userId,
                                       @Query("creditCardId") String creditCardId,
                                       @Query("deviceId") String deviceId);

    /**
     * Creates a relationship between a device and a creditCard.
     *
     * @param userId user id
     * @param creditCardId credit card id
     * @param deviceId device id
     */
    @PUT("users/{userId}/relationships")
    Call<Relationship> createRelationship(@Path("userId") String userId,
                                          @Query("creditCardId") String creditCardId,
                                          @Query("deviceId") String deviceId);

    /**
     * Removes a relationship between a device and a creditCard if it exists.
     *
     * @param userId user id
     * @param creditCardId credit card id
     * @param deviceId device id
     */
    @DELETE("users/{userId}/relationships")
    Call<Object> deleteRelationship(@Path("userId") String userId,
                                    @Query("creditCardId") String creditCardId,
                                    @Query("deviceId") String deviceId);



    /**
     * For a single user, retrieve a pagable collection of tokenized credit cards in their profile.
     *
     * @param userId user id
     * @param limit Max number of credit cards per page, default: 10
     * @param offset Start index position for list of entities returned
     */
    @GET("users/{userId}/creditCards")
    Call<CreditCardsCollection> getCreditCards(@Path("userId") String userId,
                                               @Query("limit") int limit,
                                               @Query("offset") int offset);

    /**
     * Add a single credit card to a user's profile.
     * If the card owner has no default card, then the new card will become the default.
     * However, if the owner already has a default then it will not change.
     * To change the default, you should update the user to have a new "default_source".
     *
     * @param userId user id
     * @param creditCard credit card data:(pan, expMonth, expYear, cvv, name,
     *                   address data:(street1, street2, street3, city, state, postalCode, country))
     */
    @POST("users/{userId}/creditCards")
    Call<CreditCard> addCard(@Path("userId") String userId, @Body CreditCard creditCard);

    /**
     * Retrieves the details of an existing credit card.
     * You need only supply the unique identifier that was returned upon creation.
     *
     * @param userId user id
     * @param creditCardId credit card id
     */
    @GET("users/{userId}/creditCards/{creditCardId}")
    Call<CreditCard> getCreditCard(@Path("userId") String userId, @Path("creditCardId") String creditCardId);

    /**
     * Update the details of an existing credit card.
     *
     * @param userId user id
     * @param creditCardId credit card id
     * @param creditCard credit card data to update:(name (Card holder name), address/street1, address/street2,
     *                   address/city, address/state, address/postalCode, address/countryCode)
     */
    @PATCH("users/{userId}/creditCards/{creditCardId}")
    Call<CreditCard> updateCreditCard(@Path("userId") String userId,
                                      @Path("creditCardId") String creditCardId,
                                      @Body CreditCard creditCard);

    /**
     * Delete a single credit card from a user's profile.
     * If you delete a card that is currently the default source,
     * then the most recently added source will become the new default.
     * If you delete a card that is the last remaining source on the customer
     * then the default_source attribute will become null.
     *
     * @param userId user id
     * @param creditCardId credit card id
     */
    @DELETE("users/{userId}/creditCards/{creditCardId}")
    Call<Object> deleteCreditCard(@Path("userId") String userId, @Path("creditCardId") String creditCardId);


    /**
     * Indicate a user has accepted the terms and conditions presented
     * when the credit card was first added to the user's profile.
     * This link will only be available when the credit card is awaiting the user
     * to accept or decline the presented terms and conditions.
     *
     * @param userId user id
     * @param creditCardId credit card id
     */
    @POST("users/{userId}/creditCards/{creditCardId}/acceptTerms")
    Call<CreditCard> acceptTerm(@Path("userId") String userId, @Path("creditCardId") String creditCardId);

    /**
     * Indicate a user has declined the terms and conditions.
     * Once declined the credit card will be in a final state, no other actions may be taken.
     * This link will only be available when the credit card is awaiting the user to accept
     * or decline the presented terms and conditions.
     *
     * @param userId user id
     * @param creditCardId credit card id
     */
    @POST("users/{userId}/creditCards/{creditCardId}/declineTerms")
    Call<CreditCard> declineTerms(@Path("userId") String userId, @Path("creditCardId") String creditCardId);

    /**
     * Mark the credit card as the default payment instrument.
     * If another card is currently marked as the default,
     * the default will automatically transition to the indicated credit card.
     *
     * @param userId user id
     * @param creditCardId credit card id
     */
    @POST("users/{userId}/creditCards/{creditCardId}/makeDefault")
    Call<CreditCard> makeDefault(@Path("userId") String userId, @Path("creditCardId") String creditCardId);

    /**
     * Transition the credit card into a deactivated state so that it may not be utilized for payment.
     * This link will only be available for qualified credit cards that are currently in an active state.
     *
     * @param userId user id
     * @param creditCardId credit card id
     */
    @POST("users/{userId}/creditCards/{creditCardId}/deactivate")
    Call<CreditCard> deactivate(@Path("userId") String userId, @Path("creditCardId") String creditCardId);

    /**
     * Transition the credit card into an active state where it can be utilized for payment.
     * This link will only be available for qualified credit cards that are currently in a deactivated state.
     *
     * @param userId user id
     * @param creditCardId credit card id
     */
    @POST("users/{userId}/creditCards/{creditCardId}/reactivate")
    Call<CreditCard> reactivate(@Path("userId") String userId, @Path("creditCardId") String creditCardId);

    /**
     * When an issuer requires additional authentication to verify the identity of the cardholder,
     * this indicates the user has selected the specified verification method by the indicated verificationTypeId.
     *
     * @param userId user id
     * @param creditCardId credit card id
     * @param verificationTypeId verification type id
     */
    @POST("users/{userId}/creditCards/{creditCardId}/verificationMethods/{verificationTypeId}/select")
    Call<Verification> selectVerification(@Path("userId") String userId,
                                        @Path("creditCardId") String creditCardId,
                                        @Path("verificationTypeId") String verificationTypeId);

    /**
     * If a verification method is selected that requires an entry of a pin code, this transition will be available.
     * Not all verification methods will include a secondary verification step through the FitPay API.
     *
     * @param userId user id
     * @param creditCardId credit card id
     * @param verificationTypeId verification type id
     * @param verificationCode verification code
     */
    @POST("users/{userId}/creditCards/{creditCardId}/verificationMethods/{verificationTypeId}/verify")
    Call<Verification> verify(@Path("userId") String userId,
                            @Path("creditCardId") String creditCardId,
                            @Path("verificationTypeId") String verificationTypeId,
                            @Body String verificationCode);


    /**
     * For a single user, retrieve a pagable collection of devices in their profile.
     *
     * @param userId user id
     * @param limit Max number of devices per page, default: 10
     * @param offset Start index position for list of entities returned
     */
    @GET("users/{userId}/devices")
    Call<ArrayList<Device>> getDevices(@Path("userId") String userId,
                                  @Query("limit") int limit,
                                  @Query("offset") int offset);

    /**
     * For a single user, create a new device in their profile.
     *
     * @param userId user id
     * @param device device data to create:(deviceType, manufacturerName, deviceName, serialNumber,
     *               modelNumber, hardwareRevision, firmwareRevision, softwareRevision, systemId,
     *               osName, licenseKey, bdAddress, secureElementId, pairingTs)
     */
    @POST("users/{userId}/devices")
    Call<Device> createDevice(@Path("userId") String userId, @Body Device device);

    /**
     * Retrieves the details of an existing device.
     * You need only supply the unique identifier that was returned upon creation.
     *
     * @param userId user id
     * @param deviceId device id
     */
    @GET("users/{userId}/devices/{deviceId}")
    Call<Device> getDevice(@Path("userId") String userId, @Path("deviceId") String deviceId);

    /**
     * Update the details of an existing device.
     *
     * @param userId user id
     * @param deviceId device id
     */
    @PATCH("users/{userId}/devices/{deviceId}")
    Call<Device> updateDevice(@Path("userId") String userId, @Path("deviceId") String deviceId);

    /**
     * Delete a single device.
     *
     * @param userId user id
     * @param deviceId device id
     */
    @DELETE("users/{userId}/devices/{deviceId}")
    Call<Object> deleteDevice(@Path("userId") String userId, @Path("deviceId") String deviceId);

}