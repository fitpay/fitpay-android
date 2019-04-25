package com.fitpay.android.paymentdevice.enums;

import androidx.annotation.IntDef;

import com.fitpay.android.paymentdevice.constants.States;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes action with SecureElement
 */
public final class SecureElement {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({States.POWER_OFF, States.POWER_ON, States.RESET})
    public @interface Action {
    }
}
