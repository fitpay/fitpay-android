package com.fitpay.android.webview.impl.parser;

import android.text.TextUtils;

import com.fitpay.android.a2averification.A2AIssuerAppVerification;
import com.fitpay.android.a2averification.A2AVerificationError;
import com.fitpay.android.a2averification.A2AVerificationFailed;
import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.ApiErrorDetails;
import com.fitpay.android.webview.events.IdVerificationRequest;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.events.RtmMessageResponse;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;

import java.util.Locale;

/**
 * RtmMessage parser v5
 */
public class RtmParserV5 extends RtmParserV4 {

    public RtmParserV5(WebViewCommunicatorImpl impl) {
        super(impl);
    }

    @Override
    public void parseMessage(RtmMessage msg) {
        switch (msg.getType()) {
            case RtmType.ID_VERIFICATION_REQUEST:
                impl.postMessage(new IdVerificationRequest(msg.getCallbackId()));
                break;

            case RtmType.SUPPORTS_ISSUER_APP_VERIFICATION:
                impl.postMessage(new RtmMessageResponse(msg.getCallbackId(), true, new A2AIssuerAppVerification(impl.supportsAppVerification()), RtmType.SUPPORTS_ISSUER_APP_VERIFICATION));
                break;

            case RtmType.APP_TO_APP_VERIFICATION:
                if (impl.supportsAppVerification()) {
                    A2AVerificationRequest appToAppVerification = Constants.getGson().fromJson(msg.getData(), A2AVerificationRequest.class);
                    appToAppVerification.setCallbackId(msg.getCallbackId());
                    impl.postMessage(appToAppVerification);
                } else {
                    impl.postMessage(new RtmMessageResponse(msg.getCallbackId(), false,
                            new A2AVerificationFailed(A2AVerificationError.NOT_SUPPORTED), RtmType.APP_TO_APP_VERIFICATION));
                }
                break;

            case RtmType.API_ERROR_DETAILS:
                ApiErrorDetails apiErrorDetails = Constants.getGson().fromJson(msg.getData(), ApiErrorDetails.class);

                int code = 0;
                String message = "Unknown error";

                if(apiErrorDetails != null) {
                    code = apiErrorDetails.getCode();
                    if (!TextUtils.isEmpty(apiErrorDetails.getDetailedMessage())) {
                        message = apiErrorDetails.getDetailedMessage();
                    } else if(!TextUtils.isEmpty(apiErrorDetails.getFullMessage())) {
                        message = apiErrorDetails.getFullMessage();
                    }

                    RxBus.getInstance().post(apiErrorDetails);
                }

                FPLog.e(RtmParser.TAG, String.format(Locale.getDefault(), "API Error - Code: %d Message:%s", code, message));

                break;

            default:
                super.parseMessage(msg);
        }
    }
}
