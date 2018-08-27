package com.fitpay.android.a2averification;

import com.fitpay.android.TestActions;

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
}
