package com.fitpay.android.wearable.ble.message;

import com.fitpay.android.wearable.interfaces.IControlMessage;

/**
 * Created by tgs on 3/4/16.
 */
public class ApplicationControlMessage extends BleMessage implements IControlMessage {

    private byte[] data;

    public ApplicationControlMessage withData(byte[] value) {
        this.data = value;
        return this;
    }

    public byte[] getMessage() {
        return data;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
