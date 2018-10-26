package com.fitpay.android.api.models.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.Link;
import com.fitpay.android.api.models.Links;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.CreditCardInfo;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.utils.TimestampUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User
 */
public final class User extends UserModel implements Parcelable {

    private static final String GET_DEVICES = "devices";
    private static final String GET_CARDS = "creditCards";
    private static final String EVENT_STREAM = "eventStream";
    private static final String WEBAPP_WALLET = "webapp.wallet";

    public User() {
    }

    public User(Parcel in) {
        this.id = in.readString();
        this.userInfo = in.readParcelable(UserInfo.class.getClassLoader());
        this.termsVersion = in.readString();
        this.createdTsEpoch = (Long) in.readValue(Long.class.getClassLoader());
        this.termsAcceptedTsEpoch = (Long) in.readValue(Long.class.getClassLoader());
        this.originAccountCreatedTsEpoch = (Long) in.readValue(Long.class.getClassLoader());
        this.links = in.readParcelable(Links.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeParcelable(this.userInfo, flags);
        dest.writeString(this.termsVersion);
        dest.writeValue(this.createdTsEpoch);
        dest.writeValue(this.termsAcceptedTsEpoch);
        dest.writeValue(this.originAccountCreatedTsEpoch);
        dest.writeParcelable(this.links, flags);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * Get eventStream url
     *
     * @return eventStream url
     */
    @Nullable
    public Link getEventStreamLink() {
        return getLink(EVENT_STREAM);
    }

    /**
     * Get webappWallet url
     *
     * @return webappWallet url
     */
    @Nullable
    public Link getWebappWalletLink() {
        return getLink(WEBAPP_WALLET);
    }

    /**
     * Delete user from your organization.
     *
     * @param callback result callback
     */
    public void deleteUser(@NonNull ApiCallback<Void> callback) {
        makeDeleteCall(callback);
    }

    /**
     * Update the details of an existing user.
     *
     * @param user     user data to update: firstName, lastName, birthDate, originAccountCreatedTs, termsAcceptedTs, termsVersion
     * @param callback result callback
     */
    public void updateUser(@NonNull User user, @NonNull ApiCallback<User> callback) {
        makePatchCall(user, true, User.class, callback);
    }

    /**
     * Retrieve a pagable collection of tokenized credit cards in their profile.
     *
     * @param limit    Max number of credit cards per page, default: 10
     * @param offset   Start index position for list of entities returned
     * @param callback result callback
     */
    public void getCreditCards(int limit, int offset, @NonNull ApiCallback<Collections.CreditCardCollection> callback) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("limit", limit);
        queryMap.put("offset", offset);
        makeGetCall(GET_CARDS, queryMap, Collections.CreditCardCollection.class, callback);
    }

    /**
     * Retrieve a pagable collection of tokenized credit cards in their profile.
     *
     * @param limit    Max number of credit cards per page, default: 10
     * @param offset   Start index position for list of entities returned
     * @param deviceId DeviceId to filter on
     * @param callback result callback
     */
    public void getCreditCards(int limit, int offset, @NonNull String deviceId, @NonNull ApiCallback<Collections.CreditCardCollection> callback) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("limit", limit);
        queryMap.put("offset", offset);
        queryMap.put("deviceId", deviceId);
        makeGetCall(GET_CARDS, queryMap, Collections.CreditCardCollection.class, callback);
    }

    /**
     * retrieve a specific user card
     *
     * @param cardId   the Id of the device to be retrieved
     * @param callback result callback
     * @deprecated as of v1.3 - Follow HATEOAS from getCreditCards instead
     */
    @Deprecated
    public void getCreditCard(String cardId, @NonNull ApiCallback<CreditCard> callback) {
        makeGetCall(GET_CARDS, cardId, null, CreditCard.class, callback);
    }

    /**
     * retrieve a pagable collection of devices in their profile.
     *
     * @param limit    Max number of devices per page, default: 10
     * @param offset   Start index position for list of entities returned
     * @param callback result callback
     */
    public void getDevices(int limit, int offset, @NonNull ApiCallback<Collections.DeviceCollection> callback) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("limit", limit);
        queryMap.put("offset", offset);
        makeGetCall(GET_DEVICES, queryMap, Collections.DeviceCollection.class, callback);
    }

    /**
     * retrieve a specific user device
     *
     * @param deviceId the Id of the device to be retrieved
     * @param callback result callback
     * @deprecated as of v1.3 - Follow HATEOAS from getDevices instead
     */
    @Deprecated
    public void getDevice(String deviceId, @NonNull ApiCallback<Device> callback) {
        makeGetCall(GET_DEVICES, deviceId, null, Device.class, callback);
    }

    /**
     * retrieve the user's current payment enabled device, if not found a null will be returned
     *
     * @param callback result callback
     * @deprecated as of v1.3 - Use getDevices and filter client side instead
     */
    @Deprecated
    public void getPaymentDevice(@NonNull ApiCallback<Device> callback) {
        getDevices(1, 0, new ApiCallback<Collections.DeviceCollection>() {
            @Override
            public void onSuccess(Collections.DeviceCollection result) {
                if (result.hasLink("paymentDevice")) {
                    ApiManager.getInstance().get(result.getLinkUrl("paymentDevice"), null, Device.class, new ApiCallback<Device>() {
                        @Override
                        public void onSuccess(Device result) {
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                            callback.onFailure(errorCode, errorMessage);
                        }
                    });
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                callback.onFailure(errorCode, errorMessage);
            }
        });
    }

    /**
     * Add a single credit card to a user's profile.
     * If the card owner has no default card, then the new card will become the default.
     * However, if the owner already has a default then it will not change.
     * To change the default, you should update the user to have a new "default_source".
     * <p>
     * <p>
     * <b>Important note:</b>
     * This call responds with a hypermedia link for accept terms. Getting the card again will not
     * result in the proper hypermedia link.
     * It's your own responsibility to store {@link CreditCard#getAcceptTermsUrl()} and restore
     * {@link CreditCard#setAcceptTermsUrl(String)} this link allowing the user to come back to the T&Cs at a later time
     * </p>
     *
     * @param creditCardInfo credit card data:(pan, expMonth, expYear, cvv, name,
     *                       address data:(street1, street2, street3, city, state, postalCode, country))
     * @param callback       result callback
     */
    public void createCreditCard(@NonNull CreditCardInfo creditCardInfo, @NonNull ApiCallback<CreditCard> callback) {
        Map<String, Object> queryMap = new HashMap<>(1);
        queryMap.put("encryptedData", creditCardInfo);

        makePostCall(GET_CARDS, queryMap, CreditCard.class, callback);
    }

    /**
     * Add a single credit card to a user's profile.
     * If the card owner has no default card, then the new card will become the default.
     * However, if the owner already has a default then it will not change.
     * To change the default, you should update the user to have a new "default_source".
     * <p>
     * <p>
     * <b>Important note:</b>
     * This call responds with a hypermedia link for accept terms. Getting the card again will not
     * result in the proper hypermedia link.
     * It's your own responsibility to store {@link CreditCard#getAcceptTermsUrl()} and restore
     * {@link CreditCard#setAcceptTermsUrl(String)} this link allowing the user to come back to the T&Cs at a later time
     * </p>
     *
     * @param creditCardInfo credit card data:(pan, expMonth, expYear, cvv, name,
     *                       address data:(street1, street2, street3, city, state, postalCode, country))
     * @param deviceId       id of the device the credit card should create a credential for
     * @param callback       result callback
     */
    public void createCreditCard(@NonNull CreditCardInfo creditCardInfo, @NonNull String deviceId, @NonNull ApiCallback<CreditCard> callback) {
        Map<String, Object> queryMap = new HashMap<>(2);
        queryMap.put("encryptedData", creditCardInfo);
        queryMap.put("deviceId", deviceId);

        makePostCall(GET_CARDS, queryMap, CreditCard.class, callback);
    }

    /**
     * Add a new device to a user's profile.
     *
     * @param device   device data to build:(deviceType, manufacturerName, deviceName, serialNumber,
     *                 modelNumber, hardwareRevision, firmwareRevision, softwareRevision, systemId,
     *                 osName, licenseKey, bdAddress, secureElementId, pairingTs)
     * @param callback result callback
     */
    public void createDevice(@NonNull Device device, @NonNull ApiCallback<Device> callback) {
        makePostCall(GET_DEVICES, device, Device.class, callback);
    }

    public static final class Builder {

        private String firstName;
        private String lastName;
        private String birthDate;
        private long originAccountCreatedAtEpoch;
        private long termsAcceptedAtEpoch;
        private String termsVersion;

        /**
         * Creates a Builder instance that can be used to build Gson with various configuration
         * settings. Builder follows the builder pattern, and it is typically used by first
         * invoking various configuration methods to set desired options, and finally calling
         * {@link #build()}.
         */
        public Builder() {
        }

        /**
         * Creates a {@link User} instance based on the current configuration. This method is free of
         * side-effects to this {@code Builder} instance and hence can be called multiple times.
         *
         * @return an instance of User configured with the options currently set in this builder
         */
        public User build() {
            User user = new User();
            user.userInfo.firstName = firstName;
            user.userInfo.lastName = lastName;
            user.userInfo.birthDate = birthDate;
            user.originAccountCreatedTsEpoch = originAccountCreatedAtEpoch;
            user.termsAcceptedTsEpoch = termsAcceptedAtEpoch;
            user.termsVersion = termsVersion;
            return user;
        }

        /**
         * Set first name
         *
         * @param firstName the user's first name
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setFirstName(@NonNull String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * Set last name
         *
         * @param lastName the user's last name
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setLastName(@NonNull String lastName) {
            this.lastName = lastName;
            return this;
        }

        /**
         * Set birthdate
         *
         * @param date time in milliseconds
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setBirthDate(long date) {
            this.birthDate = TimestampUtils.getReadableDate(date);
            return this;
        }

        /**
         * Set account creation time
         *
         * @param originAccountCreatedAt time in milliseconds
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setOriginAccountCreatedAt(long originAccountCreatedAt) {
            this.originAccountCreatedAtEpoch = originAccountCreatedAt;
            return this;
        }

        /**
         * Set terms accepted time
         *
         * @param termsAcceptedAt time in milliseconds
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setTermsAcceptedAt(long termsAcceptedAt) {
            this.termsAcceptedAtEpoch = termsAcceptedAt;
            return this;
        }

        /**
         * Set terms version
         *
         * @param termsVersion version name
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setTermsVersion(@NonNull String termsVersion) {
            this.termsVersion = termsVersion;
            return this;
        }
    }
}