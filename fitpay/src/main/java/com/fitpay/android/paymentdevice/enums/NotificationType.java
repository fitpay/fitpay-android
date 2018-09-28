package com.fitpay.android.paymentdevice.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NotificationType {

    public final static String SYNC = "SYNC";
    public final static String CREDITCARD_DELETED = "CREDITCARD_DELETED";
    public final static String CREDITCARD_ACTIVATED = "CREDITCARD_ACTIVATED";
    public final static String CREDITCARD_REACTIVATED = "CREDITCARD_REACTIVATED";
    public final static String CREDITCARD_SUSPENDED = "CREDITCARD_SUSPENDED";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SYNC, CREDITCARD_ACTIVATED, CREDITCARD_REACTIVATED, CREDITCARD_SUSPENDED, CREDITCARD_DELETED})
    public @interface Value {
    }
}
