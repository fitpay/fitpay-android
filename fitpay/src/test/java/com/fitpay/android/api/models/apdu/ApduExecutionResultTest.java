package com.fitpay.android.api.models.apdu;

import com.fitpay.android.CommitTest;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.utils.Hex;
import com.fitpay.android.utils.HttpLogging;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by tgs on 5/27/16.
 */
public class ApduExecutionResultTest {

    @Before
    public void init(){
        HttpLogging.setTestName(ApduExecutionResultTest.class.getSimpleName());
    }

    @Test
    public void testIsSuccessResponseCode9000() {
        ApduExecutionResult result = new ApduExecutionResult(UUID.randomUUID().toString());
        ApduCommandResult cmdResult = new ApduCommandResult.Builder().setResponseCode("9000").build();
        assertTrue("9000 should be success", result.isSuccessResponseCode(cmdResult));
    }

    @Test
    public void testIsSuccessResponseCode2() {
        ApduExecutionResult result = new ApduExecutionResult(UUID.randomUUID().toString());
        result.addResponse(new ApduCommandResult.Builder().setResponseCode("9000").build());
        result.addResponse(new ApduCommandResult.Builder().setResponseCode("9000").build());
        assertEquals("state for response with 2 '9000's", ResponseState.PROCESSED, result.getState());
        result.deriveState();
        assertEquals("derived state for response with 2 '9000's", ResponseState.PROCESSED, result.getState());
    }

    @Test
    public void testIsSuccessResponseCodeNegative() {
        ApduExecutionResult result = new ApduExecutionResult(UUID.randomUUID().toString());
        result.addResponse(new ApduCommandResult.Builder().setResponseCode("9000").build());
        result.addResponse(new ApduCommandResult.Builder().setResponseCode("0000").build());
        assertEquals("state for response with '9000' and '0000'", ResponseState.FAILED, result.getState());
        result.deriveState();
        assertEquals("derived state for response with '9000' and '0000'", ResponseState.FAILED, result.getState());
    }

    @Test
    public void testIsSuccessResponseCodeNoResponses() {
        ApduExecutionResult result = new ApduExecutionResult(UUID.randomUUID().toString());
        result.deriveState();
        assertEquals("derived state for response with no responses", ResponseState.ERROR, result.getState());
    }

    @Test
    public void testIsSuccessResponseCode3() {
        ApduExecutionResult result = new ApduExecutionResult(UUID.randomUUID().toString());
        result.addResponse(new ApduCommandResult.Builder().setResponseCode(Hex.bytesToHexString( new byte[] {(byte) 0x61, (byte) 0x00})).build());
        result.addResponse(new ApduCommandResult.Builder().setResponseCode("9000").build());
        assertEquals("state for response with 0x6100 and 9000", ResponseState.PROCESSED, result.getState());
        result.deriveState();
        assertEquals("derived state for response with 0x6100 and 9000", ResponseState.PROCESSED, result.getState());
    }



}
