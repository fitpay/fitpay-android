package com.fitpay.android.webview;

import android.app.Activity;
import android.app.Fragment;
import android.webkit.JavascriptInterface;

import com.fitpay.android.cardscanner.IFitPayCardScanner;
import com.fitpay.android.paymentdevice.models.SyncInfo;

import org.json.JSONException;


public interface WebViewCommunicator {

    /**
     * this method is called by the WV to initiate sync() or sendUserData() indirectly
     */
    @JavascriptInterface
    void dispatchMessage(String message) throws JSONException;

    /**
     * this method is called by the WV to initiate the sync sequence in the SDK
     */
    @JavascriptInterface
    void sync(String callBackId);

    /**
     * Internal use only. this method is called by the WV to initiate the sync sequence in the SDK
     *
     * @param callBackId       js callback id
     * @param syncInfo sync notification data
     */
    void sync(String callBackId, final SyncInfo syncInfo);

    /**
     * this method is called by the WV to provide 'session data' (deviceID, userID, OAuth token) to the SDK
     */
    @JavascriptInterface
    void sendUserData(String callbackId, String deviceId, String token, String userId);

    /**
     * this method is called by the WV onLoad() to retrieve JSON object with host device and wearable data
     */
    @JavascriptInterface
    String retrieveConfigJson();

    /**
     * Provide a {@link IFitPayCardScanner} implementation to handle card image scanning within the native OS.  In order to enable
     * the "supportCardScanner" must be set to false in the {@link com.fitpay.android.configs.FitpayConfig.Web#supportCardScanner} when launching the
     * webview.
     *
     * @param cardScanner
     */
    void setCardScanner(IFitPayCardScanner cardScanner);

    /**
     * Called by the webview when the consumer requests a card scan operation and the {@link com.fitpay.android.configs.FitpayConfig.Web#supportCardScanner} is false
     *
     * * @param callbackId rtm callback id
     */
    void startScan(String callbackId);

    /**
     * Send "logout" message to JS
     */
    void logout();

    /**
     * Clear all. This method should be called manually in {@link Activity#onDestroy()} or {@link Fragment#onDestroyView()}
     */
    void destroy();

    /**
     * Send "back" message to JS
     * call this function in {@link Activity#onBackPressed()}
     */
    void onBackPressed();
}
