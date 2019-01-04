package com.fitpay.android.configs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.fitpay.android.a2averification.A2AContext;
import com.fitpay.android.a2averification.A2AIssuerResponse;
import com.fitpay.android.a2averification.A2AVerificationError;
import com.fitpay.android.a2averification.A2AVerificationFailed;
import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.api.models.Link;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.cardscanner.IFitPayCardScanner;
import com.fitpay.android.paymentdevice.interfaces.PaymentDeviceConnectable;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.Foreground;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.WebViewCommunicator;
import com.fitpay.android.webview.callbacks.A2AVerificationListener;
import com.fitpay.android.webview.callbacks.IdVerificationListener;
import com.fitpay.android.webview.callbacks.UserReceivedListener;
import com.fitpay.android.webview.enums.RelativeWebPath;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.events.UserReceived;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;
import com.fitpay.android.webview.impl.webclients.FitpayWebChromeClient;
import com.fitpay.android.webview.impl.webclients.FitpayWebClient;
import com.fitpay.android.webview.models.IdVerification;

/**
 * Main Object for interacting with Fitpay Web app
 */
public class FitpayWeb {

    private static volatile boolean wasInBackground = false;

    private Activity mActivity;

    private PaymentDeviceConnectable mConnector;

    private WebView mWebView;
    private WebViewCommunicator mCommunicator;

    private FitpayWebChromeClient mWebChromeClient;
    private FitpayWebClient mWebClient;

    private WvConfig mConfig;

    private UserReceivedListener userListener;
    private RtmMessageListener rtmListener;
    private IdVerificationListener idVerificationListener;
    private A2AVerificationListener a2ARequestListener;

    private RtmDelegate rtmDelegate;
    private IdVerificationDelegate idVerificationDelegate;
    private A2AVerificationDelegate a2AVerificationDelegate;

    private boolean loggedIn;

    /**
     * Base constructor.
     * It creates default communicator {@link WebViewCommunicatorImpl} without a2a support.}
     *
     * @param activity    activity
     * @param view        web view
     * @param connectable payment device connector
     */
    public FitpayWeb(@NonNull Activity activity, @NonNull WebView view, @NonNull PaymentDeviceConnectable connectable) {
        this(activity, view, new WebViewCommunicatorImpl(activity, connectable, view), connectable);
    }

    public FitpayWeb(@NonNull Activity activity, @NonNull WebView view, @NonNull WebViewCommunicatorImpl communicator, @NonNull PaymentDeviceConnectable connector) {
        mActivity = activity;

        mConnector = connector;
        mCommunicator = communicator;

        mWebChromeClient = getWebChromeClient(activity);
        mWebClient = getWebClient();

        mWebView = view;
        initializeWVDefaultSettings();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(mCommunicator, "Android");
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebClient);

        initialize();
    }

    /**
     * Call it on {@link Activity#onCreate(Bundle)} or {@link android.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * Could be skipped if you are going to create the object inside Activity, but after onCreate()
     */
    public void onCreate() {
        initialize();
    }

    /**
     * Call it on {@link Activity#onDestroy()} or {@link Fragment#onDestroyView()}
     */
    public void onDestroy() {
        Foreground.get(mActivity).removeListener(foregroundListener);

        NotificationManager.getInstance().removeListener(userListener);
        NotificationManager.getInstance().removeListener(rtmListener);
        NotificationManager.getInstance().removeListener(idVerificationListener);
        NotificationManager.getInstance().removeListener(a2ARequestListener);

        rtmListener = null;
        idVerificationListener = null;
        a2ARequestListener = null;

        mCommunicator.destroy();
    }

    /**
     * Call it on {@link Activity#onBackPressed()}
     */
    public void onBackPressed() {
        mCommunicator.onBackPressed();
    }

    /**
     * Call it on {@link Activity#onActivityResult(int, int, Intent)} ()}
     */
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        mWebChromeClient.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case Constants.INTENT_A2A_VERIFICATION_REQUEST: {
                if (intent == null || intent.getExtras() == null) {
                    A2AVerificationFailed failed = new A2AVerificationFailed(A2AVerificationError.UNKNOWN);
                    if (a2AVerificationDelegate != null) {
                        a2AVerificationDelegate.onA2AVerificationFailed(failed);
                    } else {
                        RxBus.getInstance().post(mConnector.id(), failed);
                    }
                } else if (resultCode == Activity.RESULT_OK) {
                    String authResponse = intent.getStringExtra(com.fitpay.android.utils.Constants.A2A_STEP_UP_AUTH_RESPONSE);
                    String authCode = intent.getStringExtra(com.fitpay.android.utils.Constants.A2A_STEP_UP_AUTH_CODE);
                    A2AIssuerResponse response = new A2AIssuerResponse(authResponse, authCode);
                    if (a2AVerificationDelegate != null) {
                        a2AVerificationDelegate.processA2AIssuerResponse(response);
                    } else {
                        load(response);
                    }
                } else {
                    A2AVerificationFailed failed = new A2AVerificationFailed(A2AVerificationError.DECLINED);
                    if (a2AVerificationDelegate != null) {
                        a2AVerificationDelegate.onA2AVerificationFailed(failed);
                    } else {
                        RxBus.getInstance().post(mConnector.id(), failed);
                    }
                }
                break;
            }
        }
    }

    /**
     * Call it on {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mWebChromeClient.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Get default {@link FitpayWebChromeClient} client.
     * Don't override unless you know what you are doing.
     *
     * @return Fitpay web chrome client
     */
    @NonNull
    public FitpayWebChromeClient getWebChromeClient(Activity activity) {
        return new FitpayWebChromeClient(activity);
    }

    /**
     * Get default {@link FitpayWebClient} client.
     * Don't override unless you know what you are doing.
     *
     * @return Fitpay web client
     */
    @NonNull
    public FitpayWebClient getWebClient() {
        return new FitpayWebClient(mConnector.id());
    }

    /**
     * Initial setup
     *
     * @param userEmail            user email
     * @param userHasFitpayAccount user has Fitpay account
     * @param accessToken          skips the pin screen if valid
     * @param device               payment device
     */
    public void setupWebView(@Nullable String userEmail, boolean userHasFitpayAccount, @Nullable String accessToken, @NonNull Device device) {
        mConfig = new WvConfig.Builder()
                .email(userEmail)
                .accountExist(userHasFitpayAccount)
                .clientId(FitpayConfig.clientId)
                .setCSSUrl(FitpayConfig.Web.cssURL)
                .redirectUri(FitpayConfig.redirectURL)
                .setBaseLanguageUrl(FitpayConfig.Web.baseLanguageURL)
                .setAccessToken(accessToken)
                .demoMode(FitpayConfig.Web.demoMode)
                .demoCardGroup(FitpayConfig.Web.demoCardGroup)
                .useWebCardScanner(!FitpayConfig.Web.supportCardScanner)
                .paymentDevice(new WvPaymentDeviceInfoSecure(device))
                .build();
    }

    /**
     * Loads the main page on Fitpay based on user variables
     */
    public void load() {
        Uri.Builder builder = new Uri.Builder()
                .encodedPath(FitpayConfig.webURL)
                .appendQueryParameter("config", mConfig.getEncodedString());
        loadUrl(builder.build().toString());
    }

    /**
     * @deprecated. Use {@link #loadUrl(String)} or {@link #loadLink(Link)}
     * Loads a specific page on Fitpay based on user variables.
     * Use default {@link RelativeWebPath.Value}
     *
     * @param relativePath relative path
     */
    @Deprecated
    public void load(@NonNull @RelativeWebPath.Value String relativePath) {
        Uri.Builder builder = new Uri.Builder()
                .encodedPath(FitpayConfig.webURL)
                .appendQueryParameter("config", mConfig.getEncodedString());

        if (!StringUtils.isEmpty(relativePath)) {
            builder.appendEncodedPath(relativePath);
        }

        loadUrl(builder.build().toString());
    }

    /**
     * Load url from A2A issuer response
     *
     * @param issuerResponse issuer response
     */
    public void load(@NonNull A2AIssuerResponse issuerResponse) {
        Uri.Builder builder = new Uri.Builder()
                .encodedPath(FitpayConfig.webURL)
                .appendQueryParameter("config", mConfig.getEncodedString())
                .appendEncodedPath(a2ARequestListener.getA2aReturnLocation())
                .appendQueryParameter("a2a", issuerResponse.getEncodedString());

        loadUrl(builder.build().toString());
    }

    /**
     * Loads any valid url - use with discretion
     *
     * @param url absolute url path
     */
    public void loadUrl(String url) {
        if (StringUtils.isEmpty(FitpayConfig.webURL)) {
            throw new NullPointerException("Fitpay config is not initialized");
        }
        mWebView.loadUrl(url);
    }

    /**
     * Loads valid web links
     *
     * @param link returned from User or CreditCard object
     */
    public void loadLink(Link link) {
        String url = link.getHref();

        if (link.getTemplated()) {
            url = url.replace("{config}", mConfig.getEncodedString());
        }

        mWebView.loadUrl(url);
    }

    /**
     * Get web communicator
     *
     * @return communicator
     */
    public WebViewCommunicator getCommunicator() {
        return mCommunicator;
    }

    /**
     * Get the config to construct a url on your own if needed
     *
     * @return config url
     */
    public String getConfig() {
        return mConfig.getEncodedString();
    }

    /**
     * Set rtm message delegate
     *
     * @param rtmDelegate rtm delegate
     */
    public void setRtmDelegate(RtmDelegate rtmDelegate) {
        this.rtmDelegate = rtmDelegate;
    }

    /**
     * Set id verification delegate
     *
     * @param idVerificationDelegate id verification delegate
     */
    public void setIdVerificationDelegate(@NonNull IdVerificationDelegate idVerificationDelegate) {
        this.idVerificationDelegate = idVerificationDelegate;
    }

    /**
     * Set a2a verification delegate
     *
     * @param a2AVerificationDelegate a2a verification delegate
     */
    public void setA2AVerificationDelegate(@NonNull A2AVerificationDelegate a2AVerificationDelegate) {
        this.a2AVerificationDelegate = a2AVerificationDelegate;
    }

    /**
     * Set custom card scanner
     *
     * @param cardScanner card scanner
     */
    public void setCardScannerDelegate(@NonNull IFitPayCardScanner cardScanner) {
        this.mCommunicator.setCardScanner(cardScanner);
    }

    /**
     * Could be overridden with custom values
     */
    public void initializeWVDefaultSettings() {
        mActivity.deleteDatabase("webview.db");
        mActivity.deleteDatabase("webviewCache.db");
        mWebView.clearCache(true);
        mWebView.setScrollbarFadingEnabled(true);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setDatabaseEnabled(false);
    }

    private void initialize() {
        Foreground.get(mActivity).addListener(foregroundListener);

        if (userListener == null) {
            NotificationManager.getInstance().addListener(userListener = new UserReceivedListener(mConnector.id()) {
                @Override
                public void onUserReceived(UserReceived data) {
                    loggedIn = true;
                }
            });
        }
        if (rtmListener == null) {
            NotificationManager.getInstance().addListener(rtmListener = new RtmMessageListener());
        }
        if (idVerificationListener == null) {
            NotificationManager.getInstance().addListener(idVerificationListener = new IdVerificationListener(mConnector.id()) {
                @Override
                public IdVerification getIdVerification() {
                    if (idVerificationDelegate != null) {
                        return idVerificationDelegate.getIdVerification();
                    } else {
                        return super.getIdVerification();
                    }
                }
            });
        }
        if (a2ARequestListener == null) {
            NotificationManager.getInstance().addListener(a2ARequestListener = new A2AVerificationListener(mConnector.id()) {
                @Override
                public void onRequestReceived(A2AVerificationRequest request) {
                    if (a2AVerificationDelegate != null) {
                        a2AVerificationDelegate.onRequestReceived(request);
                    } else {
                        A2AContext a2AContext = request.getContext();
                        Intent intent = new Intent();
                        intent.setAction(a2AContext.getApplicationId() + "." + a2AContext.getAction());
                        intent.putExtra(Intent.EXTRA_TEXT, a2AContext.getPayload());
                        intent.setPackage(a2AContext.getApplicationId());
                        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
                            mActivity.startActivityForResult(intent, Constants.INTENT_A2A_VERIFICATION_REQUEST);
                        } else {
                            RxBus.getInstance().post(mConnector.id(), new A2AVerificationFailed(A2AVerificationError.CANT_PROCESS));
                        }
                    }
                }
            });
        }
    }

    private final Foreground.Listener foregroundListener = new Foreground.Listener() {
        public void onBecameForeground() {
            if (wasInBackground) {
                wasInBackground = false;

                if (!mWebChromeClient.isTakingPhoto() && loggedIn) {
                    mCommunicator.logout();
                }

                mWebChromeClient.updateTakingPhotoStatus();
            }
        }

        public void onBecameBackground() {
            wasInBackground = true;
        }
    };

    private class RtmMessageListener extends Listener {
        RtmMessageListener() {
            super(mConnector.id());
            mCommands.put(RtmMessage.class, data -> {
                if (rtmDelegate != null) {
                    rtmDelegate.onMessage((RtmMessage) data);
                }
            });
        }
    }

    /**
     * Set the rtmDelegate to receive authorization and other messages from the webview
     */
    public interface RtmDelegate {
        void onMessage(RtmMessage rtmMessage);
    }

    /**
     * Set the idVerificationDelegate to handle response from {@link com.fitpay.android.webview.events.IdVerificationRequest}
     */
    public interface IdVerificationDelegate {
        IdVerification getIdVerification();
    }

    /**
     * Set the a2aVerificationDelegate to handle step up methods using the issuer app
     */
    public interface A2AVerificationDelegate {
        void onRequestReceived(A2AVerificationRequest verificationRequest);

        void processA2AIssuerResponse(A2AIssuerResponse successResponse);

        void onA2AVerificationFailed(A2AVerificationFailed failedResponse);
    }
}
