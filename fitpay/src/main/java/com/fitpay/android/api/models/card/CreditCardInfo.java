package com.fitpay.android.api.models.card;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.models.IdVerification;

import java.util.Calendar;
import java.util.IllegalFormatException;

/***
 * Credit card info
 */
public final class CreditCardInfo implements Parcelable {

    String name;
    String cvv;
    String pan;
    Integer expMonth;
    Integer expYear;
    String language;
    Address address;

    /**
     * description : Card holder risk data
     */
    IdVerification riskData;

    CreditCardInfo() {
    }

    /**
     * The Card holder name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * TThe credit card cvv2 code
     *
     * @return cvv
     */
    public String getCVV() {
        return cvv;
    }

    /**
     * The credit card number, also known as a Primary Account Number (PAN)
     *
     * @return pan
     */
    public String getPan() {
        return pan;
    }

    /**
     * The credit card expiration month
     *
     * @return expMonth
     */
    public Integer getExpMonth() {
        return expMonth;
    }

    /**
     * The credit card expiration year
     *
     * @return expYear
     */
    public Integer getExpYear() {
        return expYear;
    }

    /**
     * Credit card language. Should use the format [language designator ISO-639-1]‌
     *
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Card holder address
     *
     * @return address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Risk data
     *
     * @return riskData
     */
    public IdVerification getRiskData() {
        return riskData;
    }

    @Override
    public String toString() {
        return "CreditCardInfo";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.cvv);
        dest.writeString(this.pan);
        dest.writeValue(this.expMonth);
        dest.writeValue(this.expYear);
        dest.writeValue(this.language);
        dest.writeParcelable(this.address, flags);
        dest.writeParcelable(this.riskData, flags);
    }

    protected CreditCardInfo(Parcel in) {
        this.name = in.readString();
        this.cvv = in.readString();
        this.pan = in.readString();
        this.expMonth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.expYear = (Integer) in.readValue(Integer.class.getClassLoader());
        this.language = in.readString();
        this.address = in.readParcelable(Address.class.getClassLoader());
        this.riskData = in.readParcelable(IdVerification.class.getClassLoader());
    }

    public static final Parcelable.Creator<CreditCardInfo> CREATOR = new Parcelable.Creator<CreditCardInfo>() {
        @Override
        public CreditCardInfo createFromParcel(Parcel source) {
            return new CreditCardInfo(source);
        }

        @Override
        public CreditCardInfo[] newArray(int size) {
            return new CreditCardInfo[size];
        }
    };

    public static final class Builder {

        private String name;
        private String cvv;
        private String pan;
        private Integer expMonth;
        private Integer expYear;
        private String language;
        private Address address;
        private IdVerification riskData;

        /**
         * Creates a Builder instance that can be used to build Gson with various configuration
         * settings. Builder follows the builder pattern, and it is typically used by first
         * invoking various configuration methods to set desired options, and finally calling
         * {@link #build()}.
         */
        public Builder() {
        }

        /**
         * Creates a {@link CreditCardInfo} instance based on the current configuration. This method is free of
         * side-effects to this {@code Builder} instance and hence can be called multiple times.
         *
         * @return an instance of {@link CreditCardInfo} configured with the options currently set in this builder
         */
        public CreditCardInfo build() {
            CreditCardInfo creditCardInfo = new CreditCardInfo();
            creditCardInfo.name = name;
            creditCardInfo.cvv = cvv;
            creditCardInfo.pan = pan;
            creditCardInfo.expYear = expYear;
            creditCardInfo.expMonth = expMonth;
            creditCardInfo.language = language;
            creditCardInfo.address = address;
            creditCardInfo.riskData = riskData;
            return creditCardInfo;
        }

        /**
         * Set card holder name
         *
         * @param name card holder name
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setName(@NonNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Set credit card cvv2 code
         *
         * @param cvv cards's cvv2 code. string with 3 digits only
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setCVV(@Nullable String cvv) {
            if (StringUtils.isEmpty(cvv)) {
                cvv = null;
            } else {
                cvv = cvv.trim();
            }

            this.cvv = cvv;
            return this;
        }

        /**
         * Set credit card primary account number (PAN)
         *
         * @param pan cards's PAN. string with 16 digits only
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setPAN(@NonNull String pan) {
            this.pan = pan;
            return this;
        }

        /**
         * Set credit card expiration date
         *
         * @param expYear  cards's expiration year
         * @param expMonth cards's expiration month
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setExpDate(Integer expYear, Integer expMonth) throws IllegalFormatException {
            if (expYear != null) {
                Calendar calendar = Calendar.getInstance();
                if (expMonth != null && expYear < calendar.get(Calendar.YEAR) && expMonth < calendar.get(Calendar.MONTH) + 1) {
                    throw new IllegalArgumentException("Incorrect expiration date. Date is in the past.");
                } else if (expMonth == null && expYear < calendar.get(Calendar.YEAR)) {
                    throw new IllegalArgumentException("Incorrect expiration date. Year is in the past.");
                }
            } else if (expMonth != null) {
                throw new IllegalArgumentException("Incorrect expiration date. Month was specified without year.");
            }

            this.expYear = expYear;
            this.expMonth = expMonth;
            return this;
        }

        /**
         * Set card holder address
         *
         * @param language credit card language
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setLanguage(@NonNull String language) {
            this.language = language;
            return this;
        }

        /**
         * Set card holder address
         *
         * @param address card holder address
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setAddress(@NonNull Address address) {
            this.address = address;
            return this;
        }


        /**
         * Set risk data {@link IdVerification}
         *
         * @param riskData card holder risk data
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setRiskData(@NonNull IdVerification riskData) {
            this.riskData = riskData;
            return this;
        }
    }
}
