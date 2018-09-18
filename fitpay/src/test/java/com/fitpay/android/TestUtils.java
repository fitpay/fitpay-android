package com.fitpay.android;

import java.util.Random;

public class TestUtils {

    private static final Random random = new Random();

    private TestUtils() {
        // static methods only
    }

    public static String getRandomLengthString(int minLength, int maxLength) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        int length = minLength;
        if (maxLength > minLength) {
            length = minLength + random.nextInt(maxLength - minLength);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int pos = random.nextInt(chars.length());
            sb.append(chars.substring(pos, pos + 1));
        }
        return sb.toString();
    }

    public static String getRandomLengthNumber(int minLength, int maxLength) {
        String chars = "0123456789";
        int length = minLength;
        if (maxLength > minLength) {
            length = minLength + random.nextInt(maxLength - minLength);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int pos = random.nextInt(chars.length());
            sb.append(chars.substring(pos, pos + 1));
        }
        return sb.toString();
    }
}
