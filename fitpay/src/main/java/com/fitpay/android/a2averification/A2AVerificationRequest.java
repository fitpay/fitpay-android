package com.fitpay.android.a2averification;

/**
 * App-to-app verification request.
 */
public class A2AVerificationRequest {
    private String cardType;
    private String creditCardId;
    private String verificationId;
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

    /**
     * The card type: Mastercard, Visa, Discover, Maestro, etc
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * The unique identifier for the card
     */
    public String getCreditCardId() {
        return creditCardId;
    }

    /**
     * The verification identifier associated with the app to app verification
     * method for the card
     */
    public String getVerificationId(){
        return verificationId;
    }

}
