package com.fitpay.android.webview.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.enums.DeviceTimeZone;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessageResponse;

import java.util.Date;
import java.util.Locale;

/**
 * IdVerification data
 */

public final class IdVerification implements Parcelable {
    private Date oemAccountInfoUpdatedDate; // Most recent date this user update their: Billing Address, Name, Email, password, or other Personally Identifiable Information associated to their account.
    private Date oemAccountCreatedDate;
    private Integer suspendedCardsInOemAccount; // If this user has multiple devices, how many cards are suspended in total across all devices?
    private Date lastOemAccountActivityDate; // Date this account was previously used, never today.
    private Date deviceLostModeDate; // Date this device was reported lost or stolen. Don't send if you don't have it.
    private Integer devicesWithIdenticalActiveToken;
    private Integer activeTokensOnAllDevicesForOemAccount; // If this user has multiple devices, how many cards are active in total across all devices?"
    private Integer oemAccountScore; // int between 0-9
    private Integer deviceScore; // int between 0-9
    private Boolean nfcCapable; // Only needed if your device is NOT nfcCapable

    private String billingCountryCode; // Country of user's billing address in ISO 3166-1 alpha-2 format, e.g., US; maximum 2 characters
    private String oemAccountCountryCode; // Country setting of account or phone in ISO 3166-1 alpha-2 format
    private String deviceCountry; // Country setting of payment device
    private String oemAccountUserName; // First and Last name of account
    private Date devicePairedToOemAccountDate; // What day was this device first paired with this oemAccount?
    private String deviceTimeZone; // Time Zone Abbreviation. Example: PDT, MST
    private Integer deviceTimeZoneSetBy; // 1 - Time Zone Set by Network; 2 - Time Zone Set by User; 3 - Time Zone set by Device Location
    private String deviceIMEI; // Only needed if your payment device has a cell connection
    private String billingLine1;
    private String billingLine2;
    private String billingCity;
    private String billingState;
    private String billingZip;

    private String locale; //ISO 3166-1 alpha-2

    private IdVerification() {
    }

    /**
     * send data to RTM
     */
    public void send(String callbackId) {
        RxBus.getInstance().post(new RtmMessageResponse(callbackId, this, RtmType.ID_VERIFICATION));
    }

    public static final class Builder {
        private Date oemAccountInfoUpdatedDate;
        private Date oemAccountCreatedDate;
        private Integer suspendedCardsInOemAccount;
        private Date lastOemAccountActivityDate;
        private Date deviceLostModeDate;
        private Integer devicesWithIdenticalActiveToken;
        private Integer activeTokensOnAllDevicesForOemAccount;
        private Integer oemAccountScore;
        private Integer deviceScore;
        private Boolean nfcCapable;

        private String billingCountryCode;
        private String oemAccountCountryCode;
        private String deviceCountry;
        private String oemAccountUserName;
        private Date devicePairedToOemAccountDate;
        private String deviceTimeZone;
        @DeviceTimeZone.SetBy
        private Integer deviceTimeZoneSetBy;
        private String deviceIMEI;
        private String billingLine1;
        private String billingLine2;
        private String billingCity;
        private String billingState;
        private String billingZip;

        private final String locale;

        public Builder() {
            locale = Locale.getDefault().getLanguage() + '-' + Locale.getDefault().getCountry();
        }

        /**
         * Most recent date this user update their: Billing Address, Name, Email, password, or other Personally Identifiable Information associated to their account.
         *
         * @param oemAccountInfoUpdatedDate time in ISO 8601 format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
         * @return this
         */
        public Builder setOemAccountInfoUpdatedDate(Date oemAccountInfoUpdatedDate) {
            this.oemAccountInfoUpdatedDate = oemAccountInfoUpdatedDate;
            return this;
        }

        /**
         * OEM account created date
         *
         * @param oemAccountCreatedDate time in ISO 8601 format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
         * @return this
         */
        public Builder setOemAccountCreatedDate(Date oemAccountCreatedDate) {
            this.oemAccountCreatedDate = oemAccountCreatedDate;
            return this;
        }

        /**
         * If this user has multiple devices, how many cards are suspended in total across all devices
         *
         * @param suspendedCardsInOemAccount
         * @return this
         */
        public Builder setSuspendedCardsInOemAccount(int suspendedCardsInOemAccount) {
            this.suspendedCardsInOemAccount = suspendedCardsInOemAccount;
            return this;
        }

        /**
         * Date this account was previously used, never today.
         *
         * @param lastOemAccountActivityDate time in ISO 8601 format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
         * @return this
         */
        public Builder setLastOemAccountActivityDate(Date lastOemAccountActivityDate) {
            this.lastOemAccountActivityDate = lastOemAccountActivityDate;
            return this;
        }

        /**
         * Date this device was reported lost or stolen. Don't send if you don't have it.
         *
         * @param deviceLostModeDate time in ISO 8601 format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
         * @return this
         */
        public Builder setDeviceLostModeDate(Date deviceLostModeDate) {
            this.deviceLostModeDate = deviceLostModeDate;
            return this;
        }

        /**
         * Set account score.
         *
         * @param oemAccountScore Between 0-9
         * @return this
         */
        public Builder setOemAccountScore(int oemAccountScore) {
            if (oemAccountScore < 0 && oemAccountScore > 9) {
                throw new IllegalArgumentException("oemAccountScore should be between 0-9");
            }
            this.oemAccountScore = oemAccountScore;
            return this;
        }

        /**
         * Device with identical active token
         *
         * @param devicesWithIdenticalActiveToken
         * @return
         */
        public Builder setDevicesWithIdenticalActiveToken(Integer devicesWithIdenticalActiveToken) {
            this.devicesWithIdenticalActiveToken = devicesWithIdenticalActiveToken;
            return this;
        }

        /**
         * If this user has multiple devices, how many cards are active in total across all devices?
         *
         * @param activeTokensOnAllDevicesForOemAccount
         * @return this
         */
        public Builder setActiveTokensOnAllDevicesForOemAccount(Integer activeTokensOnAllDevicesForOemAccount) {
            this.activeTokensOnAllDevicesForOemAccount = activeTokensOnAllDevicesForOemAccount;
            return this;
        }

        /**
         * Set device score.
         *
         * @param deviceScore Between 0-9
         * @return this
         */
        public Builder setDeviceScore(int deviceScore) {
            if (deviceScore < 0 && deviceScore > 9) {
                throw new IllegalArgumentException("deviceScore should be between 0-9");
            }
            this.deviceScore = deviceScore;
            return this;
        }

        /**
         * Only needed if your device is NOT nfcCapable
         *
         * @param nfcCapable
         * @return this
         */
        public Builder setNfcCapable(boolean nfcCapable) {
            this.nfcCapable = nfcCapable;
            return this;
        }

        /**
         * Country of user's billing address in ISO 3166-1 alpha-2 format, e.g., US; maximum 2 characters
         *
         * @param billingCountryCode
         * @return this
         */
        public Builder setBillingCountryCode(String billingCountryCode) {
            if (StringUtils.isEmpty(billingCountryCode) && billingCountryCode.length() > 2) {
                throw new IllegalArgumentException("billingCountryCode maximum 2 characters");
            }
            this.billingCountryCode = billingCountryCode;
            return this;
        }

        /**
         * Country setting of account or phone in ISO 3166-1 alpha-2 format
         *
         * @param oemAccountCountryCode
         * @return this
         */
        public Builder setOemAccountCountryCode(String oemAccountCountryCode) {
            this.oemAccountCountryCode = oemAccountCountryCode;
            return this;
        }

        /**
         * Country setting of payment device
         *
         * @param deviceCountry
         * @return this
         */
        public Builder setDeviceCountry(String deviceCountry) {
            this.deviceCountry = deviceCountry;
            return this;
        }

        /**
         * First and Last name of account
         *
         * @param oemAccountUserName
         * @return this
         */
        public Builder setOemAccountUserName(String oemAccountUserName) {
            this.oemAccountUserName = oemAccountUserName;
            return this;
        }

        /**
         * What day was this device first paired with this oemAccount
         *
         * @param devicePairedToOemAccountDate time in ISO 8601 format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
         * @return this
         */
        public Builder setDevicePairedToOemAccountDate(Date devicePairedToOemAccountDate) {
            this.devicePairedToOemAccountDate = devicePairedToOemAccountDate;
            return this;
        }

        /**
         * Time Zone Abbreviation. Example: PDT, MST
         *
         * @param deviceTimeZone
         * @return this
         */
        public Builder setDeviceTimeZone(String deviceTimeZone) {
            this.deviceTimeZone = deviceTimeZone;
            return this;
        }

        /**
         * 1 - Time Zone Set by Network; 2 - Time Zone Set by User; 3 - Time Zone set by Device Location
         *
         * @param deviceTimeZoneSetBy
         * @return this
         */
        public Builder setDeviceTimeZoneSetBy(@DeviceTimeZone.SetBy Integer deviceTimeZoneSetBy) {
            this.deviceTimeZoneSetBy = deviceTimeZoneSetBy;
            return this;
        }

        /**
         * Only needed if your payment device has a cell connection
         *
         * @param deviceIMEI
         * @return this
         */
        public Builder setDeviceIMEI(String deviceIMEI) {
            this.deviceIMEI = deviceIMEI;
            return this;
        }

        /**
         * Billing line 1
         *
         * @param billingLine1
         * @return this
         */
        public Builder setBillingLine1(String billingLine1) {
            this.billingLine1 = billingLine1;
            return this;
        }

        /**
         * Billing line 2
         *
         * @param billingLine2
         * @return this
         */
        public Builder setBillingLine2(String billingLine2) {
            this.billingLine2 = billingLine2;
            return this;
        }

        /**
         * Billing city
         *
         * @param billingCity
         * @return this
         */
        public Builder setBillingCity(String billingCity) {
            this.billingCity = billingCity;
            return this;
        }

        /**
         * Billing state
         *
         * @param billingState
         * @return this
         */
        public Builder setBillingState(String billingState) {
            this.billingState = billingState;
            return this;
        }

        /**
         * Billing zip
         *
         * @param billingZip
         * @return this
         */
        public Builder setBillingZip(String billingZip) {
            this.billingZip = billingZip;
            return this;
        }

        public IdVerification build() {
            IdVerification idVerification = new IdVerification();

            idVerification.oemAccountInfoUpdatedDate = oemAccountInfoUpdatedDate;
            idVerification.oemAccountCreatedDate = oemAccountCreatedDate;
            idVerification.suspendedCardsInOemAccount = suspendedCardsInOemAccount;
            idVerification.lastOemAccountActivityDate = lastOemAccountActivityDate;
            idVerification.deviceLostModeDate = deviceLostModeDate;
            idVerification.devicesWithIdenticalActiveToken = devicesWithIdenticalActiveToken;
            idVerification.activeTokensOnAllDevicesForOemAccount = activeTokensOnAllDevicesForOemAccount;
            idVerification.oemAccountScore = oemAccountScore;
            idVerification.deviceScore = deviceScore;
            idVerification.nfcCapable = nfcCapable;

            idVerification.billingCountryCode = billingCountryCode;
            idVerification.oemAccountCountryCode = oemAccountCountryCode;
            idVerification.deviceCountry = deviceCountry;
            idVerification.oemAccountUserName = oemAccountUserName;
            idVerification.devicePairedToOemAccountDate = devicePairedToOemAccountDate;
            idVerification.deviceTimeZone = deviceTimeZone;
            idVerification.deviceTimeZoneSetBy = deviceTimeZoneSetBy;
            idVerification.deviceIMEI = deviceIMEI;
            idVerification.billingLine1 = billingLine1;
            idVerification.billingLine2 = billingLine2;
            idVerification.billingCity = billingCity;
            idVerification.billingState = billingState;
            idVerification.billingZip = billingZip;

            idVerification.locale = locale;

            return idVerification;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.oemAccountInfoUpdatedDate != null ? this.oemAccountInfoUpdatedDate.getTime() : -1);
        dest.writeLong(this.oemAccountCreatedDate != null ? this.oemAccountCreatedDate.getTime() : -1);
        dest.writeValue(this.suspendedCardsInOemAccount);
        dest.writeLong(this.lastOemAccountActivityDate != null ? this.lastOemAccountActivityDate.getTime() : -1);
        dest.writeLong(this.deviceLostModeDate != null ? this.deviceLostModeDate.getTime() : -1);
        dest.writeValue(this.devicesWithIdenticalActiveToken);
        dest.writeValue(this.activeTokensOnAllDevicesForOemAccount);
        dest.writeValue(this.oemAccountScore);
        dest.writeValue(this.deviceScore);
        dest.writeValue(this.nfcCapable);
        dest.writeString(this.billingCountryCode);
        dest.writeString(this.oemAccountCountryCode);
        dest.writeString(this.deviceCountry);
        dest.writeString(this.oemAccountUserName);
        dest.writeLong(this.devicePairedToOemAccountDate != null ? this.devicePairedToOemAccountDate.getTime() : -1);
        dest.writeString(this.deviceTimeZone);
        dest.writeValue(this.deviceTimeZoneSetBy);
        dest.writeString(this.deviceIMEI);
        dest.writeString(this.billingLine1);
        dest.writeString(this.billingLine2);
        dest.writeString(this.billingCity);
        dest.writeString(this.billingState);
        dest.writeString(this.billingZip);
        dest.writeString(this.locale);
    }

    protected IdVerification(Parcel in) {
        long tmpOemAccountInfoUpdatedDate = in.readLong();
        this.oemAccountInfoUpdatedDate = tmpOemAccountInfoUpdatedDate == -1 ? null : new Date(tmpOemAccountInfoUpdatedDate);
        long tmpOemAccountCreatedDate = in.readLong();
        this.oemAccountCreatedDate = tmpOemAccountCreatedDate == -1 ? null : new Date(tmpOemAccountCreatedDate);
        this.suspendedCardsInOemAccount = (Integer) in.readValue(Integer.class.getClassLoader());
        long tmpLastOemAccountActivityDate = in.readLong();
        this.lastOemAccountActivityDate = tmpLastOemAccountActivityDate == -1 ? null : new Date(tmpLastOemAccountActivityDate);
        long tmpDeviceLostModeDate = in.readLong();
        this.deviceLostModeDate = tmpDeviceLostModeDate == -1 ? null : new Date(tmpDeviceLostModeDate);
        this.devicesWithIdenticalActiveToken = (Integer) in.readValue(Integer.class.getClassLoader());
        this.activeTokensOnAllDevicesForOemAccount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.oemAccountScore = (Integer) in.readValue(Integer.class.getClassLoader());
        this.deviceScore = (Integer) in.readValue(Integer.class.getClassLoader());
        this.nfcCapable = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.billingCountryCode = in.readString();
        this.oemAccountCountryCode = in.readString();
        this.deviceCountry = in.readString();
        this.oemAccountUserName = in.readString();
        long tmpDevicePairedToOemAccountDate = in.readLong();
        this.devicePairedToOemAccountDate = tmpDevicePairedToOemAccountDate == -1 ? null : new Date(tmpDevicePairedToOemAccountDate);
        this.deviceTimeZone = in.readString();
        this.deviceTimeZoneSetBy = (Integer) in.readValue(Integer.class.getClassLoader());
        this.deviceIMEI = in.readString();
        this.billingLine1 = in.readString();
        this.billingLine2 = in.readString();
        this.billingCity = in.readString();
        this.billingState = in.readString();
        this.billingZip = in.readString();
        this.locale = in.readString();
    }

    public static final Parcelable.Creator<IdVerification> CREATOR = new Parcelable.Creator<IdVerification>() {
        @Override
        public IdVerification createFromParcel(Parcel source) {
            return new IdVerification(source);
        }

        @Override
        public IdVerification[] newArray(int size) {
            return new IdVerification[size];
        }
    };
}
