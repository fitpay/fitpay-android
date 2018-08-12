package com.fitpay.android.webview.impl.webclients;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.RtmMessageResponse;
import com.fitpay.android.webview.models.RtmVersion;

/**
 * Default Fitpay web client.
 */
public class FitpayWebClient extends WebViewClient {

    private static final String TAG = FitpayWebClient.class.getSimpleName();

    protected final String connectorId;

    public FitpayWebClient(String connectorId) {
        this.connectorId = connectorId;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return handleUri(view, Uri.parse(url));
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return handleUri(view, request.getUrl());
    }

    /**
     * @param view {@link WebView}
     * @param uri  {@link Uri}
     * @return boolean
     */
    private boolean handleUri(@NonNull WebView view, @NonNull final Uri uri) {
        final String uriToString = uri.toString();
        FPLog.d(TAG, "handleUri\n" + uriToString);

        // Launch the dial action.
        if (uriToString.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);
            return true;
        }

        // If host is NOT FitPay, show an Android action intent so the URL is loaded into an Android browser app.
        else {
            String host = uri.getHost();
            if (host != null) {
                if (!host.contains("fit-pay.com")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        RxBus.getInstance().post(connectorId, new RtmMessageResponse(new RtmVersion(RtmType.RTM_VERSION), RtmType.VERSION));
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }
}