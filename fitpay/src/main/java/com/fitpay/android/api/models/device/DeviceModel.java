package com.fitpay.android.api.models.device;

import android.support.annotation.Nullable;

/**
 * Device model
 */
abstract class DeviceModel extends PaymentDevice {

    protected String deviceIdentifier;

    /**
     * description : The serial number for a particular instance of the device
     */
    protected String serialNumber;

    /**
     * description : The model number that is assigned by the device vendor.
     */
    protected String modelNumber;

    /**
     * description : The hardware revision for the hardware within the device.
     */
    protected String hardwareRevision;

    /**
     * description : The firmware revision for the firmware within the device.
     */
    protected String firmwareRevision;

    /**
     * description : The software revision for the software within the device.
     */
    protected String softwareRevision;

    protected Long createdTsEpoch;

    /**
     * description : The name of the operating system
     */
    protected String osName;

    /**
     * description : A structure containing an Organizationally Unique Identifier (OUI)
     * followed by a manufacturer-defined identifier and is unique for each individual instance of the product.
     */
    protected String systemId;

    /**
     * description : The license key parameter is used to read or write the license key of the device
     */
    protected String licenseKey;

    /**
     * description : The BD address parameter is used to read the Bluetooth device address
     */
    protected String bdAddress;

    /**
     * description : The time the device was paired
     */
    protected String pairingTs;


    protected String hostDeviceId;

    /**
     * notification token. (Optional)
     */
    protected String notificationToken;

    /**
     * Unique identifier to platform asset that contains details about the embedded secure element for the device. (Optional)
     */
    protected String profileId;

    /**
     * Will be present if makeDefault is called on a Credit Card with this deviceId
     */
    protected String defaultCreditCardId;

    /**
     * Failed initialization reason: code
     */
    protected String lastStateTransitionReasonCode;

    /**
     * Failed initialization reason: message
     */
    protected String lastStateTransitionReasonMessage;

    protected DeviceModel() {
    }

    public String getDeviceIdentifier() { return deviceIdentifier; }

    public String getSerialNumber() { return serialNumber; }

    public String getModelNumber() { return modelNumber; }

    public String getHardwareRevision() { return hardwareRevision; }

    public String getFirmwareRevision() { return firmwareRevision; }

    public String getSoftwareRevision() { return softwareRevision; }

    public long getCreatedTsEpoch() { return createdTsEpoch; }

    public String getOsName() { return osName; }

    public String getSystemId() { return systemId; }

    public String getLicenseKey() { return licenseKey; }

    public String getBdAddress() { return bdAddress; }

    public String getPairingTs() { return pairingTs; }

    public String getHostDeviceId() { return hostDeviceId; }

    public String getNotificationToken() { return notificationToken; }

    public String getProfileId() { return profileId; }

    public String getDefaultCreditCardId() { return defaultCreditCardId; }

    @Nullable
    public String getLastStateTransitionReasonCode() {
        return lastStateTransitionReasonCode;
    }

    @Nullable
    public String getLastStateTransitionReasonMessage() {
        return lastStateTransitionReasonMessage;
    }
}
