package com.fitpay.android.a2averification;

import com.fitpay.android.TestActions;
import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessageResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class A2AVerificationTest extends TestActions {

    @Override
    @Before
    public void before(){}

    @Test
    public void a2aVerificationRequest() {
        A2AVerificationRequest request = getA2AVerificationRequest();
        Assert.assertNotNull(request);
        Assert.assertNotNull(request.getContext());
        Assert.assertEquals("VISA", request.getCardType());
        Assert.assertEquals("com.fitpay.issuerdemo", request.getContext().getApplicationId());
        Assert.assertEquals("generate_auth_code", request.getContext().getAction());
        Assert.assertEquals("eyJ1c2VySWQiOiIxNTdmMTUxOC1kYzRjLTRhYWMtYWRmNS03NjRkMDE2MTJjNGEiLCJ0b2tlbml6YXRpb25JZCI6ImIzZjcwZTQzLWMwNjYtNGU5ZC1iNWVjLWI3MjM3MzAyZjljYyIsInZlcmlmaWNhdGlvbklkIjoiM2I0MmU2NWYtMTYwOC00YWZkLWJkNzItNjQ2MTQyYjAwYTZlIn0=", request.getContext().getPayload());
    }

    @Test
    public void a2aIssuerResponse() {
        String authResponse = A2AStepupResult.APPROVED;
        String authCode = "0000";
        A2AIssuerResponse response = new A2AIssuerResponse(authResponse, authCode);

        Assert.assertEquals(response.getAuthCode(), authCode);
        Assert.assertEquals(response.getResponse(), authResponse);
    }

    @Test
    public void a2aRTMResponsesTest() {
        FitpayConfig.supportApp2App = true;
        A2AIssuerAppVerification a2AIssuerAppVerification = new A2AIssuerAppVerification();
        Assert.assertEquals("{\"supportsIssuerAppVerification\":true}", Constants.getGson().toJson(a2AIssuerAppVerification));

        A2AVerificationFailed a2AVerificationFailed = new A2AVerificationFailed(A2AVerificationError.NOT_SUPPORTED);
        Assert.assertEquals("{\"reason\":\"appToAppNotSupported\"}", Constants.getGson().toJson(a2AVerificationFailed));
    }
}
