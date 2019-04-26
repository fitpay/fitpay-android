package com.fitpay.android.paymentdevice.enums;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Apdu execution errors enum
 */
public class CommitResult {

    public static final int SUCCESS = 0;
    public static final int SKIPPED = 1;
    public static final int FAILED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SUCCESS, SKIPPED, FAILED})
    public @interface Type {
    }

    @Type
    private int type;

    public CommitResult(@Type int type) {
        this.type = type;
    }

    @CommitResult.Type
    public int getType() {
        return type;
    }
}
