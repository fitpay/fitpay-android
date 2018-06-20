package com.fitpay.android.a2averification;

/**
 * App-to-app verification request.
 */
public class A2AVerificationRequest {
    private String cardType;
    private String returnLocation;
    private A2AContext context;
    private String callbackId; //internal usage

    /**
     * String to build the correct url when returning back from issuer app
     * This should be saved locally through the process
     *
     * @return returnLocation
     */
    public String getReturnLocation() {
        return returnLocation;
    }

    /**
     * Object containing information needed to pass into the issuer app
     *
     * @return context
     */
    public A2AContext getContext() {
        return context;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getCardType() {
        return cardType;
    }


}
