package com.fitpay.android.paymentdevice.impl.ble.message;

import com.fitpay.android.paymentdevice.interfaces.ISecureMessage;

/**
 * Created by tgs on 3/4/16.
 */
public class SecurityStateMessage extends BleMessage implements ISecureMessage {

    private boolean nfcEnabled;
    private byte nfcErrorCode = 0x00;

    public SecurityStateMessage() {
    }

    public SecurityStateMessage withNfcEnabled(boolean enabled) {
        this.nfcEnabled = enabled;
        return this;
    }

    public SecurityStateMessage withNfcErrorCode(byte nfcErrorCode) {
        this.nfcErrorCode = nfcErrorCode;
        return this;
    }

    public SecurityStateMessage withData(byte[] bytes) {
        if (bytes.length == 0) {
            this.nfcEnabled = false;
        } else {
            this.nfcEnabled = bytes[0] == 0x01;
            if (bytes.length > 1) {
                nfcErrorCode = bytes[1];
            }
        }
        return this;
    }

    public byte[] getMessage() {
        byte[] message = new byte[2];
        if (nfcEnabled) {
            message[0] = 0x01;
        } else {
            message[0] = 0x00;
        }
        message[1] = nfcErrorCode;
        return message;
    }

    public boolean isNfcEnabled() {
        return nfcEnabled;
    }

    public byte getNfcErrorCode() {
        return nfcErrorCode;
    }
}
