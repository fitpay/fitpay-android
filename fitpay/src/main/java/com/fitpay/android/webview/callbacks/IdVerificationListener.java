package com.fitpay.android.webview.callbacks;

import androidx.annotation.NonNull;

import com.fitpay.android.utils.Listener;
import com.fitpay.android.webview.events.IdVerificationRequest;
import com.fitpay.android.webview.models.IdVerification;

/**
 * Listen to {@link IdVerificationRequest}
 */
public class IdVerificationListener extends Listener {
    public IdVerificationListener(@NonNull String connectorId) {
        super(connectorId);
        mCommands.put(IdVerificationRequest.class, data ->
                getIdVerification().send(connectorId, ((IdVerificationRequest) data).getCallbackId()));
    }

    public IdVerification getIdVerification() {
        return new IdVerification.Builder().build();
    }
}