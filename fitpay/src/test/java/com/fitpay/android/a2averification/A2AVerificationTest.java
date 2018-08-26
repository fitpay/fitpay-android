package com.fitpay.android.a2averification;

import com.fitpay.android.utils.Constants;

import org.junit.Assert;
import org.junit.Test;

public class A2AVerificationTest {

    @Test
    public void a2aVerificationRequest() {
        String a2aVerificationRequest = "{\"cardType\":\"VISA\",\"returnLocation\":\"\\/idv\\/b3f70e43-c066-4e9d-b5ec-b7237302f9cc\\/select\\/3b42e65f-1608-4afd-bd72-646142b00a6e\",\"context\":{\"applicationId\":\"com.fitpay.issuerdemo\",\"action\":\"generate_auth_code\",\"payload\":\"eyJ1c2VySWQiOiIxNTdmMTUxOC1kYzRjLTRhYWMtYWRmNS03NjRkMDE2MTJjNGEiLCJ0b2tlbml6YXRpb25JZCI6ImIzZjcwZTQzLWMwNjYtNGU5ZC1iNWVjLWI3MjM3MzAyZjljYyIsInZlcmlmaWNhdGlvbklkIjoiM2I0MmU2NWYtMTYwOC00YWZkLWJkNzItNjQ2MTQyYjAwYTZlIn0=\"}}";
        A2AVerificationRequest request = Constants.getGson().fromJson(a2aVerificationRequest, A2AVerificationRequest.class);

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
