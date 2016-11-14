package com.fitpay.android.api.models.device;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.DeviceTypes;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.Links;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.CreditCardRef;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.DeviceOperationException;
import com.fitpay.android.utils.TimestampUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

/**
 * Payment Device Information
 */
public final class Device extends DeviceModel implements Parcelable {

    private static final String COMMITS = "commits";
    private static final String USER = "user";

    private List<CreditCardRef> cardRelationships;

    public List<CreditCardRef> getCardRelationships() {
        return cardRelationships;
    }

    /**
     * Get current user
     *
     * @param callback result callback
     */
    public void getUser(@NonNull ApiCallback<User> callback) {
        makeGetCall(USER, null, User.class, callback);
    }

    public boolean canGetUser() {
        return hasLink(USER);
    }

    /**
     * Update the details of an existing device.
     *
     * @param callback result callback
     */
    public void updateDevice(@NonNull Device device, @NonNull ApiCallback<Device> callback) {
        makePatchCall(device, false, Device.class, callback);
    }

    /**
     * Add values to existing device.
     *
     * @param callback result callback
     */
    public void updateToken(@NonNull Device device, boolean add, @NonNull ApiCallback<Device> callback) {
        makePatchCall(device, add, false, Device.class, callback);
    }

    public boolean canUpdate() {
        return hasLink(SELF);
    }

    /**
     * Delete a single device.
     *
     * @param callback result callback
     */
    public void deleteDevice(@NonNull ApiCallback<Void> callback) {
        makeDeleteCall(callback);
    }

    public boolean canDelete() {
        return hasLink(SELF);
    }

    /**
     * Retrieves a collection of events that should be committed to this device.
     *
     * @param limit        Max number of events per page, default: 10
     * @param offset       Start index position for list of entities returned
     * @param lastCommitId last commit id
     * @param callback     result callback
     */
    public void getCommits(int limit, int offset, String lastCommitId, final ApiCallback<Collections.CommitsCollection> callback) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("limit", limit);
        queryMap.put("offset", offset);
        if (lastCommitId != null) {
            queryMap.put("commitsAfter", lastCommitId);
        }
        makeGetCall(COMMITS, queryMap, Collections.CommitsCollection.class, callback);
    }

    public boolean canGetCommits() {
        return hasLink(COMMITS);
    }


    /**
     * Retrieves a collection of events that should be committed to this device.
     *
     * @param limit    Max number of events per page, default: 10
     * @param offset   Start index position for list of entities returned
     * @param callback result callback
     */
    public void getCommits(int limit, int offset, final ApiCallback<Collections.CommitsCollection> callback) {
        this.getCommits(limit, offset, null, callback);
    }

    /**
     * Retrieves a collection of events that should be committed to this device.
     * Limit: 10
     * Offset: 0
     *
     * @param callback result callback
     */
    public void getCommits(final ApiCallback<Collections.CommitsCollection> callback) {
        this.getCommits(10, 0, null, callback);
    }

    /**
     * Retrieves a collection of events that should be committed to this device.
     * Limit: 10
     * Offset: 0
     *
     * @param lastCommitId last commit id
     * @param callback     result callback
     */
    public void getCommits(String lastCommitId, final ApiCallback<Collections.CommitsCollection> callback) {
        this.getCommits(10, 0, lastCommitId, callback);
    }

    /**
     * Retrieves 'all' events that should be committed to this device.
     * All is really limited to 100.
     *
     * @param lastCommitId last commit id
     * @param callback     result callback
     */
    public void getAllCommits(String lastCommitId, final ApiCallback<Collections.CommitsCollection> callback) {
        final int limit = 50;
        final Collections.CommitsCollection tempCommitsStorage = new Collections.CommitsCollection();
        getCommits(limit, 0, lastCommitId, new ApiCallback<Collections.CommitsCollection>() {
            @Override
            public void onSuccess(Collections.CommitsCollection result) {
                tempCommitsStorage.addCollection(result);

                if (result.hasNext()) {
                    String lastCommitId = result.getResults().get(result.getResults().size() - 1).commitId;
                    getCommits(limit, 0, lastCommitId, this);
                } else {
                    callback.onSuccess(tempCommitsStorage);
                }
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                if (tempCommitsStorage.getResults() != null) {
                    tempCommitsStorage.getResults().clear();
                }
                callback.onFailure(errorCode, errorMessage);
            }
        });
    }


    /**
     * Retrieves 'all' events that should be committed to this device.
     *
     * @param lastCommitId last commit id
     * @return observable
     */
    public Observable<Collections.CommitsCollection> getAllCommits(final String lastCommitId) {
        return Observable.create(new Observable.OnSubscribe<Collections.CommitsCollection>() {
            @Override
            public void call(Subscriber<? super Collections.CommitsCollection> subscriber) {
                getAllCommits(lastCommitId, new ApiCallback<Collections.CommitsCollection>() {
                    @Override
                    public void onSuccess(Collections.CommitsCollection result) {
                        if (result == null) {
                            subscriber.onError(new Exception("commits result is null"));
                            return;
                        }

                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        subscriber.onError(new DeviceOperationException(errorMessage, errorCode));
                    }
                });
            }
        });
    }

    public static final class Builder {
        private String deviceIdentifier;
        @DeviceTypes.Type
        private String deviceType;
        private String manufacturerName;
        private String deviceName;
        private String serialNumber;
        private String modelNumber;
        private String hardwareRevision;
        private String firmwareRevision;
        private String softwareRevision;
        private String systemId;
        private String osName;
        private String licenseKey;
        private String bdAddress;
        private String secureElementId;
        private String casd;
        private String pairingTs;
        private String notificationToken;

        /**
         * Creates a Builder instance that can be used to build Gson with various configuration
         * settings. Builder follows the builder pattern, and it is typically used by first
         * invoking various configuration methods to set desired options, and finally calling
         * {@link #build()}.
         */
        public Builder() {
        }

        /**
         * Creates a {@link Device} instance based on the current configuration. This method is free of
         * side-effects to this {@code Builder} instance and hence can be called multiple times.
         *
         * @return an instance of {@link CreditCard} configured with the options currently set in this builder
         */
        public Device build() {
            Device device = new Device();
            device.deviceIdentifier = deviceIdentifier;
            device.deviceType = deviceType;
            device.manufacturerName = manufacturerName;
            device.deviceName = deviceName;
            device.serialNumber = serialNumber;
            device.modelNumber = modelNumber;
            device.hardwareRevision = hardwareRevision;
            device.firmwareRevision = firmwareRevision;
            device.softwareRevision = softwareRevision;
            device.systemId = systemId;
            device.osName = osName;
            device.licenseKey = licenseKey;
            device.bdAddress = bdAddress;
            device.secureElement = new SecureElement(secureElementId);
            device.pairingTs = pairingTs;
            device.notificationToken = notificationToken;
            device.casd = casd;
            return device;
        }

        /**
         * Set device identifier
         *
         * @param deviceIdentifier The device identifier parameter is used to read or write the identifier key of the device
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setDeviceIdentifier(@NonNull String deviceIdentifier) {
            this.deviceIdentifier = deviceIdentifier;
            return this;
        }

        /**
         * Set device name
         *
         * @param deviceName The name of the device model.
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setDeviceName(@NonNull String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        /**
         * Set device type
         *
         * @param deviceType The type of device (PHONE, TABLET, ACTIVITY_TRACKER, SMARTWATCH, PC, CARD_EMULATOR, CLOTHING, JEWELRY, OTHER
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setDeviceType(@DeviceTypes.Type String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        /**
         * Set manufacture name
         *
         * @param manufacturerName The manufacturer name of the device.
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setManufacturerName(String manufacturerName) {
            this.manufacturerName = manufacturerName;
            return this;
        }

        /**
         * Set serial number
         *
         * @param serialNumber The serial number for a particular instance of the device
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        /**
         * Set model number
         *
         * @param modelNumber The model number that is assigned by the device vendor.
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setModelNumber(String modelNumber) {
            this.modelNumber = modelNumber;
            return this;
        }

        /**
         * Set hardware revision
         *
         * @param hardwareRevision The hardware revision for the hardware within the device.
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setHardwareRevision(String hardwareRevision) {
            this.hardwareRevision = hardwareRevision;
            return this;
        }

        /**
         * Set firmware revision
         *
         * @param firmwareRevision The firmware revision for the hardware within the device.
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setFirmwareRevision(String firmwareRevision) {
            this.firmwareRevision = firmwareRevision;
            return this;
        }

        /**
         * Set software revision
         *
         * @param softwareRevision The software revision for the hardware within the device.
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setSoftwareRevision(String softwareRevision) {
            this.softwareRevision = softwareRevision;
            return this;
        }

        /**
         * Set system ID
         *
         * @param systemId A structure containing an Organizationally Unique Identifier (OUI)
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setSystemId(String systemId) {
            this.systemId = systemId;
            return this;
        }

        /**
         * Set OS name
         *
         * @param osName The name of the operating system
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setOSName(String osName) {
            this.osName = osName;
            return this;
        }

        /**
         * Set license key
         *
         * @param licenseKey The license key parameter is used to read or write the license key of the device
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setLicenseKey(String licenseKey) {
            this.licenseKey = licenseKey;
            return this;
        }

        /**
         * Set bluetooth device address
         *
         * @param bdAddress The BD address parameter is used to read the Bluetooth device address
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setBdAddress(String bdAddress) {
            this.bdAddress = bdAddress;
            return this;
        }

        /**
         * Set secure element id
         *
         * @param secureElementId The ID of a secure element in a payment capable device
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setSecureElementId(String secureElementId) {
            this.secureElementId = secureElementId;
            return this;
        }

        /**
         * Set pairing time
         *
         * @param pairingTs The time the device was paired
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setPairingTs(long pairingTs) {
            this.pairingTs = TimestampUtils.getISO8601StringForTime(pairingTs);
            return this;
        }

        /**
         * Set notification token use {@link #setNotificationToken(String)}
         */
        @Deprecated
        public Builder setNotificaitonToken(String notificationToken) {
            this.notificationToken = notificationToken;
            return this;
        }

        /**
         * Set notification token
         *
         * @param notificationToken The hardware revision for the hardware within the device.
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setNotificationToken(String notificationToken) {
            this.notificationToken = notificationToken;
            return this;
        }

        /**
         * Set CASD
         *
         * @param casd
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setCASD(String casd) {
            this.casd = casd;
            return this;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceIdentifier);
        dest.writeString(this.serialNumber);
        dest.writeString(this.modelNumber);
        dest.writeString(this.hardwareRevision);
        dest.writeString(this.firmwareRevision);
        dest.writeString(this.softwareRevision);
        dest.writeValue(this.createdTsEpoch);
        dest.writeString(this.osName);
        dest.writeString(this.systemId);
        dest.writeString(this.licenseKey);
        dest.writeString(this.bdAddress);
        dest.writeString(this.pairingTs);
        dest.writeString(this.hostDeviceId);
        dest.writeList(this.cardRelationships);
        dest.writeParcelable(this.links, flags);
        dest.writeString(this.notificationToken);
        dest.writeString(this.casd);
    }

    public Device() {
    }

    protected Device(Parcel in) {
        this.deviceIdentifier = in.readString();
        this.serialNumber = in.readString();
        this.modelNumber = in.readString();
        this.hardwareRevision = in.readString();
        this.firmwareRevision = in.readString();
        this.softwareRevision = in.readString();
        this.createdTsEpoch = (Long) in.readValue(Long.class.getClassLoader());
        this.osName = in.readString();
        this.systemId = in.readString();
        this.licenseKey = in.readString();
        this.bdAddress = in.readString();
        this.pairingTs = in.readString();
        this.hostDeviceId = in.readString();
        this.cardRelationships = new ArrayList<>();
        in.readList(this.cardRelationships, CreditCardRef.class.getClassLoader());
        this.links = in.readParcelable(Links.class.getClassLoader());
        this.notificationToken = in.readString();
        this.casd = in.readString();
    }

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel source) {
            return new Device(source);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}