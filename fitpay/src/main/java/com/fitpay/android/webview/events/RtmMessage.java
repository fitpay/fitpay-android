package com.fitpay.android.webview.events;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.webview.enums.RtmType;

/**
 * RTM message from JS
 */
public class RtmMessage {

    private String data;
    private String type;
    private String callbackId;

    public RtmMessage(String callbackId, Object data, String type) {
        this.callbackId = callbackId;
        this.type = type;
        if (data != null) {
            if (data instanceof String) {
                this.data= (String) data;
            } else {
                this.data = Constants.getGson().toJson(data);
            }
        }
    }

    @Deprecated //see getData()
    public String getJsonData() {
        return data;
    }

    public String getData() {
        return data;
    }

    @RtmType.Request
    public String getType() {
        return type;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public String toString() {
        return Constants.getGson().toJson(this);
    }
}
