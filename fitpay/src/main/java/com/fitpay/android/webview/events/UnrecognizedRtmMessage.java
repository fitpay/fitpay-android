package com.fitpay.android.webview.events;

import com.fitpay.android.utils.Constants;

/**
 * RTM message from JS
 */
public class UnrecognizedRtmMessage {

    private RtmMessage message;

    public UnrecognizedRtmMessage(RtmMessage message) {
        this.message = message;
    }

    public RtmMessage getMessage() {
        return message;
    }

    public String toString() {
        return Constants.getGson().toJson(this);
    }
}
