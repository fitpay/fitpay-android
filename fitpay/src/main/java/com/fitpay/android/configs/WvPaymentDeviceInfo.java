package com.fitpay.android.configs;

import com.fitpay.android.api.models.device.Device;

/***
 * Device info for {@link WvConfig}
 */
class WvPaymentDeviceInfo {

    private String deviceType;
    private String manufacturerName;
    private String deviceName;
    private String firmwareRevision;
    private String osName;
    private String serialNumber;
    private String modelNumber;
    private String hardwareRevision;
    private String softwareRevision;
    private String systemId;
    private String licenseKey;
    private String bdAddress;
    private String notificationToken;

    private WvPaymentDeviceInfo() {
    }

    public WvPaymentDeviceInfo(Device device) {
        deviceType = device.getDeviceType();
        manufacturerName = device.getManufacturerName();
        deviceName = device.getDeviceName();
        firmwareRevision = device.getFirmwareRevision();
        osName = device.getOsName();
        serialNumber = device.getSerialNumber();
        modelNumber = device.getModelNumber();
        hardwareRevision = device.getHardwareRevision();
        softwareRevision = device.getSoftwareRevision();
        systemId = device.getSystemId();
        licenseKey = device.getLicenseKey();
        bdAddress = device.getBdAddress();
        notificationToken = device.getNotificationToken();
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getFirmwareRevision() {
        return firmwareRevision;
    }

    public String getOsName() {
        return osName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public String getBdAddress() {
        return bdAddress;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WvPaymentDeviceInfo)) return false;

        WvPaymentDeviceInfo that = (WvPaymentDeviceInfo) o;

        if (deviceType != null ? !deviceType.equals(that.deviceType) : that.deviceType != null)
            return false;
        if (manufacturerName != null ? !manufacturerName.equals(that.manufacturerName) : that.manufacturerName != null)
            return false;
        if (deviceName != null ? !deviceName.equals(that.deviceName) : that.deviceName != null)
            return false;
        if (firmwareRevision != null ? !firmwareRevision.equals(that.firmwareRevision) : that.firmwareRevision != null)
            return false;
        if (osName != null ? !osName.equals(that.osName) : that.osName != null) return false;
        if (serialNumber != null ? !serialNumber.equals(that.serialNumber) : that.serialNumber != null)
            return false;
        if (modelNumber != null ? !modelNumber.equals(that.modelNumber) : that.modelNumber != null)
            return false;
        if (hardwareRevision != null ? !hardwareRevision.equals(that.hardwareRevision) : that.hardwareRevision != null)
            return false;
        if (softwareRevision != null ? !softwareRevision.equals(that.softwareRevision) : that.softwareRevision != null)
            return false;
        if (systemId != null ? !systemId.equals(that.systemId) : that.systemId != null)
            return false;
        if (licenseKey != null ? !licenseKey.equals(that.licenseKey) : that.licenseKey != null)
            return false;
        if (bdAddress != null ? !bdAddress.equals(that.bdAddress) : that.bdAddress != null)
            return false;
        return notificationToken != null ? notificationToken.equals(that.notificationToken) : that.notificationToken == null;
    }

    @Override
    public int hashCode() {
        int result = deviceType != null ? deviceType.hashCode() : 0;
        result = 31 * result + (manufacturerName != null ? manufacturerName.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (firmwareRevision != null ? firmwareRevision.hashCode() : 0);
        result = 31 * result + (osName != null ? osName.hashCode() : 0);
        result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
        result = 31 * result + (modelNumber != null ? modelNumber.hashCode() : 0);
        result = 31 * result + (hardwareRevision != null ? hardwareRevision.hashCode() : 0);
        result = 31 * result + (softwareRevision != null ? softwareRevision.hashCode() : 0);
        result = 31 * result + (systemId != null ? systemId.hashCode() : 0);
        result = 31 * result + (licenseKey != null ? licenseKey.hashCode() : 0);
        result = 31 * result + (bdAddress != null ? bdAddress.hashCode() : 0);
        result = 31 * result + (notificationToken != null ? notificationToken.hashCode() : 0);
        return result;
    }
}
