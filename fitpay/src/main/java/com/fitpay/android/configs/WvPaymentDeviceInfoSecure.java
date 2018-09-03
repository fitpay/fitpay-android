package com.fitpay.android.configs;

import com.fitpay.android.api.models.device.Device;

/***
 * Extended device info for {@link WvConfig}
 */
class WvPaymentDeviceInfoSecure extends WvPaymentDeviceInfo {
    private String secureElementId;
    private String casd;

    WvPaymentDeviceInfoSecure(Device device) {
        super(device);
        secureElementId = device.getSecureElementId();
        casd = device.getCasd();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        boolean result = super.equals(o);

        if (!(o instanceof WvPaymentDeviceInfoSecure)) return result;

        WvPaymentDeviceInfoSecure that = (WvPaymentDeviceInfoSecure) o;

        if (casd != null ? !casd.equals(that.casd) : that.casd != null) {
            return false;
        }

        return secureElementId != null ? secureElementId.equals(that.secureElementId) : that.secureElementId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (secureElementId != null ? secureElementId.hashCode() : 0);
        result = 31 * result + (casd != null ? casd.hashCode() : 0);
        return result;
    }

}
