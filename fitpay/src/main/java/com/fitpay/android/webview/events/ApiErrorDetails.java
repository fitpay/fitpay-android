package com.fitpay.android.webview.events;

/**
 * API error detailed message
 */
public class ApiErrorDetails {
    private int code;
    private String detailedMessage;
    private String fullMessage;

    public int getCode() {
        return code;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public String getFullMessage() {
        return fullMessage;
    }
}
