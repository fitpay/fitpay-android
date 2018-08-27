package com.fitpay.android.webview.models;

import com.fitpay.android.BuildConfig;

import org.junit.Assert;
import org.junit.Test;

public class ModelsTest {
    @Test
    public void rtmVersionTest() {
        RtmVersion version = new RtmVersion(171);
        Assert.assertEquals(171, version.getVersion());
    }

    @Test
    public void sdkVersionTest() {
        SdkVersion version = new SdkVersion(BuildConfig.SDK_VERSION);
        Assert.assertEquals(BuildConfig.SDK_VERSION, version.getVersion());
    }

}
