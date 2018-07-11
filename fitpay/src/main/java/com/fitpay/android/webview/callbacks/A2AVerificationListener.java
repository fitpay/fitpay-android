package com.fitpay.android.webview.callbacks;

import android.support.annotation.NonNull;

import com.fitpay.android.a2averification.A2AVerificationFailed;
import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessageResponse;

/**
 * Listen to a2a request {@link A2AVerificationRequest} and {@link A2AVerificationFailed}
 */
public class A2AVerificationListener extends Listener {
    private String requestCallbackId;
    private String returnLocation;

    public A2AVerificationListener(@NonNull String connectorId) {
        super(connectorId);
        mCommands.put(A2AVerificationRequest.class, data -> {
            A2AVerificationRequest request = (A2AVerificationRequest) data;
            returnLocation = request.getReturnLocation();
            requestCallbackId = request.getCallbackId();
            onRequestReceived(request);
        });
        mCommands.put(A2AVerificationFailed.class, data ->
                RxBus.getInstance().post(connectorId, new RtmMessageResponse(requestCallbackId, false, data, RtmType.APP_TO_APP_VERIFICATION)));
    }

    public void onRequestReceived(A2AVerificationRequest request) {
    }

    /**
     * Get app-to-app return location
     * <p>
     * On completion of the issuer intent the OEM app must then open the web-view using the returnLocation.
     * <baseUrl>/<returnLocation>?config=<base64 encoded config with a2a>
     *
     * @return a2a return location
     */
    public String getA2aReturnLocation() {
        return returnLocation;
    }
}
