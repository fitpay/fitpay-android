package com.fitpay.android.api.models.security;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.KeysManager;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NamedResource;
import com.fitpay.android.utils.NotificationManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by ssteveli on 10/5/17.
 */

public class BearerTokenTest extends BaseTestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(BearerTokenTest.class);

    private AccessDeniedListener listener;

    @Before
    public void before() throws Exception {
        this.listener = new AccessDeniedListener();
        NotificationManager.getInstance().addListenerToCurrentThread(this.listener);
    }

    @Override
    @After
    public void after() {
        NotificationManager.getInstance().removeListener(this.listener);
        this.listener = null;
        super.after();
    }

    @Test
    public void testExpiredTokenTriggerAccessDeniedEvents() throws Exception {
        OAuthToken token = new OAuthToken.Builder()
                .accessToken("eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0MmMwOWY5YS1mYWRmLTQyZDUtOGYzZC0zN2M4NTI2MTllY2YiLCJzdWIiOiIwYWQxMmEwNC0yZDc0LTRmYjUtYjFmMi00ZmVkZjcwMGRlMGQiLCJzY29wZSI6WyJ1c2VyLnJlYWQiLCJ1c2VyLndyaXRlIiwidHJhbnNhY3Rpb25zLnJlYWQiLCJkZXZpY2VzLndyaXRlIiwiZGV2aWNlcy5yZWFkIiwib3JnYW5pemF0aW9ucy5GSVRQQVkiLCJjcmVkaXRDYXJkcy53cml0ZSIsImNyZWRpdENhcmRzLnJlYWQiXSwiY2xpZW50X2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiY2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiYXpwIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlcl9pZCI6IjBhZDEyYTA0LTJkNzQtNGZiNS1iMWYyLTRmZWRmNzAwZGUwZCIsIm9yaWdpbiI6InVhYSIsInVzZXJfbmFtZSI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSFmcF93ZWJhcHBfcEprVnAyUmwiLCJlbWFpbCI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSIsImF1dGhfdGltZSI6MTUwNTMyMDM3NCwicmV2X3NpZyI6IjU5MzQ5Njc1IiwiaWF0IjoxNTA1MzIwMzc0LCJleHAiOjE1MDUzNjM1NzQsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC91YWEvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlciIsInRyYW5zYWN0aW9ucyIsImRldmljZXMiLCJvcmdhbml6YXRpb25zIiwiY3JlZGl0Q2FyZHMiXX0.Z6WP2EIZR7jumtqfPboCPczJf-CR3I6RF498UlNQPsVuOV9bVbK1o0UjhVWYUnKQEfc_Ujirp_z8Eb6jeDx1eFyDN6cvFV9Bp0UJrvPBO79gCL3jeu0yb-M1mESTYKuoyk5rDa4_jW_1gI9BKDX8UXAEICaELasQRv4fgG0zGcua-f-FJJywtkvLc3PEaZP2xN8wpcUL053jg2QaNjgGWH_YWN3krj43gnAcgt9rOVZlTJKSGpED0Np4bq8IHZa6FBh-aFG0OzO3VWilMHiwFDLTEIlgrfVvV5-7_JKXDDDgy9ukbtmbzth1xPVBVNlxKS7K6tSlvttJ3esRuYMUqw")
                .build();

        // set a mock key pair to avoid intercepted additional API call to create key pair
        KeysManager.getInstance().createPairForType(KeysManager.KEY_API);
        ApiManager.getInstance().setAuthToken(token);

        CountDownLatch latch = new CountDownLatch(1);
        final List<Integer> codes = new ArrayList<>();

        ApiManager.getInstance().getUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                codes.add(200);
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                FPLog.e(errorMessage);
                codes.add(errorCode);
                latch.countDown();

            }
        });

        latch.await();
        Assert.assertEquals("incorrect number for response codes captured", 1, codes.size());
        Assert.assertEquals(new Integer(AccessDenied.INVALID_TOKEN_RESPONSE_CODE), codes.get(0));
        Assert.assertEquals("access denied not posted to RxBus", 2, listener.getReceived().size());
        Assert.assertEquals(AccessDenied.Reason.EXPIRED_TOKEN, listener.getReceived().get(0).getReason());
        Assert.assertEquals(AccessDenied.Reason.UNAUTHORIZED, listener.getReceived().get(1).getReason());
    }

    @Test
    public void testExpiredTokenRefreshResetsInterceptor() throws Exception {
        OAuthToken token = new OAuthToken.Builder()
                .accessToken("eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0MmMwOWY5YS1mYWRmLTQyZDUtOGYzZC0zN2M4NTI2MTllY2YiLCJzdWIiOiIwYWQxMmEwNC0yZDc0LTRmYjUtYjFmMi00ZmVkZjcwMGRlMGQiLCJzY29wZSI6WyJ1c2VyLnJlYWQiLCJ1c2VyLndyaXRlIiwidHJhbnNhY3Rpb25zLnJlYWQiLCJkZXZpY2VzLndyaXRlIiwiZGV2aWNlcy5yZWFkIiwib3JnYW5pemF0aW9ucy5GSVRQQVkiLCJjcmVkaXRDYXJkcy53cml0ZSIsImNyZWRpdENhcmRzLnJlYWQiXSwiY2xpZW50X2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiY2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiYXpwIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlcl9pZCI6IjBhZDEyYTA0LTJkNzQtNGZiNS1iMWYyLTRmZWRmNzAwZGUwZCIsIm9yaWdpbiI6InVhYSIsInVzZXJfbmFtZSI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSFmcF93ZWJhcHBfcEprVnAyUmwiLCJlbWFpbCI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSIsImF1dGhfdGltZSI6MTUwNTMyMDM3NCwicmV2X3NpZyI6IjU5MzQ5Njc1IiwiaWF0IjoxNTA1MzIwMzc0LCJleHAiOjE1MDUzNjM1NzQsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC91YWEvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlciIsInRyYW5zYWN0aW9ucyIsImRldmljZXMiLCJvcmdhbml6YXRpb25zIiwiY3JlZGl0Q2FyZHMiXX0.Z6WP2EIZR7jumtqfPboCPczJf-CR3I6RF498UlNQPsVuOV9bVbK1o0UjhVWYUnKQEfc_Ujirp_z8Eb6jeDx1eFyDN6cvFV9Bp0UJrvPBO79gCL3jeu0yb-M1mESTYKuoyk5rDa4_jW_1gI9BKDX8UXAEICaELasQRv4fgG0zGcua-f-FJJywtkvLc3PEaZP2xN8wpcUL053jg2QaNjgGWH_YWN3krj43gnAcgt9rOVZlTJKSGpED0Np4bq8IHZa6FBh-aFG0OzO3VWilMHiwFDLTEIlgrfVvV5-7_JKXDDDgy9ukbtmbzth1xPVBVNlxKS7K6tSlvttJ3esRuYMUqw")
                .build();

        // set a mock key pair to avoid intercepted additional API call to create key pair
        KeysManager.getInstance().createPairForType(KeysManager.KEY_API);
        ApiManager.getInstance().setAuthToken(token);

        for (int i = 0; i < 2; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            ApiManager.getInstance().getUser(new ApiCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    latch.countDown();
                }

                @Override
                public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                    latch.countDown();

                }
            });
            latch.await();
        }

        List<AccessDenied> tokenRefreshRequired = listener.getReceived().stream()
                .filter(e -> e.getReason() == AccessDenied.Reason.EXPIRED_TOKEN)
                .filter(AccessDenied::isTokenRefreshRequired)
                .collect(Collectors.toList());

        Assert.assertEquals("should have 1 refresh required", 1, tokenRefreshRequired.size());
        listener.cleanReceived();

        // resetting the token should reset the interceptor
        ApiManager.getInstance().setAuthToken(token);

        CountDownLatch latch = new CountDownLatch(1);
        ApiManager.getInstance().getUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                FPLog.e(errorMessage);
                latch.countDown();

            }
        });
        latch.await();

        tokenRefreshRequired = listener.getReceived().stream()
                .filter(AccessDenied::isTokenRefreshRequired)
                .collect(Collectors.toList());

        Assert.assertEquals("should have 1 refresh required", 0, tokenRefreshRequired.size());
    }

    @Test
    public void testExpiredTokenDoesNotResultInEndlessRetry() throws Exception {
        OAuthToken token = new OAuthToken.Builder()
                .accessToken("eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0MmMwOWY5YS1mYWRmLTQyZDUtOGYzZC0zN2M4NTI2MTllY2YiLCJzdWIiOiIwYWQxMmEwNC0yZDc0LTRmYjUtYjFmMi00ZmVkZjcwMGRlMGQiLCJzY29wZSI6WyJ1c2VyLnJlYWQiLCJ1c2VyLndyaXRlIiwidHJhbnNhY3Rpb25zLnJlYWQiLCJkZXZpY2VzLndyaXRlIiwiZGV2aWNlcy5yZWFkIiwib3JnYW5pemF0aW9ucy5GSVRQQVkiLCJjcmVkaXRDYXJkcy53cml0ZSIsImNyZWRpdENhcmRzLnJlYWQiXSwiY2xpZW50X2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiY2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiYXpwIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlcl9pZCI6IjBhZDEyYTA0LTJkNzQtNGZiNS1iMWYyLTRmZWRmNzAwZGUwZCIsIm9yaWdpbiI6InVhYSIsInVzZXJfbmFtZSI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSFmcF93ZWJhcHBfcEprVnAyUmwiLCJlbWFpbCI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSIsImF1dGhfdGltZSI6MTUwNTMyMDM3NCwicmV2X3NpZyI6IjU5MzQ5Njc1IiwiaWF0IjoxNTA1MzIwMzc0LCJleHAiOjE1MDUzNjM1NzQsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC91YWEvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlciIsInRyYW5zYWN0aW9ucyIsImRldmljZXMiLCJvcmdhbml6YXRpb25zIiwiY3JlZGl0Q2FyZHMiXX0.Z6WP2EIZR7jumtqfPboCPczJf-CR3I6RF498UlNQPsVuOV9bVbK1o0UjhVWYUnKQEfc_Ujirp_z8Eb6jeDx1eFyDN6cvFV9Bp0UJrvPBO79gCL3jeu0yb-M1mESTYKuoyk5rDa4_jW_1gI9BKDX8UXAEICaELasQRv4fgG0zGcua-f-FJJywtkvLc3PEaZP2xN8wpcUL053jg2QaNjgGWH_YWN3krj43gnAcgt9rOVZlTJKSGpED0Np4bq8IHZa6FBh-aFG0OzO3VWilMHiwFDLTEIlgrfVvV5-7_JKXDDDgy9ukbtmbzth1xPVBVNlxKS7K6tSlvttJ3esRuYMUqw")
                .build();

        // set a mock key pair to avoid intercepted additional API call to create key pair
        KeysManager.getInstance().createPairForType(KeysManager.KEY_API);
        ApiManager.getInstance().setAuthToken(token);

        CountDownLatch latch = new CountDownLatch(5);
        ApiManager.getInstance().getUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();

            }
        });

        latch.await(3000, TimeUnit.MILLISECONDS);
        Assert.assertEquals("should only have triggered one failure", 4, latch.getCount());
    }

    private class AccessDeniedListener extends Listener {
        private List<AccessDenied> received = new ArrayList<>();

        private AccessDeniedListener() {
            mCommands.put(AccessDenied.class, data -> onAccessDenied((AccessDenied) data));
        }

        private void onAccessDenied(AccessDenied accessDenied) {
            received.add(accessDenied);
        }

        public List<AccessDenied> getReceived() {
            return received;
        }

        public void cleanReceived() {
            received.clear();
        }
    }
}
