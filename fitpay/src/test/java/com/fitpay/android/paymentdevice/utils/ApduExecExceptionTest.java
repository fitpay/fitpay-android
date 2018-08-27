package com.fitpay.android.paymentdevice.utils;

import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.models.apdu.ApduCommandResult;
import com.fitpay.android.paymentdevice.enums.ApduExecutionError;

import org.junit.Assert;
import org.junit.Test;

public class ApduExecExceptionTest {

    @Test
    public void testApduExecException() {
        ApduExecutionError error = new ApduExecutionError(ApduExecutionError.ON_TIMEOUT);
        Assert.assertEquals(ApduExecutionError.ON_TIMEOUT, error.getReason());

        ApduCommandResult apduCommandResult = new ApduCommandResult.Builder()
                .setCommandId("1a2b3c")
                .setContinueOnFailure(false)
                .setResponseCode("68")
                .setResponseData("0000")
                .build();


        ApduExecException execException = new ApduExecException(
                ResponseState.FAILED,
                "Apdu Execution Failed. Reason:" + error.getReason(),
                apduCommandResult.getCommandId(),
                apduCommandResult.getResponseCode());

        try {
            throw execException;
        } catch (ApduExecException e) {
            Assert.assertEquals(ResponseState.FAILED, e.getResponseState());
            Assert.assertEquals("68", e.getResponseCode());
            Assert.assertTrue(e.getMessage().contains("1a2b3c"));
        }
    }
}
