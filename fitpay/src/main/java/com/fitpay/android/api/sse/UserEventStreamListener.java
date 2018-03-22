package com.fitpay.android.api.sse;

import com.fitpay.android.api.models.UserStreamEvent;
import com.fitpay.android.paymentdevice.callbacks.IListeners;
import com.fitpay.android.utils.Listener;
import com.google.gson.JsonObject;

/**
 * Synchronization callbacks
 */
public abstract class UserEventStreamListener extends Listener implements IListeners.UserEventStreamListener {
    public UserEventStreamListener() {
        super();
        mCommands.put(UserStreamEvent.class, event -> onUserEvent((UserStreamEvent) event));
    }
}
