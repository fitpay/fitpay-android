package com.fitpay.android.webview.impl;

import org.junit.Assert;
import org.junit.Test;

public class AppResponseTest {

    @Test
    public void responseTest() {
        AppResponseModel failedResponse = new AppResponseModel.Builder()
                .status(1)
                .reason("reason_unknown")
                .build();

        Assert.assertEquals(failedResponse.getStatus(), 1);
        Assert.assertEquals(failedResponse.getReason(), "reason_unknown");
    }
}
