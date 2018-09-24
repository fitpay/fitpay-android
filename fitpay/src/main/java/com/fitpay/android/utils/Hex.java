package com.fitpay.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

/**
 * Created by ssteveli on 1/25/16.
 */
public class Hex {
    private final static byte[] encodingTable = {
            (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    private final static byte[] decodingTable = new byte[128];

    static {
        for (int i = 0; i < encodingTable.length; i++) {
            decodingTable[encodingTable[i]] = (byte) i;
        }

        decodingTable['A'] = decodingTable['a'];
        decodingTable['B'] = decodingTable['b'];
        decodingTable['C'] = decodingTable['c'];
        decodingTable['D'] = decodingTable['d'];
        decodingTable['E'] = decodingTable['e'];
        decodingTable['F'] = decodingTable['f'];
    }

    public static String bytesToHexString(byte[] bytes) {
        return bytesToHexString(bytes, 0, bytes.length);
    }

    public static String bytesToHexString(byte[] bytes, int off, int length) {
        StringWriter out = new StringWriter();
        for (int i = off; i < length; i++) {
            int v = bytes[i] & 0xff;
            out.write(encodingTable[(v >>> 4)]);
            out.write(encodingTable[v & 0xf]);
        }

        return out.toString();
    }

    public static byte[] hexStringToBytes(String s) {
        if (s == null || s.length() == 0) {
            return new byte[0]; //throw new IllegalArgumentException("invalid hex string, it's empty");
        }

        if ((s.length() % 2) != 0) {
            throw new IllegalArgumentException("invalid hex string, length of " + s.length() + " is not an even number");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte b1, b2;
        int end = s.length();

        while (end > 0) {
            if (!ignore(s.charAt(end - 1))) {
                break;
            }
            end--;
        }

        int i = 0;
        while (i < end) {
            while (i < end && ignore(s.charAt(i))) {
                i++;
            }

            try {
                b1 = decodingTable[s.charAt(i++)];
            } catch (StringIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("invalid hex string (" + s + "), invalid character at position: " + i);
            }

            while (i < end && ignore(s.charAt(i))) {
                i++;
            }

            try {
                b2 = decodingTable[s.charAt(i++)];
            } catch (StringIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("invalid hex string (" + s + "), invalid character at position: " + i);
            }

            out.write((b1 << 4) | b2);
        }

        return out.toByteArray();
    }

    public static byte[] sequenceToBytes(int sequenceId) {
        String val = Integer.toHexString(sequenceId);
        if ((val.length() & 1) != 0) {
            val = "0" + val;
        }

        return new byte[]{Hex.hexStringToBytes(val)[0], 0x00};
    }

    private static boolean ignore(char c) {
        return (c == '\n' || c == '\r' || c == '\t' || c == ' ');
    }

}
