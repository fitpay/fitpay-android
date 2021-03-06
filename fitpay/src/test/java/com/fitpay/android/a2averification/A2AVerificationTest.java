package com.fitpay.android.a2averification;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.NamedResource;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class A2AVerificationTest extends BaseTestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(A2AVerificationTest.class);

    @Test
    public void a2aVerificationRequest() {
        A2AVerificationRequest request = getA2AVerificationRequest();
        Assert.assertNotNull(request);
        Assert.assertNotNull(request.getContext());
        Assert.assertEquals("VISA", request.getCardType());
        Assert.assertEquals("b3f70e43-c066-4e9d-b5ec-b7237302f9cc", request.getCreditCardId());
        Assert.assertEquals("3b42e65f-1608-4afd-bd72-646142b00a6e", request.getVerificationId());
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
