package com.fitpay.android.api.models.user;

import com.google.gson.annotations.SerializedName;

/**
 * User creation request
 */
public class UserCreateRequest {

    /**
     * JSON Web Encrypted compact serialization of the user's information from
     *
     * @see UserAuthInfo
     */
    @SerializedName("encryptedData")
    protected UserAuthInfo userInfo;

    @SerializedName("client_id")
    protected String clientId;

    /**
     * The version of the FitPay terms and conditions that were accepted
     */
    protected String termsVersion;

    protected String origin;

    protected Long termsAcceptedTsEpoch;
    protected Long originAccountCreatedTsEpoch;

    protected UserCreateRequest() {
        userInfo = new UserAuthInfo();
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public long getTermsAcceptedTsEpoch() {
        return termsAcceptedTsEpoch;
    }

    public long getOriginAccountCreatedTsEpoch() {
        return originAccountCreatedTsEpoch;
    }

    public String getFirstName() {
        return userInfo.firstName;
    }

    public String getLastName() {
        return userInfo.lastName;
    }

    public String getBirthDate() {
        return userInfo.birthDate;
    }

    public String getEmail() {
        return userInfo.email;
    }

    public String getOrigin() {
        return origin;
    }

    public void addCredentials(String id){
        clientId = id;
    }

    public static final class Builder {

        private String pin;
        private String firstName;
        private String lastName;
        private String email;
        private String birthDate;
        private String origin;
        private Long originAccountCreatedAtEpoch;
        private Long termsAcceptedAtEpoch;
        private String termsVersion;

        public Builder pin(String pin) {
            this.pin = pin;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder birthDate(String birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder originAccountCreatedAtEpoch(long originAccountCreatedAtEpoch) {
            this.originAccountCreatedAtEpoch = originAccountCreatedAtEpoch;
            return this;
        }

        public Builder termsAcceptedAtEpoch(long termsAcceptedAtEpoch) {
            this.termsAcceptedAtEpoch = termsAcceptedAtEpoch;
            return this;
        }

        public Builder termsVersion(String termsVersion) {
            this.termsVersion = termsVersion;
            return this;
        }

        public UserCreateRequest build() {

            UserCreateRequest uc = new UserCreateRequest();
            uc.origin = this.origin;
            uc.originAccountCreatedTsEpoch = this.originAccountCreatedAtEpoch;
            uc.termsAcceptedTsEpoch = this.termsAcceptedAtEpoch;
            uc.termsVersion = this.termsVersion;

            UserAuthInfo ud = new UserAuthInfo();
            ud.pin = this.pin;
            ud.firstName = this.firstName;
            ud.lastName = this.lastName;
            ud.birthDate = this.birthDate;
            ud.email = this.email;

            uc.userInfo = ud;

            return uc;
        }

    }

}