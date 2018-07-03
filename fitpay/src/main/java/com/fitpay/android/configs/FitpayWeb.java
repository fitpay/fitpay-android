package com.fitpay.android.configs;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.fitpay.android.a2averification.A2AIssuerResponse;
import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.interfaces.PaymentDeviceConnectable;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.enums.RelativePath;
import com.fitpay.android.webview.events.IdVerificationRequest;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.impl.FitpayWebClient;
import com.fitpay.android.webview.impl.WebRelativePath;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;

/**
 * Main Object for interacting with Fitpay Web app
 */
public class FitpayWeb {

    private PaymentDeviceConnectable mConnector;

    private WebView mWebView;
    private WebViewCommunicatorImpl mCommunicator;

    private WvConfig mConfig;

    private RtmMessageListener rtmListner;
    private IdVerificationListener idVerificationListener;
    private A2ARequestListener a2ARequestListener;

    private RtmDelegate rtmDelegate;
    private IdVerificationDelegate idVerificationDelegate;
    private A2AVerificationDelegate a2AVerificationDelegate;

    /**
     * Base constructor.
     * It creates default communicator {@link WebViewCommunicatorImpl} without a2a support. See {@link WebViewCommunicatorImpl#supportsAppVerification()}
     *
     * @param activity activity
     * @param view web view
     * @param connectable payment device connector
     */
    public FitpayWeb(@NonNull Activity activity, @NonNull WebView view, @NonNull PaymentDeviceConnectable connectable) {
        this(view, new WebViewCommunicatorImpl(activity, connectable, view), connectable);
    }

    public FitpayWeb(@NonNull WebView view, @NonNull WebViewCommunicatorImpl communicator, @NonNull PaymentDeviceConnectable connector) {
        mConnector = connector;
        mCommunicator = communicator;
        mWebView = view;
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(mCommunicator, "Android");
        mWebView.setWebViewClient(getWebClient());

        initialize();
    }

    private void initialize() {
        if (mCommunicator != null) {
            if (rtmListner == null) {
                NotificationManager.getInstance().addListener(rtmListner = new RtmMessageListener());
            }
            if (idVerificationListener == null) {
                NotificationManager.getInstance().addListener(idVerificationListener = new IdVerificationListener());
            }
            if (a2ARequestListener == null) {
                NotificationManager.getInstance().addListener(a2ARequestListener = new A2ARequestListener());
            }
        }
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
        NotificationManager.getInstance().removeListener(rtmListner);
        NotificationManager.getInstance().removeListener(idVerificationListener);
        NotificationManager.getInstance().removeListener(a2ARequestListener);

        rtmListner = null;
        idVerificationListener = null;
        a2ARequestListener = null;
    }

    /**
     * Get default {@link FitpayWebClient} client.
     * Don't override unless you need custom client.
     *
     * @return Fitpay web client
     */
    public FitpayWebClient getWebClient() {
        return new FitpayWebClient(mConnector.id());
    }

    /**
     * Initial setup
     *
     * @param userEmail            user email
     * @param userHasFitpayAccount user has Fitpay account
     * @param device               payment device
     */
    public void setupWebView(@NonNull String userEmail, boolean userHasFitpayAccount, @NonNull Device device) {
        mConfig = new WvConfig.Builder()
                .email(userEmail)
                .accountExist(userHasFitpayAccount)
                .version(FitpayConfig.Web.version)
                .clientId(FitpayConfig.clientId)
                .setCSSUrl(FitpayConfig.Web.cssURL)
                .demoMode(FitpayConfig.Web.demoMode)
                .demoCardGroup(FitpayConfig.Web.demoCardGroup)
                .useWebCardScanner(FitpayConfig.Web.supportCardScanner) //we are going to use CardIO card scanner
                .paymentDevice(new WvPaymentDeviceInfoSecure(device))
                .build();
    }

    /**
     * Loads the main page on Fitpay based on user variables
     */
    public void load() {
        load(WebRelativePath.PAGE_DEFAULT);
    }

    /**
     * Loads a specific page on Fitpay based on user variables.
     * Use default {@link WebRelativePath} or your own inherited from {@link RelativePath}
     *
     * @param relativePath relative path
     */
    public void load(@NonNull RelativePath relativePath) {
        Uri.Builder builder = new Uri.Builder()
                .encodedPath(FitpayConfig.webURL)
                .appendQueryParameter("config", mConfig.getEncodedString());

        String pathValue = relativePath.valueOf();
        if (!StringUtils.isEmpty(pathValue)) {
            builder.appendEncodedPath(pathValue);
        }

        mWebView.loadUrl(builder.build().toString());
    }

    /**
     * Loads any valid url - use with discretion
     *
     * @param absolutePath absolute url path
     */
    public void load(@NonNull String absolutePath) {
        mWebView.loadUrl(absolutePath);
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
                .appendEncodedPath(mCommunicator.getA2aReturnLocation())
                .appendQueryParameter("a2a", issuerResponse.getEncodedString());

        load(builder.build().toString());
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
    public void setIdVerificationDelegate(IdVerificationDelegate idVerificationDelegate) {
        this.idVerificationDelegate = idVerificationDelegate;
    }

    /**
     * Set a2a verification delegate
     *
     * @param a2AVerificationDelegate a2a verification delegate
     */
    public void setA2AVerificationDelegate(A2AVerificationDelegate a2AVerificationDelegate) {
        this.a2AVerificationDelegate = a2AVerificationDelegate;
    }

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

    private class IdVerificationListener extends Listener {
        IdVerificationListener() {
            super(mConnector.id());
            mCommands.put(IdVerificationRequest.class, data -> {
                if (idVerificationDelegate != null) {
                    idVerificationDelegate.onRequestReceived((IdVerificationRequest) data);
                }
            });
        }
    }

    private class A2ARequestListener extends Listener {
        A2ARequestListener() {
            super(mConnector.id());
            mCommands.put(A2AVerificationRequest.class, data -> {
                if (a2AVerificationDelegate != null) {
                    a2AVerificationDelegate.onRequestReceived(((A2AVerificationRequest) data));
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
     * Set the idVerificationDelegate to handle id verification request
     */
    public interface IdVerificationDelegate {
        void onRequestReceived(IdVerificationRequest request);
    }

    /**
     * Set the a2aVerificationDelegate to handle step up methods using the issuer app
     */
    public interface A2AVerificationDelegate {
        void onRequestReceived(A2AVerificationRequest request);
    }
}
