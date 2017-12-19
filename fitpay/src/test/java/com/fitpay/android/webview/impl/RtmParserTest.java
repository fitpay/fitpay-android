package com.fitpay.android.webview.impl;

import android.app.Activity;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessage;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by Vlad on 19.07.2017.
 */

public class RtmParserTest {

    private WebViewCommunicatorImpl wvci;

    @Before
    public void init() {
        Activity context = Mockito.mock(Activity.class);
        wvci = new WebViewCommunicatorImpl(context, -1);

    }

    @After
    public void terminate() {
        wvci = null;
    }

    @Test
    public void testWebAppVersionLower() {
        String rtmMsgStr = "{\"callbackId\":\"0\",\"data\":\"{\\\"version\\\":3}\",\"type\":\"version\"}";
        RtmMessage msg = Constants.getGson().fromJson(rtmMsgStr, RtmMessage.class);
        int webAppRtmVersion = 2;

        String errorMsg = null;
        try {
            RtmParserImpl.parse(wvci, webAppRtmVersion, msg);
        } catch (IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        Assert.assertNull(errorMsg);
    }

    @Test
    public void testWebAppVersionHigher() {
        String rtmMsgStr = "{\"callbackId\":\"0\",\"data\":\"{\\\"version\\\":3}\",\"type\":\"version\"}";
        RtmMessage msg = Constants.getGson().fromJson(rtmMsgStr, RtmMessage.class);
        int webAppRtmVersion = RtmType.RTM_VERSION + 1;

        String errorMsg = null;
        try {
            RtmParserImpl.parse(wvci, webAppRtmVersion, msg);
        } catch (IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("WebApp RTM version:" + webAppRtmVersion + " is not supported", errorMsg);
    }

    @Test
    public void testWebAppVersionSame() {
        String rtmMsgStr = "{\"callbackId\":\"0\",\"data\":\"{\\\"version\\\":3}\",\"type\":\"version\"}";
        RtmMessage msg = Constants.getGson().fromJson(rtmMsgStr, RtmMessage.class);
        int webAppRtmVersion = RtmType.RTM_VERSION;

        String errorMsg = null;
        try {
            RtmParserImpl.parse(wvci, webAppRtmVersion, msg);
        } catch (IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        Assert.assertNull(errorMsg);
    }

    @Ignore("needs to be rewritten, we don't throw exceptions anymore... needs to listen to RxBus instead for the unrecognized message")
    @Test
    public void testWebAppVersionSameNoMethod() {
        String rtmMsgStr = "{\"callbackId\":\"9\",\"data\":\"{\\\"next\\\":\\\"\\\\/walletAccess\\\",\\\"previous\\\":\\\"\\\\/cards\\\"}\",\"type\":\"navigationStart\"}";
        RtmMessage msg = Constants.getGson().fromJson(rtmMsgStr, RtmMessage.class);
        int webAppRtmVersion = 3;

        String errorMsg = "";
        try {
            RtmParserImpl.parse(wvci, webAppRtmVersion, msg);
        } catch (IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("unsupported action value navigationStart", errorMsg);
    }

    @Test
    public void testWebAppVersionSameWrongData() {
        String rtmMsgStr = "{\"callbackId\":\"0\",\"type\":\"version\"}";
        RtmMessage msg = Constants.getGson().fromJson(rtmMsgStr, RtmMessage.class);
        int webAppRtmVersion = RtmType.RTM_VERSION;

        String errorMsg = null;
        try {
            RtmParserImpl.parse(wvci, webAppRtmVersion, msg);
        } catch (IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("missing required message data", errorMsg);
    }
}
