package com.fitpay.android.webview.models.a2a;

import android.util.Base64;

import com.fitpay.android.webview.enums.A2AStepupResult;
import com.fitpay.android.webview.events.a2a.A2AVerificationRequest;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

/**
 * Issuer response data for {@link A2AVerificationRequest}
 *
 * Intent extra data keys:
 * {@value com.fitpay.android.utils.Constants#A2A_STEP_UP_AUTH_CODE}
 * {@value com.fitpay.android.utils.Constants#A2A_STEP_UP_AUTH_RESPONSE}
 */
public class A2AIssuerResponse {
    @A2AStepupResult.Response
    private String response;
    private String authCode;

    public A2AIssuerResponse(@A2AStepupResult.Response String response, String authCode) {
        this.response = response;
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getEncodedString() {
        byte[] bytesToEncode = toString().getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(bytesToEncode, Base64.URL_SAFE);
    }
}
