package com.fitpay.android.webview.impl;

import android.app.Activity;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.events.UnrecognizedRtmMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Vlad on 19.07.2017.
 */

public class RtmParserTest extends BaseTestActions {

    private WebViewCommunicatorImpl wvci;

    @Before
    public void before() {
        Activity context = Mockito.mock(Activity.class);
        MockPaymentDeviceConnector deviceConnector = new MockPaymentDeviceConnector(context);
        wvci = new WebViewCommunicatorImpl(context, deviceConnector, null);
    }

    @Override
    @After
    public void after() {
        wvci.destroy();
        wvci = null;
        super.after();
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

    //@Ignore("needs to be rewritten, we don't throw exceptions anymore... needs to listen to RxBus instead for the unrecognized message")
    @Test
    public void testWebAppVersionSameNoMethod() throws InterruptedException {
        String rtmMsgStr = "{\"callbackId\":\"9\",\"data\":\"{\\\"next\\\":\\\"\\\\/walletAccess\\\",\\\"previous\\\":\\\"\\\\/cards\\\"}\",\"type\":\"navigationStart\"}";
        RtmMessage msg = Constants.getGson().fromJson(rtmMsgStr, RtmMessage.class);
        int webAppRtmVersion = 3;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<RtmMessage> rtmRef = new AtomicReference<>();
        Listener listener = new UnrecognizedMessageListener(wvci.getConnectorId(), rtmRef, latch);

        NotificationManager.getInstance().addListener(listener, Schedulers.immediate());

        RtmParserImpl.parse(wvci, webAppRtmVersion, msg);

        latch.await(10, TimeUnit.SECONDS);

        NotificationManager.getInstance().removeListener(listener);

        assertNotNull(rtmRef.get());
        assertEquals(msg, rtmRef.get());
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

    class UnrecognizedMessageListener extends Listener {
        private UnrecognizedMessageListener(String id, AtomicReference<RtmMessage> rtmRef, CountDownLatch latch) {
            super(id);
            mCommands.put(UnrecognizedRtmMessage.class, data -> {
                rtmRef.set(((UnrecognizedRtmMessage) data).getMessage());
                latch.countDown();
            });
        }
    }

    ;
}
