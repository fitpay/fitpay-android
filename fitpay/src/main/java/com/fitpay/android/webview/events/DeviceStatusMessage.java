package com.fitpay.android.webview.events;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Responses from other systems
 */
public class DeviceStatusMessage {

    public static final int ERROR = 0;
    public static final int SUCCESS = 1;
    public static final int PROGRESS = 2;
    public static final int PENDING = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ERROR, SUCCESS, PROGRESS, PENDING})
    public @interface Code {
    }

    private String message;
    private transient String deviceId;
    @Code
    private int type;

    public DeviceStatusMessage(String status, @Code int type) {
        this(status, null, type);
    }

    public DeviceStatusMessage(String status, String deviceId, @Code int type) {
        this.message = status;
        this.deviceId = deviceId;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DeviceStatusMessage{" +
                "message='" + message + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", type=" + type +
                '}';
    }
}
