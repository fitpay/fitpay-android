package com.fitpay.android.paymentdevice.enums;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Apdu execution errors enum
 */
public class DeviceOperationError {

    public static final int NONE = -1;
    public static final int DEVICE_FAILED_TO_RESPOND = 1000;
    public static final int DEVICE_RESPONDED_WITH_INVALID_CONTENT = 1001;
    public static final int DEVICE_FAILED_TO_PROVIDE_REQUESTED_DATA = 1002;
    public static final int DEVICE_DENIED_THE_REQUEST = 1003;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, DEVICE_FAILED_TO_RESPOND, DEVICE_RESPONDED_WITH_INVALID_CONTENT, DEVICE_FAILED_TO_PROVIDE_REQUESTED_DATA, DEVICE_DENIED_THE_REQUEST})
    public @interface Reason {
    }

    private @Reason int reason;

    public DeviceOperationError(@Reason int reason) {
        this.reason = reason;
    }

    public @DeviceOperationError.Reason int getReason(){
        return reason;
    }
}
