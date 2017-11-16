package com.fitpay.android.api.services;

import android.os.Build;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.utils.FPLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.internal.platform.Platform;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by tgs on 5/20/16.
 */
public class BaseClient {
    protected static final String FP_KEY_ID = "fp-key-id";
    protected static final String FP_KEY_SDK_VER = "X-FitPay-SDK";


    public static OkHttpClient.Builder getOkHttpClient() {
        OkHttpClient.Builder builder = getDefaultOkHttpClient();

        int connectTimeout = Integer.valueOf(ApiManager.getConfig().get(ApiManager.PROPERTY_HTTP_CONNECT_TIMEOUT));
        int readTimeout = Integer.valueOf(ApiManager.getConfig().get(ApiManager.PROPERTY_HTTP_READ_TIMEOUT));
        int writeTimeout = Integer.valueOf(ApiManager.getConfig().get(ApiManager.PROPERTY_HTTP_WRITE_TIMEOUT));

        builder = builder.connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true);

        if (FPLog.showHttpLogs()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            builder = builder.addInterceptor(logging);
        }

        return enableTls12OnPreLollipop(builder);
    }

    private static OkHttpClient.Builder getDefaultOkHttpClient() {
        return new OkHttpClient.Builder();
    }

    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                FPLog.i("pre lollipop ssl configuraiton being used");

                SSLContext sc = SSLContext.getDefault();
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), Platform.get().trustManager(sc.getSocketFactory()));

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
}
