package com.fitpay.android.webview.impl;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.events.RtmMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Vlad on 04.12.2017.
 */

public class UnrecognizedMessageTest extends BaseTestActions{

    private RtmMessage message;

    private CountDownLatch latch;
    private UnrecognizedRtmMessageListener listener;

    @Before
    public void before() throws Exception {
        latch = new CountDownLatch(1);
        this.listener = new UnrecognizedRtmMessageListener(latch);
        NotificationManager.getInstance().addListener(listener, Schedulers.immediate());
    }

    @Override
    @After
    public void after() {
        NotificationManager.getInstance().removeListener(listener);
        this.listener = null;
        super.after();
    }

    @Test
    public void testUnrecognizedRtmMessage() {
        String rtmMsgStr = "{\"callbackId\":\"10\",\"data\":\"{\\\"resource\\\":\\\"The Truth Is Out There\\\"}\",\"type\":\"somethingUnknown\"}";
        RtmMessage msg = Constants.getGson().fromJson(rtmMsgStr, RtmMessage.class);

        RxBus.getInstance().post(msg);

        try {
            FPLog.d("UnrecognizedMessageTest", "start:" + System.currentTimeMillis());
            latch.await(30, TimeUnit.SECONDS);
            FPLog.d("UnrecognizedMessageTest", "end:" + System.currentTimeMillis());
        } catch (InterruptedException e) {
            FPLog.d("UnrecognizedMessageTest", "exception:" + System.currentTimeMillis());
            e.printStackTrace();
        }

        assertNotNull("unrecognized message shouldn't be null", message);
        UnrecognizedRtmData data = Constants.getGson().fromJson(message.getData(), UnrecognizedRtmData.class);
        assertEquals("unrecognized message data should be equal", "The Truth Is Out There", data.resource);
    }

    private class UnrecognizedRtmMessageListener extends Listener {
        public UnrecognizedRtmMessageListener(final CountDownLatch latch) {
            super();
            mCommands.put(RtmMessage.class, data -> {
                FPLog.d("UnrecognizedMessageTest", "data received:" + data.toString());
                message = (RtmMessage) data;
                if ("somethingUnknown".equals(message.getType())) {
                    latch.countDown();
                }
            });
        }
    }

    private class UnrecognizedRtmData {
        String resource;
    }
}
