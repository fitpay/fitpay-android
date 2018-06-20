package com.fitpay.android.paymentdevice.impl.ble;

import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationPacketMessage;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by ssteveli on 1/26/16.
 */
class ContinuationPayload {

    private final HashMap<Integer, byte[]> data;
    private final UUID targetUuid;

    public ContinuationPayload(UUID targetUuid) {
        this.data = new HashMap<>();
        this.targetUuid = targetUuid;
    }

    public void processPacket(ContinuationPacketMessage message) {
        if (message == null) {
            return;
        }

        if (data.containsKey(message.getSortOrder())) {
            FPLog.w("received duplicate continuation packet #" + message.getSortOrder());
        }

        FPLog.d("received packet #" + message.getSortOrder() + ": [" + Hex.bytesToHexString(message.getData()) + "]");
        data.put(message.getSortOrder(), message.getData());
    }

    public byte[] getValue() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int x = 0; x < data.size(); x++) {
            if (!data.containsKey(x)) {
                throw new IllegalStateException("invalid continuation payload, missing packet #" + x);
            }

            out.write(data.get(x));
        }

        return out.toByteArray();
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }
}
