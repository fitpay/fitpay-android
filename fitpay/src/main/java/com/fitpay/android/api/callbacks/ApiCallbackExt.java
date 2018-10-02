package com.fitpay.android.api.callbacks;

import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.utils.Constants;

/**
 * Communicates responses from a server
 *
 * @param <T> expected response type
 */
public abstract class ApiCallbackExt<T> implements ApiCallback<T> {

    /**
     * Invoked when a network or unexpected exception occurred during the HTTP request.
     *
     * @param apiErrorResponse parsed API error response
     */
    abstract public void onFailure(ErrorResponse apiErrorResponse);

    @Override
    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
        ErrorResponse errorResponse;
        try {
            errorResponse = Constants.getGson().fromJson(errorMessage, ErrorResponse.class);
        } catch (Exception e) {
            errorResponse = new ErrorResponse(errorCode, errorMessage);
        }
        onFailure(errorResponse);
    }
}
