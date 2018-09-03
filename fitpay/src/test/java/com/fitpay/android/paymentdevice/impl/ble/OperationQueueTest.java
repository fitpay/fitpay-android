package com.fitpay.android.paymentdevice.impl.ble;

import com.fitpay.android.TestActions;
import com.fitpay.android.api.models.apdu.ApduPackage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OperationQueueTest extends TestActions {

    @Override
    @Before
    public void before() {
    }

    @Test
    public void getFirstOperation() {
        ApduPackage apduPackage = getTestApduPackage();
        GattOperation operation = new GattApduOperation("1", apduPackage);
        OperationQueue queue = new OperationQueue();
        queue.add(operation);
        Assert.assertEquals(1, queue.size());

        int size = 0;
        while (queue.getFirst() != null) {
            size++;
        }

        Assert.assertEquals("total gatt operations", 26, size);
    }
}
