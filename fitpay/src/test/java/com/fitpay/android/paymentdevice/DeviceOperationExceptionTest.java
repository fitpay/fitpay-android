package com.fitpay.android.paymentdevice;

import com.fitpay.android.paymentdevice.enums.DeviceOperationError;

import org.junit.Assert;
import org.junit.Test;

public class DeviceOperationExceptionTest {

    @Test
    public void testDeviceOperationException() {
        DeviceOperationError error = new DeviceOperationError(DeviceOperationError.DEVICE_DENIED_THE_REQUEST);

        DeviceOperationException exception = new DeviceOperationException("CustomException", error.getReason(), null);
        try {
            throw exception;
        } catch (DeviceOperationException e) {
            Assert.assertEquals(DeviceOperationError.DEVICE_DENIED_THE_REQUEST, e.getErrorCode());
            Assert.assertEquals("CustomException", e.getMessage());
        }
    }
}
