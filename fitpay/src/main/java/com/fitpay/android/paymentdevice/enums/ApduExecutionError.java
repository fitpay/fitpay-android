package com.fitpay.android.paymentdevice.enums;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Apdu execution errors enum
 */
public class ApduExecutionError {

    public static final int CONTINUATION_ERROR = 0;
    public static final int ON_TIMEOUT = 1;
    public static final int WRONG_CHECKSUM = 2;
    public static final int WRONG_SEQUENCE = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONTINUATION_ERROR, ON_TIMEOUT, WRONG_CHECKSUM, WRONG_SEQUENCE})
    public @interface Reason {
    }

    private @Reason int reason;

    public ApduExecutionError(@Reason int reason) {
        this.reason = reason;
    }

    public @ApduExecutionError.Reason int getReason(){
        return reason;
    }
}
