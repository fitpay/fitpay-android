package com.fitpay.android.paymentdevice.impl.ble.message;

import com.fitpay.android.paymentdevice.interfaces.ISecureMessage;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

public class MessagesFromCharacteristicTest {

    @Test
    public void testISecureMessage() {
        byte[] value = new byte[]{0x01};
        ISecureMessage securityStateMessage = new SecurityStateMessage().withData(value);
        Assert.assertTrue(securityStateMessage.isNfcEnabled());
    }

    @Test
    public void testNotificationMessage() {
        Date date = new Date(System.currentTimeMillis());
        NotificationMessage notificationMessage = new NotificationMessage().withDate(date);
        Assert.assertEquals(date, notificationMessage.getDate());
        byte[] message = notificationMessage.getMessage();
        Assert.assertTrue(Arrays.equals(message, MessageBuilder.getDateTimeMessage(date)));
    }

    @Test
    public void testApduResultMessage() {
        byte[] value = "9000".getBytes();
        ApduResultMessage apduResultMessage = new ApduResultMessage().withMessage(value);
        Assert.assertEquals(57, apduResultMessage.getResult());
        Assert.assertTrue(Arrays.equals(value, apduResultMessage.getMessage()));
    }
}
