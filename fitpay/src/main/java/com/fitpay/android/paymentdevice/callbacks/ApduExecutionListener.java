package com.fitpay.android.paymentdevice.callbacks;

import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.utils.Listener;

/**
 * Apdu callback
 */
public abstract class ApduExecutionListener extends Listener implements IListeners.ApduListener {
    public ApduExecutionListener() {
        this(null);
    }

    public ApduExecutionListener(String connectorId) {
        super(connectorId);
        mCommands.put(ApduExecutionResult.class, data -> {
            ApduExecutionResult result = (ApduExecutionResult) data;

            switch (result.getState()) {
                case ResponseState.PROCESSED:
                    onApduPackageResultReceived(result);
                    break;

                default:
                    onApduPackageErrorReceived(result);
                    break;
            }
        });
    }
}
