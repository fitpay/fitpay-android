package com.fitpay.android.webview.callbacks;

import com.fitpay.android.TestActions;
import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.events.IdVerificationRequest;
import com.fitpay.android.webview.events.UserReceived;
import com.fitpay.android.webview.models.IdVerification;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import rx.schedulers.Schedulers;

public class WVListenersTest extends TestActions {

    private String id = "12345";

    @Override
    @Before
    public void before() {
    }

    @Test
    public void a2aListenerTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        AtomicReference<A2AVerificationRequest> requestRef = new AtomicReference<>();
        A2AVerificationListener listener = new A2AVerificationListener(id) {
            @Override
            public void onRequestReceived(A2AVerificationRequest request) {
                requestRef.set(request);
                latch.countDown();
            }
        };

        NotificationManager.getInstance().addListener(listener, Schedulers.immediate());

        A2AVerificationRequest request = getA2AVerificationRequest();
        RxBus.getInstance().post(id, request);

        Thread.sleep(1000);
        latch.await(10, TimeUnit.SECONDS);

        NotificationManager.getInstance().removeListener(listener);

        Assert.assertNotNull(requestRef.get());
        Assert.assertEquals(request, requestRef.get());
    }

    @Test
    public void idVerificationTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<IdVerification> idVerRef = new AtomicReference<>();
        IdVerificationListener requestListener = new IdVerificationListener(id) {
            @Override
            public IdVerification getIdVerification() {
                IdVerification idVerification = super.getIdVerification();
                idVerRef.set(idVerification);
                latch.countDown();
                return idVerification;
            }
        };

        NotificationManager.getInstance().addListener(requestListener, Schedulers.immediate());

        Thread.sleep(1000);
        RxBus.getInstance().post(id, new IdVerificationRequest("1"));

        latch.await(10, TimeUnit.SECONDS);

        NotificationManager.getInstance().removeListener(requestListener);

        Assert.assertNotNull(idVerRef.get());
    }

    @Test
    public void userReceivedTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UserReceived> userReceivedRef = new AtomicReference<>();
        UserReceivedListener listener = new UserReceivedListener(id) {
            @Override
            public void onUserReceived(UserReceived data) {
                userReceivedRef.set(data);
                latch.countDown();
            }
        };

        NotificationManager.getInstance().addListener(listener, Schedulers.immediate());

        Thread.sleep(1000);
        RxBus.getInstance().post(id, new UserReceived("userId-1234", "test@test.test"));

        latch.await(10, TimeUnit.SECONDS);

        NotificationManager.getInstance().removeListener(listener);

        Assert.assertNotNull(userReceivedRef.get());
        Assert.assertEquals("userId-1234", userReceivedRef.get().userId());
        Assert.assertEquals("test@test.test", userReceivedRef.get().getEmail());
    }
}
