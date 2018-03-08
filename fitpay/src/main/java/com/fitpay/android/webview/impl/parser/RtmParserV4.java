package com.fitpay.android.webview.impl.parser;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.events.RtmMessageResponse;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;
import com.fitpay.android.webview.models.SdkVersion;

/**
 * RtmMessage parser v4
 */
public class RtmParserV4 extends RtmParserV2 {

    public RtmParserV4(WebViewCommunicatorImpl impl) {
        super(impl);
    }

    @Override
    public void parseMessage(RtmMessage msg) {
        switch (msg.getType()) {
            case RtmType.SCAN_REQUEST:
                impl.startScan(msg.getCallbackId());
                break;

            case RtmType.SDK_VERSION_REQUEST:
                RxBus.getInstance().post(new RtmMessageResponse(msg.getCallbackId(), new SdkVersion(BuildConfig.SDK_VERSION), RtmType.SDK_VERSION));
                break;

            default:
                super.parseMessage(msg);
        }
    }
}
