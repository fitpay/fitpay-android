package com.fitpay.android.paymentdevice.callbacks;

import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.utils.Listener;

/**
 * Connection callback
 */
public abstract class ConnectionListener extends Listener implements IListeners.ConnectionListener {
    public ConnectionListener() {
        this(null);
    }

    public ConnectionListener(String filter) {
        super(filter);
        mCommands.put(Connection.class, data -> {
            Connection event = (Connection) data;
            onDeviceStateChanged(event.getState());
        });
    }
}
