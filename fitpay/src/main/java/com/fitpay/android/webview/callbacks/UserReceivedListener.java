package com.fitpay.android.webview.callbacks;

import com.fitpay.android.utils.Listener;
import com.fitpay.android.webview.events.UserReceived;

/**
 * Listen to {@link UserReceived}
 */
public abstract class UserReceivedListener extends Listener {
    public UserReceivedListener(String connectorId) {
        super(connectorId);
        mCommands.put(UserReceived.class, data -> onUserReceived((UserReceived) data));
    }

    public abstract void onUserReceived(UserReceived data);
}