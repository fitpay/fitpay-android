package com.fitpay.android.api.models.apdu;

import android.support.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;

import java.util.List;

/**
 * Apdu package
 */
public final class ApduPackage extends ApduPackageModel {
    private static final String APDU_RESPONSE = "apduResponse";

    /**
     * Endpoint to allow for returning responses to APDU execution.
     *
     * @param apduExecutionResult package confirmation data:(packageId, state, executedTs, executedDuration, apduResponses:(commandId, commandId, responseData))
     * @param callback            result callback
     */
    public void confirm(@NonNull ApduExecutionResult apduExecutionResult, @NonNull ApiCallback<Void> callback) {
        makePostCall(APDU_RESPONSE, apduExecutionResult, Void.class, callback);
    }

    public ApduCommand getNextCommand(ApduCommand lastCommand) {
        List<ApduCommand> commandList = getApduCommands();
        if (commandList != null && commandList.size() > 0) {
            if (lastCommand == null) {
                return commandList.get(0);
            }
            int nextIndex = commandList.indexOf(lastCommand) + 1;
            if (nextIndex < commandList.size()) {
                return commandList.get(nextIndex);
            }
        }
        return null;
    }

}
