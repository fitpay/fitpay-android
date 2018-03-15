package com.fitpay.android;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.sse.UserEventStream;
import com.fitpay.android.api.sse.UserEventStreamManager;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.Command;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class UserEventStreamSyncTest extends TestActions {

    @Test
    public void testUserEventStreamProducesSync() throws Exception {
        this.user = getUser();
        assertNotNull(user);

        Device device = createDevice(user, getTestDevice());

        final CountDownLatch syncLatch = new CountDownLatch(1);
        final List<SyncRequest> syncRequests = new ArrayList<>();
        NotificationManager.getInstance().addListener(new Listener() {
            @Override
            public Map<Class, Command> getCommands() {
                mCommands.put(SyncRequest.class, data -> handleSyncRequest((SyncRequest) data));
                return mCommands;
            }

            public void handleSyncRequest(SyncRequest request) {
                syncRequests.add(request);
                syncLatch.countDown();
            }
        });

        UserEventStream stream = UserEventStreamManager.subscribe(user.getId(), new MockPaymentDeviceConnector(), device);

        CreditCard createdCard = createCreditCard(user, getTestCreditCard("9999504454545450"));

        final CountDownLatch latch = new CountDownLatch(1);
        createdCard.acceptTerms(new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                latch.countDown();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {

            }
        });

        syncLatch.await(5000, TimeUnit.MILLISECONDS);

        assertTrue(syncRequests.size() > 0);
        SyncRequest syncRequest = syncRequests.get(0);
        assertNotNull(syncRequest.getSyncId());

        assertNotNull(syncRequest.getSyncInfo());
        assertEquals(user.getId(), syncRequest.getSyncInfo().getUserId());
        assertEquals(device.getDeviceIdentifier(), syncRequest.getSyncInfo().getDeviceId());
        assertEquals(SyncInitiator.PLATFORM, syncRequest.getSyncInfo().getInitiator());
        assertEquals(syncRequest.getSyncId(), syncRequest.getSyncInfo().getSyncId());
        assertEquals(ApiManager.getConfig().get("clientId"), syncRequest.getSyncInfo().getClientId());

        assertNotNull(syncRequest.getConnector());
        assertNotNull(syncRequest.getDevice());
        assertEquals(device.getDeviceIdentifier(), syncRequest.getDevice().getDeviceIdentifier());
        assertNotNull(syncRequest.getUser());
        assertEquals(user.getId(), syncRequest.getUser().getId());


    }

}