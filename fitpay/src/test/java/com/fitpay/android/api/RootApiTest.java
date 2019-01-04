package com.fitpay.android.api;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.api.callbacks.ApiCallbackExt;
import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.api.models.RootLinks;
import com.fitpay.android.utils.NamedResource;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RootApiTest extends BaseTestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(RootApiTest.class);

    @Test
    public void getRootLinks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        RootLinks[] apiResult = new RootLinks[1];

        ApiManager.getInstance().getRootLinks(new ApiCallbackExt<RootLinks>() {
            @Override
            public void onFailure(ErrorResponse apiErrorResponse) {
                latch.countDown();
            }

            @Override
            public void onSuccess(RootLinks result) {
                apiResult[0] = result;
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        Assert.assertNotNull("Result is empty", apiResult[0]);
        Assert.assertNotNull("webapp.privacyPolicy is empty", apiResult[0].getWebappPrivacyPolicy());
        Assert.assertNotNull("webapp.terms is empty", apiResult[0].getWebappTerms());
    }
}
