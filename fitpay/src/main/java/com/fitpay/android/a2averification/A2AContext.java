package com.fitpay.android.a2averification;

/**
 * App-to-App context data
 */
public final class A2AContext {

    private String applicationId;
    private String action;
    private String payload;

    /**
     * iTunes App Id
     * 10 digit number as a string
     * Can be used to construct an iTunes URL as a fallback if the user doesn't have the issuer app installed
     *
     * @return applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * The url used to be open the issuer app
     *
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * The payload to send the issuer as a parameter
     * ?a2apayload={payload}
     *
     * @return payload
     */
    public String getPayload() {
        return payload;
    }
}
