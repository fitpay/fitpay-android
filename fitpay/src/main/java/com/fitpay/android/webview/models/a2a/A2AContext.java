package com.fitpay.android.webview.models.a2a;

/**
 * App-to-App context data for {@link android.content.Intent}
 */
public final class A2AContext {
    private String applicationId;
    private String action;
    private String payload;

    public String getApplicationId() {
        return applicationId;
    }

    public String getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }
}
