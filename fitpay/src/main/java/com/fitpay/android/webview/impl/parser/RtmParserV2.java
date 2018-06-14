package com.fitpay.android.webview.impl.parser;

import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;
import com.fitpay.android.webview.models.RtmVersion;

import org.json.JSONObject;

import static com.fitpay.android.utils.Constants.WV_DATA;

/**
 * RtmMessage parser v2
 */
public class RtmParserV2 extends RtmParser {

    public RtmParserV2(WebViewCommunicatorImpl impl) {
        super(impl);
    }

    public void parseMessage(RtmMessage msg) {
        String callbackId = msg.getCallbackId();

        switch (msg.getType()) {
            case RtmType.USER_DATA:
                String deviceId = null;
                String token = null;
                String userId = null;

                try {
                    JSONObject obj = new JSONObject(msg.getData());
                    deviceId = obj.getString("deviceId");
                    token = obj.getString("token");
                    userId = obj.getString("userId");
                } catch (Exception e) {
                    FPLog.e(WV_DATA, e);
                    throwException("missing required message data");
                }

                impl.sendUserData(callbackId, deviceId, token, userId);

                break;
            case RtmType.SYNC:
                SyncInfo syncInfo = Constants.getGson().fromJson(msg.getData(), SyncInfo.class);

                if (null != syncInfo && null != syncInfo.getSyncId()) {
                    syncInfo.setInitiator(SyncInitiator.WEB_VIEW);
                    impl.sync(callbackId, syncInfo);
                } else {
                    impl.sync(callbackId);
                }

                break;
            case RtmType.VERSION:
                try {
                    RtmVersion webAppRtmVersion = Constants.getGson().fromJson(msg.getData(), RtmVersion.class);
                    if (webAppRtmVersion != null) {
                        impl.setWebAppRtmVersion(webAppRtmVersion);
                    } else {
                        throw new NullPointerException("RtmVersion is empty");
                    }
                } catch (Exception e) {
                    FPLog.e(WV_DATA, e);
                    throwException("missing required message data");
                }

                break;
            case RtmType.NO_HISTORY:
                impl.onNoHistory();

                break;
            default:
                super.parseMessage(msg);
        }
    }
}
