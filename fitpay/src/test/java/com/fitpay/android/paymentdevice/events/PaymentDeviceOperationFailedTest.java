package com.fitpay.android.paymentdevice.events;

import org.junit.Assert;
import org.junit.Test;

public class PaymentDeviceOperationFailedTest {

    @Test
    public void resultTest() {
        PaymentDeviceOperationFailed operationFailed = new PaymentDeviceOperationFailed.Builder()
                .reason("IDDQD")
                .reasonCode(1001)
                .build();

        Assert.assertEquals(operationFailed.getReason(), "IDDQD");
        Assert.assertEquals(operationFailed.getReasonCode(), 1001);
    }
}
