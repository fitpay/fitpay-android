package com.fitpay.android.api.models.device;

import com.fitpay.android.api.enums.DeviceTypes;
import com.fitpay.android.api.models.BaseModel;

/**
 * Payment device
 */
public class PaymentDevice extends BaseModel {

    /**
     * The type of device (PHONE, TABLET, ACTIVITY_TRACKER, SMARTWATCH, PC, CARD_EMULATOR, CLOTHING, JEWELRY, OTHER
     */
    @DeviceTypes.Type
    protected String deviceType;

    /**
     * The manufacturer name of the device.
     */
    protected String manufacturerName;

    /**
     * The initialization state of the device.
     */
    protected String state;

    /**
     * The name of the device model.
     */
    protected String deviceName;

    /**
     * description : The ID of a secure element in a payment capable device
     */
    protected SecureElement secureElement;

    protected PaymentDevice() {
    }

    @DeviceTypes.Type
    public String getDeviceType() {
        return deviceType;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceState() {
        return state;
    }

    public String getSecureElementId() {
        return secureElement != null ? secureElement.secureElementId : null;
    }

    public String getCasd() {
        return secureElement != null ? secureElement.casd : null;
    }

    @Deprecated // see getCasd()
    public String getCASD() {
        return getCasd();
    }

    /**
     * Secure element
     */
    public static class SecureElement {
        final String secureElementId;
        final String casd;

        public SecureElement(String casd, String secureElementId) {
            this.casd = casd;
            this.secureElementId = secureElementId;
        }
    }
}
