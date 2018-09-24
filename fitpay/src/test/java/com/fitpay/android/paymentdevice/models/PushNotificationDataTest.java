package com.fitpay.android.paymentdevice.models;

import com.fitpay.android.paymentdevice.enums.PushNotification;
import com.fitpay.android.paymentdevice.events.NotificationSyncRequest;
import com.fitpay.android.utils.Constants;

import org.junit.Assert;
import org.junit.Test;

public class PushNotificationDataTest {

    @Test
    public void test1_NotificationDetails() {
        String data = "{\"_links\":{\"user\":{\"href\":\"https://api.fit-pay.com/users/73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786\"},\"creditCard\":{\"href\":\"https://api.fit-pay.com/users/73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786/creditCards/38a511c3-7e03-4711-b657-91d22c72db38\"},\"device\":{\"href\":\"https://api.fit-pay.com/users/73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786/devices/4f593dbc-89ec-4408-a92e-c252cdc5e231\"}},\"userId\":\"73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786\",\"deviceId\":\"4f593dbc-89ec-4408-a92e-c252cdc5e231\",\"creditCardId\":\"38a511c3-7e03-4711-b657-91d22c72db38\",\"clientId\":\"testId\"}";
        NotificationDetails details = Constants.getGson().fromJson(data, NotificationDetails.class);
        details.setType(PushNotification.CREDITCARD_ACTIVATED);
        Assert.assertNotNull(details);
        Assert.assertEquals("73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786", details.getUserId());
        Assert.assertEquals("4f593dbc-89ec-4408-a92e-c252cdc5e231", details.getDeviceId());
        Assert.assertEquals("38a511c3-7e03-4711-b657-91d22c72db38", details.getCreditCardId());
        Assert.assertEquals("testId", details.getClientId());
        Assert.assertEquals(PushNotification.CREDITCARD_ACTIVATED, details.getType());
    }

    @Test
    public void test2_SyncInfo() {
        String data = "{\"deviceId\":\"4f593dbc-89ec-4408-a92e-c252cdc5e231\",\"clientId\":\"testId\",\"type\":\"SYNC\",\"_links\":{\"ackSync\":{\"href\":\"https://api.fit-pay.com/notifications/563e2142-ec49-4597-b6ed-bffb42962447/acknowledge\"},\"completeSync\":{\"href\":\"https://api.fit-pay.com/notifications/563e2142-ec49-4597-b6ed-bffb42962447/complete\"}},\"id\":\"563e2142-ec49-4597-b6ed-bffb42962447\"}";
        NotificationSyncRequest request = new NotificationSyncRequest(data);
        Assert.assertNotNull(request);
        Assert.assertNotNull(request.getSyncInfo());
        Assert.assertEquals("4f593dbc-89ec-4408-a92e-c252cdc5e231", request.getSyncInfo().getDeviceId());
        Assert.assertEquals("563e2142-ec49-4597-b6ed-bffb42962447", request.getSyncInfo().getSyncId());
        Assert.assertEquals("testId", request.getSyncInfo().getClientId());
        Assert.assertEquals(PushNotification.SYNC, request.getSyncInfo().getType());

    }
}
