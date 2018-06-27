package com.fitpay.android;

import android.app.Activity;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.api.models.UserStreamEvent;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.sse.UserEventStreamListener;
import com.fitpay.android.api.sse.UserEventStreamManager;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class UserEventStreamSyncTest extends TestActions {

    @Test
    public void testWebviewCommunicatorUsesUserEventStream() throws Exception {
        // setup a user and device
        this.user = getUser();
        assertNotNull(user);

        Device device = createDevice(user, getTestDevice());

        Activity context = Mockito.mock(Activity.class);

        MockPaymentDeviceConnector connector = new MockPaymentDeviceConnector();

        // pretend to launch the webview and act like the user has logged into the WV, this should
        // cause the user event stream subscription to occur
        WebViewCommunicatorImpl wvc = new WebViewCommunicatorImpl(context, connector, null);
        wvc.sendUserData(
                null,
                device.getDeviceIdentifier(),
                ApiManager.getInstance().getApiService().getToken().getAccessToken(),
                user.getId());

        boolean subscribed = false;
        for (int i = 0; i < 10; i++) {
            subscribed = UserEventStreamManager.isSubscribed(user.getId());

            if (!subscribed) {
                Thread.sleep(500);
            }
        }

        assertTrue(UserEventStreamManager.isSubscribed(user.getId()));

        // now let's get the platform to initiate a SYNC by adding a card, the automatic sync
        // from user event stream is enabled by default, therefore we should see a sync requeset
        // come out onto the RxBus
        final CountDownLatch syncLatch = new CountDownLatch(1);
        final List<SyncRequest> syncRequests = new ArrayList<>();
        final SyncListener syncListener = new SyncListener(syncLatch, syncRequests);
        NotificationManager.getInstance().addListener(syncListener);

        CreditCard createdCard = createCreditCard(user, getTestCreditCardInfo("9999504454545450"));

        final CountDownLatch latch = new CountDownLatch(1);
        createdCard.acceptTerms(new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                latch.countDown();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        syncLatch.await(30000, TimeUnit.MILLISECONDS);

        NotificationManager.getInstance().removeListener(syncListener);

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

        // now let's close the webview and ensure the subscription is removed
        wvc.close();

        assertFalse(UserEventStreamManager.isSubscribed(user.getId()));
    }

    @Test
    public void testUserEventStreamSubscriptionProducesEvents() throws Exception {
        this.user = getUser();
        assertNotNull(user);

        createDevice(user, getTestDevice());

        // just to ensure events are going out, we'll just wait for the CREDITCARD_CREATED event
        final CountDownLatch eventLatch = new CountDownLatch(1);
        final List<UserStreamEvent> events = new ArrayList<>();
        NotificationManager.getInstance().addListener(new UserEventStreamListener() {
            @Override
            public void onUserEvent(UserStreamEvent event) {
                events.add(event);

                if ("CREDITCARD_CREATED".equals(event.getType())) {
                    eventLatch.countDown();
                }
            }
        });

        UserEventStreamManager.subscribe(user.getId());

        CreditCard createdCard = createCreditCard(user, getTestCreditCardInfo("9999504454545450"));

        final CountDownLatch latch = new CountDownLatch(1);
        createdCard.acceptTerms(new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                latch.countDown();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        eventLatch.await(30000, TimeUnit.MILLISECONDS);

        assertTrue(events.size() > 0);
    }

    private class SyncListener extends Listener {
        final CountDownLatch syncLatch;
        final List<SyncRequest> syncRequests;

        SyncListener(CountDownLatch latch, List<SyncRequest> requests) {
            this.syncLatch = latch;
            this.syncRequests = requests;
            mCommands.put(SyncRequest.class, data -> handleSyncRequest((SyncRequest) data));
        }

        void handleSyncRequest(SyncRequest request) {
            syncRequests.add(request);
            syncLatch.countDown();
        }
    }
}