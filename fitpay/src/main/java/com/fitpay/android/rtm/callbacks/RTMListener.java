package com.fitpay.android.rtm.callbacks;

import com.fitpay.android.rtm.models.WebViewSessionData;

/**
 * @deprecated as of v1.0.3 - not being used
 * RTM callback listener
 */
@Deprecated
public interface RTMListener {
    void onConnect();

    void onError(String message);

    void onUserLogin(WebViewSessionData sessionData);

    void onSynchronizationRequest();

    void onSynchronizationComplete();
}
