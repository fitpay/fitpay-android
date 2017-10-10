package com.fitpay.android.api.models.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by ssteveli on 10/9/17.
 */

public class OAuthTokenTest {

    @Test
    public void ensureJwtParsingIsWorkingCorrectly() {
        OAuthToken token = new OAuthToken.Builder()
                .accessToken("eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0MmMwOWY5YS1mYWRmLTQyZDUtOGYzZC0zN2M4NTI2MTllY2YiLCJzdWIiOiIwYWQxMmEwNC0yZDc0LTRmYjUtYjFmMi00ZmVkZjcwMGRlMGQiLCJzY29wZSI6WyJ1c2VyLnJlYWQiLCJ1c2VyLndyaXRlIiwidHJhbnNhY3Rpb25zLnJlYWQiLCJkZXZpY2VzLndyaXRlIiwiZGV2aWNlcy5yZWFkIiwib3JnYW5pemF0aW9ucy5GSVRQQVkiLCJjcmVkaXRDYXJkcy53cml0ZSIsImNyZWRpdENhcmRzLnJlYWQiXSwiY2xpZW50X2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiY2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiYXpwIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlcl9pZCI6IjBhZDEyYTA0LTJkNzQtNGZiNS1iMWYyLTRmZWRmNzAwZGUwZCIsIm9yaWdpbiI6InVhYSIsInVzZXJfbmFtZSI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSFmcF93ZWJhcHBfcEprVnAyUmwiLCJlbWFpbCI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSIsImF1dGhfdGltZSI6MTUwNTMyMDM3NCwicmV2X3NpZyI6IjU5MzQ5Njc1IiwiaWF0IjoxNTA1MzIwMzc0LCJleHAiOjE1MDUzNjM1NzQsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC91YWEvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlciIsInRyYW5zYWN0aW9ucyIsImRldmljZXMiLCJvcmdhbml6YXRpb25zIiwiY3JlZGl0Q2FyZHMiXX0.Z6WP2EIZR7jumtqfPboCPczJf-CR3I6RF498UlNQPsVuOV9bVbK1o0UjhVWYUnKQEfc_Ujirp_z8Eb6jeDx1eFyDN6cvFV9Bp0UJrvPBO79gCL3jeu0yb-M1mESTYKuoyk5rDa4_jW_1gI9BKDX8UXAEICaELasQRv4fgG0zGcua-f-FJJywtkvLc3PEaZP2xN8wpcUL053jg2QaNjgGWH_YWN3krj43gnAcgt9rOVZlTJKSGpED0Np4bq8IHZa6FBh-aFG0OzO3VWilMHiwFDLTEIlgrfVvV5-7_JKXDDDgy9ukbtmbzth1xPVBVNlxKS7K6tSlvttJ3esRuYMUqw")
                .build();

        Assert.assertEquals("0ad12a04-2d74-4fb5-b1f2-4fedf700de0d", token.getUserId());
        Assert.assertNotNull(token.getIssuedTs());
        Assert.assertNotNull(token.getExpiresTs());
        Assert.assertTrue(token.getExpiresTs().before(new Date()));
        Assert.assertTrue(token.isExpired());
    }

    @Test
    public void ensureWhenExpiredTsIsNotInTokenThatExpiresInIsUsed() throws Exception {
        OAuthToken token = new OAuthToken.Builder()
                .accessToken("eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0MmMwOWY5YS1mYWRmLTQyZDUtOGYzZC0zN2M4NTI2MTllY2YiLCJzdWIiOiIwYWQxMmEwNC0yZDc0LTRmYjUtYjFmMi00ZmVkZjcwMGRlMGQiLCJzY29wZSI6WyJ1c2VyLnJlYWQiLCJ1c2VyLndyaXRlIiwidHJhbnNhY3Rpb25zLnJlYWQiLCJkZXZpY2VzLndyaXRlIiwiZGV2aWNlcy5yZWFkIiwib3JnYW5pemF0aW9ucy5GSVRQQVkiLCJjcmVkaXRDYXJkcy53cml0ZSIsImNyZWRpdENhcmRzLnJlYWQiXSwiY2xpZW50X2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiY2lkIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwiYXpwIjoiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlcl9pZCI6IjBhZDEyYTA0LTJkNzQtNGZiNS1iMWYyLTRmZWRmNzAwZGUwZCIsIm9yaWdpbiI6InVhYSIsInVzZXJfbmFtZSI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSFmcF93ZWJhcHBfcEprVnAyUmwiLCJlbWFpbCI6InNjb3R0K25ld3dhbGxldEBmaXQtcGF5LmNvbSIsImF1dGhfdGltZSI6MTUwNTMyMDM3NCwicmV2X3NpZyI6IjU5MzQ5Njc1IiwiaWF0IjoxNTA1MzIwMzc0LCJleHAiOjE1MDUzNjM1NzQsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC91YWEvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsiZnBfd2ViYXBwX3BKa1ZwMlJsIiwidXNlciIsInRyYW5zYWN0aW9ucyIsImRldmljZXMiLCJvcmdhbml6YXRpb25zIiwiY3JlZGl0Q2FyZHMiXX0.Z6WP2EIZR7jumtqfPboCPczJf-CR3I6RF498UlNQPsVuOV9bVbK1o0UjhVWYUnKQEfc_Ujirp_z8Eb6jeDx1eFyDN6cvFV9Bp0UJrvPBO79gCL3jeu0yb-M1mESTYKuoyk5rDa4_jW_1gI9BKDX8UXAEICaELasQRv4fgG0zGcua-f-FJJywtkvLc3PEaZP2xN8wpcUL053jg2QaNjgGWH_YWN3krj43gnAcgt9rOVZlTJKSGpED0Np4bq8IHZa6FBh-aFG0OzO3VWilMHiwFDLTEIlgrfVvV5-7_JKXDDDgy9ukbtmbzth1xPVBVNlxKS7K6tSlvttJ3esRuYMUqw")
                .expiredTs(null)
                .expiresIn(1) // in seconds
                .build();

        Thread.sleep(2000);

        Assert.assertEquals("0ad12a04-2d74-4fb5-b1f2-4fedf700de0d", token.getUserId());
        Assert.assertNotNull(token.getIssuedTs());
        Assert.assertTrue(token.isExpired());
    }

}
