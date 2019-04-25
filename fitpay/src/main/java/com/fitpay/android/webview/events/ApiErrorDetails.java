package com.fitpay.android.webview.events;

import androidx.annotation.Nullable;

/**
 * API error detailed message. Used for RTM messages only.
 * {@link #code}- http status code
 * {@link #detailedMessage} and {@link #fullMessage} - reason, may be null
 */
public class ApiErrorDetails {
    private int code;
    private String detailedMessage;
    private String fullMessage;

    public int getCode() {
        return code;
    }

    public @Nullable String getDetailedMessage() {
        return detailedMessage;
    }

    public @Nullable String getFullMessage() {
        return fullMessage;
    }
}
