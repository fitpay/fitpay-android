package com.fitpay.android.webview.impl;

import android.app.Activity;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestUtils;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.models.user.UserCreateRequest;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.utils.EventCallback;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.webview.WebViewCommunicator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by tgs on 5/20/16.
 */
public class WebViewCommunicatorTest extends TestActions {

    private User user;

    @Before
    public void setup() throws Exception {
        userName = TestUtils.getRandomLengthString(5, 10) + "@"
                + TestUtils.getRandomLengthString(5, 10) + "." + TestUtils.getRandomLengthString(4, 10);
        pin = TestUtils.getRandomLengthNumber(4, 4);

        UserCreateRequest user = getNewTestUser(userName, pin);
        User createdUser = createUser(user);
        assertNotNull("user should have been created", createdUser);

        loginIdentity = getTestLoginIdentity(userName, pin);
        doLogin(loginIdentity);

        this.user = getUser();
        assertNotNull(user);
        Device device = getTestDevice();

        Device createdDevice = createDevice(this.user, device);

        assertNotNull("device", createdDevice);
    }

    @Test
    @Ignore  // can only be run manually since needs valid user token
    public void testSendUserData() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        @EventCallback.Status String[] status = new String[1];

        final Listener callbackListener = new EventCallbackListener() {
            @Override
            void onEvent(EventCallback callback) {
                status[0] = callback.getStatus();
                latch.countDown();
            }
        };

        NotificationManager.getInstance().addListener(callbackListener, Schedulers.immediate());

        String deviceId = "72f8f402-9afd-4856-9675-c6f2f54a6753";
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJjNDQ1MTY5OS00YWM1LTQ2ZWQtOTVhNy1iZTIyYTM5NTQ2Y2QiLCJzdWIiOiI1NzUwODY1ZC1iMGQ4LTQwYTYtOWQ4NS1mNmM4NjNmN2E2YzYiLCJzY29wZSI6WyJ1c2VyLnJlYWQiLCJ1c2VyLndyaXRlIiwidHJhbnNhY3Rpb25zLnJlYWQiLCJkZXZpY2VzLndyaXRlIiwiZGV2aWNlcy5yZWFkIiwiY3JlZGl0Q2FyZHMud3JpdGUiLCJjcmVkaXRDYXJkcy5yZWFkIl0sImNsaWVudF9pZCI6InBhZ2FyZSIsImNpZCI6InBhZ2FyZSIsImF6cCI6InBhZ2FyZSIsInVzZXJfaWQiOiI1NzUwODY1ZC1iMGQ4LTQwYTYtOWQ4NS1mNmM4NjNmN2E2YzYiLCJvcmlnaW4iOiJ1YWEiLCJ1c2VyX25hbWUiOiJwQHAuY29tIXBhZ2FyZSIsImVtYWlsIjoicEBwLmNvbSIsImF1dGhfdGltZSI6MTQ3OTk5NTI1MiwicmV2X3NpZyI6ImUwYjM5MDZiIiwiaWF0IjoxNDc5OTk1MjUyLCJleHAiOjE0Nzk5OTcwNTIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC91YWEvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsicGFnYXJlIiwidXNlciIsInRyYW5zYWN0aW9ucyIsImRldmljZXMiLCJjcmVkaXRDYXJkcyJdfQ.kRwsY3AKZ-fF4sWZQylThbbwPmHiqmS9D2Xd24Ux3KY-N9S7ZfUOunPa1wMa2jcxzZfBJ5FSdFm61Gqci99x85-PvwD7rUOJBr6AoBSRukzqf6S1m2owVAcWVJLsH8meGbFX_L-NG7yMwJAYiJQ2AGqBUkwvfm3fizpM-jBoKfqrkAjFeUceBiqGdFKljpcqyxHIhjQGOLxlJKOdYNQcr_aMQHg4yh8d_k5LggrKz6XeS7X8-Zj9sylWK4qxgt016o6qyUxgPKZb0GMYsu-XBi9fCAg2yjzttHCQTvk3dhAtw0Yb-HHW4DP6X9EdRpyaXptbWYHOThxnfYE7g0k7rQ";
        String userId = "5750865d-b0d8-40a6-9d85-f6c863f7a6c6";

        Activity context = Mockito.mock(Activity.class);
        WebViewCommunicator wvc = new WebViewCommunicatorImpl(context, new MockPaymentDeviceConnector(), -1);
        wvc.sendUserData(null, deviceId, token, userId);

        latch.await(60, TimeUnit.SECONDS);
        assertNotNull("status value", status[0]);
        assertEquals("status value", "OK", status[0]);

        NotificationManager.getInstance().removeListener(callbackListener);
    }

    private abstract class EventCallbackListener extends Listener {
        EventCallbackListener() {
            mCommands.put(EventCallback.class, data -> onEvent(((EventCallback) data)));
        }

        abstract void onEvent(EventCallback callback);
    }
}
