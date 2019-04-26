package com.fitpay.android.paymentdevice.enums;

import androidx.annotation.IntDef;

import com.fitpay.android.paymentdevice.constants.States;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Payment device connection states enum
 */
public final class Connection {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({States.NEW, States.DISCONNECTED, States.CONNECTING, States.CONNECTED, States.DISCONNECTING, States.INITIALIZED})
    public @interface State {
    }

    @State
    private int state;

    public Connection(@State int state) {
        this.state = state;
    }

    @Connection.State
    public int getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "state=" + state +
                '}';
    }
}
