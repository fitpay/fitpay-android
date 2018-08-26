package com.fitpay.android.api.services;

import android.os.Build;

import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.FPLog;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by tgs on 5/20/16.
 */
public class BaseClient {
    protected static final String TAG = "API_REQUEST";

    protected static final String FP_KEY_ID = "fp-key-id";
    protected static final String FP_KEY_SDK_VER = "X-FitPay-SDK";

    protected static final int HTTP_CONNECT_TIMEOUT = 60;
    protected static final int HTTP_READ_TIMEOUT = 60;
    protected static final int HTTP_WRITE_TIMEOUT = 60;

    public static OkHttpClient.Builder getOkHttpClient() {
        return getOkHttpClient(FPLog.showHttpLogs());
    }

    public static OkHttpClient.Builder getOkHttpClient(boolean enabledLogging) {
        OkHttpClient.Builder builder = getDefaultOkHttpClient();

        builder = builder.connectTimeout(HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true);

        if (enabledLogging) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(BaseClient::printLog);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            builder = builder.addInterceptor(logging);
        }

        return enableTls12OnPreLollipop(builder);
    }

    private static OkHttpClient.Builder getDefaultOkHttpClient() {
        int cacheSize = 20 * 1024 * 1024; // 20MB
        return new OkHttpClient.Builder();
                //.cache(new Cache(FitpayConfig.appContext.getCacheDir(), cacheSize));
    }

    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                FPLog.i("pre lollipop ssl configuration being used");

                // pulled from {@link OkHttpClient} javadoc in finding the trustmanager, which isn't really exposed!
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
                }

                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                SSLContext sc = SSLContext.getDefault();

                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), trustManager);

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                FPLog.e("Error while setting up TLS 1.2 support on a pre-lollipop device, SDK " + Build.VERSION.SDK_INT, exc);

                throw new RuntimeException(exc);
            }
        }

        return client;
    }

    protected static void
     printLog(String message){
        if(FPLog.showHttpLogs()){
            FPLog.v(TAG, message);
        }
    }
}
