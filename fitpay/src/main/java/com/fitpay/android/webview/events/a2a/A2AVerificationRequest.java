package com.fitpay.android.webview.events.a2a;

import com.fitpay.android.webview.models.a2a.A2AContext;

/**
 * App-to-app verification request.
 */
public class A2AVerificationRequest {
    private String cardType;
    private String returnLocation;
    private A2AContext context;
    private String callbackId; //internal usage

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }


    public String getCardType() {
        return cardType;
    }

    public String getReturnLocation() {
        return returnLocation;
    }

    public A2AContext getContext() {
        return context;
    }

}
