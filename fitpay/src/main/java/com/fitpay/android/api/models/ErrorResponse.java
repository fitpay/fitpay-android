package com.fitpay.android.api.models;

import android.support.annotation.Nullable;

import com.fitpay.android.utils.StringUtils;

import java.util.List;

/**
 * Parsed API error response errorBody.
 * Used in {@link com.fitpay.android.api.callbacks.ApiCallbackExt#onFailure(ErrorResponse)}
 */
public class ErrorResponse {
    private int status;
    private long created;
    private String requestId;
    private String path;
    private String summary;
    private String description;
    private ErrorMessage details;
    private ErrorMessage message;

    public ErrorResponse(){
    }

    public ErrorResponse(int code, String message){
        status = code;
        description = message;
    }

    public int getStatus() {
        return status;
    }

    public long getCreated() {
        return created;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getPath() {
        return path;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public @Nullable String getDetails() {
        return details != null ? details.getMessage() : null;
    }

    public @Nullable String getMessage() {
        return message != null ? message.getMessage() : null;
    }

    public @Nullable String getError(){
        String error = getMessage();
        if(StringUtils.isEmpty(error)){
            error = getDescription();
        }
        return error;
    }

    @Override
    public String toString(){
        return getMessage();
    }

    public static class ErrorMessage {
        private List<ErrorMessageInfo> messages;

        public ErrorMessage(List<ErrorMessageInfo> messages){
            this.messages = messages;
        }

        String getMessage(){
            return messages != null && !messages.isEmpty() ? messages.get(0).message : null;
        }
    }

    public static class ErrorMessageInfo{
        private String message;
    }
}
