package com.fitpay.android.webview.impl;

/**
 * @deprecated as of v1.0.3 - not being used
 * Created by Ross on 5/27/2016.
 */
@Deprecated
public class AppCallbackModel {

    private String command;
    private String status;
    private String reason;
    private String timestamp;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
