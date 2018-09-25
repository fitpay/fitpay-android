package com.fitpay.android.paymentdevice.models;

import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.paymentdevice.events.NotificationSyncRequest;
import com.fitpay.android.paymentdevice.events.PushNotificationRequest;

import org.junit.Assert;
import org.junit.Test;

public class PushNotificationDataTest {

    @Test
    public void test1_NotificationDetails() {
        String data = "{\"_links\":{\"user\":{\"href\":\"https://api.fit-pay.com/users/73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786\"},\"creditCard\":{\"href\":\"https://api.fit-pay.com/users/73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786/creditCards/38a511c3-7e03-4711-b657-91d22c72db38\"},\"device\":{\"href\":\"https://api.fit-pay.com/users/73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786/devices/4f593dbc-89ec-4408-a92e-c252cdc5e231\"}},\"userId\":\"73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786\",\"deviceId\":\"4f593dbc-89ec-4408-a92e-c252cdc5e231\",\"creditCardId\":\"38a511c3-7e03-4711-b657-91d22c72db38\",\"clientId\":\"testId\"}";
        PushNotificationRequest details = new PushNotificationRequest(data);
        Assert.assertNotNull(details);

        SyncInfo syncInfo = details.getSyncInfo();
        Assert.assertNotNull(syncInfo);
        syncInfo.setType(PushNotificationRequest.CREDITCARD_ACTIVATED);

        Assert.assertEquals("73dc1dfa-fbf5-4fc6-86d1-4f9ca45ac786", syncInfo.getUserId());
        Assert.assertEquals("4f593dbc-89ec-4408-a92e-c252cdc5e231", syncInfo.getDeviceId());
        Assert.assertEquals("38a511c3-7e03-4711-b657-91d22c72db38", syncInfo.getCreditCardId());
        Assert.assertEquals("testId", syncInfo.getClientId());
        Assert.assertEquals(PushNotificationRequest.CREDITCARD_ACTIVATED, syncInfo.getType());
    }

    @Test
    public void test2_SyncInfo() {
        String data = "{\"deviceId\":\"4f593dbc-89ec-4408-a92e-c252cdc5e231\",\"clientId\":\"testId\",\"type\":\"SYNC\",\"_links\":{\"ackSync\":{\"href\":\"https://api.fit-pay.com/notifications/563e2142-ec49-4597-b6ed-bffb42962447/acknowledge\"},\"completeSync\":{\"href\":\"https://api.fit-pay.com/notifications/563e2142-ec49-4597-b6ed-bffb42962447/complete\"}},\"id\":\"563e2142-ec49-4597-b6ed-bffb42962447\"}";
        NotificationSyncRequest request = new NotificationSyncRequest(data);
        Assert.assertNotNull(request);

        SyncInfo syncInfo = request.getSyncInfo();
        Assert.assertNotNull(syncInfo);
        syncInfo.setInitiator(SyncInitiator.PLATFORM);

        Assert.assertEquals("4f593dbc-89ec-4408-a92e-c252cdc5e231", syncInfo.getDeviceId());
        Assert.assertEquals("563e2142-ec49-4597-b6ed-bffb42962447", syncInfo.getSyncId());
        Assert.assertEquals("testId", syncInfo.getClientId());
        Assert.assertEquals(PushNotificationRequest.SYNC, syncInfo.getType());
        Assert.assertEquals(SyncInitiator.PLATFORM, syncInfo.getInitiator());

    }
}
