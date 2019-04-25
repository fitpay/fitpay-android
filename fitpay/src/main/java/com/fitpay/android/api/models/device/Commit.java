package com.fitpay.android.api.models.device;

import androidx.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.utils.FPLog;

/**
 * Commit
 */
public final class Commit extends CommitModel {

    private static final String REL_APDU_RESPONSE = "apduResponse";
    private static final String REL_CONFIRM = "confirm";

    /**
     * Endpoint to confirm APDU execution.
     *
     * @param apduExecutionResult package confirmation data:(packageId, state, executedTs, executedDuration, apduResponses:(commandId, commandId, responseData))
     * @param callback            result callback
     */
    public void confirm(@NonNull ApduExecutionResult apduExecutionResult, @NonNull ApiCallback<Void> callback) {
        if (!canConfirmApduResponse()) {
            FPLog.i("skipping apdu response confirmation, no apduResponse link is present");
            return;
        }

        makeNoResponsePostCall(REL_APDU_RESPONSE, apduExecutionResult, callback);
    }

    /**
     * Endpoint to confirm commit handling.
     *
     * @param commitConfirm       confirm result
     * @param callback            result callback
     */
    public void confirm(@NonNull CommitConfirm commitConfirm, @NonNull ApiCallback<Void> callback) {
        if (!canConfirmCommit()) {
            FPLog.i("skipping commit confirmation, no confirm link is present");
            return;
        }

        makeNoResponsePostCall(REL_CONFIRM, commitConfirm, callback);
    }

    public boolean canConfirmCommit() {
        return hasLink(REL_CONFIRM);
    }

    public boolean canConfirmApduResponse() {
        return hasLink(REL_APDU_RESPONSE);
    }
}