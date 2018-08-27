package com.fitpay.android.paymentdevice.utils;

import android.content.Context;

import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.PaymentDeviceConnectable;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.HashMap;


public class PaymentDeviceConnectorManagerTest {

    @Test
    public void managerTest() throws NoSuchFieldException {

        PaymentDeviceConnectorManager manager = PaymentDeviceConnectorManager.getInstance();
        Assert.assertNotNull(manager);

        Context context = Mockito.mock(Context.class);
        MockPaymentDeviceConnector connector = new MockPaymentDeviceConnector(context);
        Assert.assertNotNull(connector);

        HashMap<String, PaymentDeviceConnectable> map = new HashMap<>(5);
        FieldSetter.setField(manager, manager.getClass().getDeclaredField("connectors"), map);

        manager.addConnector("1", connector);
        Assert.assertEquals(connector, manager.getConnector("1"));

        manager.removeConnector(connector);
        Assert.assertNull(manager.getConnector("1"));
    }
}
