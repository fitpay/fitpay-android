package com.fitpay.android.paymentdevice.impl.ble.message;

import android.os.ParcelUuid;

import com.fitpay.android.paymentdevice.impl.ble.BluetoothUuid;
import com.fitpay.android.utils.Hex;

import java.util.UUID;

/**
 * Created by tgs on 3/4/16.
 */
public class ContinuationControlBeginMessage extends ContinuationControlMessage {

    private UUID uuid;

    public ContinuationControlBeginMessage withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public ContinuationControlBeginMessage withUuid(String uuidValue) {
        this.uuid = UUID.fromString(uuidValue);
        return this;
    }

    public ContinuationControlBeginMessage withMessage(byte[] message) {

        if (message == null || message.length == 0) {
            return this;
        }

        if (message[0] != 0x00) {
            throw new IllegalArgumentException("message is not a valid continuation control begin message: " + Hex.bytesToHexString(message));
        }


        if (message.length != 17) {
            throw new IllegalArgumentException("message is not a valid continuation control begin message: " + Hex.bytesToHexString(message));
        }

        ParcelUuid targetUuid;
        byte[] uuidBytes = new byte[16];
        System.arraycopy(message, 1, uuidBytes, 0, 16);
        targetUuid = BluetoothUuid.parseUuidFrom(uuidBytes);
        this.uuid = targetUuid.getUuid();

        return this;
    }


    public byte[] getMessage() {
        byte[] id = getLittleEndianBytes(this.uuid);
        byte[] message = new byte[1 + id.length];
        System.arraycopy(MessageConstants.CONTINUATION_BEGIN, 0, message, 0, MessageConstants.CONTINUATION_BEGIN.length);
        System.arraycopy(id, 0, message, 1, id.length);
        return message;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder
                .append(ContinuationControlBeginMessage.class.getSimpleName())
                .append('(')
                .append("uuid: ")
                .append(this.getUuid());

        return builder.toString();
    }

}
