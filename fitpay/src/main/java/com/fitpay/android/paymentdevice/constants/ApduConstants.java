package com.fitpay.android.paymentdevice.constants;

/**
 * Apdu constants
 */
public final class ApduConstants {
    public final static byte[] NORMAL_PROCESSING = new byte[]{(byte) 0x90, (byte) 0x00};
    public final static byte[] NORMAL_PROCESSING_WITH_DATA = new byte[]{(byte) 0x61};

    public final static byte[][] SUCCESS_RESULTS = {
            NORMAL_PROCESSING,
            NORMAL_PROCESSING_WITH_DATA
    };

    public final static byte[] APDU_CONTINUE_COMMAND_DATA = new byte[]{(byte) 0x00, (byte) 0xc0, (byte) 0x00, (byte) 0x00};

    public static boolean equals(byte[] a, byte[] a2) {
        if (a == a2) {
            return true;
        }

        if (a == null || a2 == null) {
            return false;
        }

        if (a2.length > 2) {
            return false;
        }

        if (a.length == 1) {
            return a[0] == a2[0];
        }

        for (int i = 0; i < a.length; i++) {
            if (a[i] != a2[i])
                return false;
        }

        return true;
    }
}
