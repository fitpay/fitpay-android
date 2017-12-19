package com.fitpay.android.webview.impl.parser;

import com.fitpay.android.utils.FPLog;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;

/**
 * RtmMessage parser
 */
public class RtmParser {
    private static final String TAG = RtmParser.class.getSimpleName();

    protected WebViewCommunicatorImpl impl;

    public RtmParser(WebViewCommunicatorImpl impl) {
        this.impl = impl;
    }

    public void parseMessage(RtmMessage msg) {
        FPLog.d(TAG, String.format("Unrecognized RTM message of type %s. Skipping.", msg.getType()));
    }

    protected void throwException(String message) {
        throw new IllegalStateException(message);
    }
}
