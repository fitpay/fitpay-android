package com.fitpay.android.a2averification;

/**
 * Error event if {@link A2AVerificationRequest} failed
 */
public class A2AVerificationFailed {
    @A2AVerificationError.Reason
    private String reason;

    public A2AVerificationFailed(@A2AVerificationError.Reason String reason) {
        this.reason = reason;
    }
}
