package com.fitpay.android.api.models;

import java.util.List;

/**
 * Parsed error response from API errorBody
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

    public String getDetails() {
        return details != null ? details.getMessage() : "";
    }

    public String getMessage() {
        return message != null ? message.getMessage() : "";
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
            return messages != null && !messages.isEmpty() ? messages.get(0).message : "";
        }
    }

    public static class ErrorMessageInfo{
        private String message;
    }
}
