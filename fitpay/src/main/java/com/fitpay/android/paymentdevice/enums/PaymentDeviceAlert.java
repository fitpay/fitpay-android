package com.fitpay.android.paymentdevice.enums;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Payment device alerts enum
 */
public final class PaymentDeviceAlert {

    public static final String TRANSACTION = "Transaction";
    public static final String SECURITY = "Security";
    public static final String CONNECTION = "Connection";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TRANSACTION, SECURITY, CONNECTION})
    public @interface Type {
    }
}
