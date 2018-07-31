package com.fitpay.android;

import android.content.Context;

import com.fitpay.android.configs.FitpayConfig;

import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import okhttp3.Cache;

/**
 * Test constants
 */
public final class TestConstants {
    private final static String PROPERTY_API_BASE_URL = "apiBaseUrl";
    private final static String PROPERTY_AUTH_BASE_URL = "authBaseUrl";
    private final static String PROPERTY_CLIENT_ID = "clientId";
    private final static String PROPROPERTY_REDIRECT_URLPERTY_CLIENT_ID = "redirectUrl";

    static String getClientId() {
        return System.getProperty(PROPERTY_CLIENT_ID, "fp_webapp_pJkVp2Rl");
    }

    static void configureFitpay() {
        final String tempFilePath = System.getProperty("java.io.tmpdir") + "fitpay_tests_cache";
        final File tmpDirectory = new File(tempFilePath);
        try {
            deleteDirectoryRecursion(tmpDirectory.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        tmpDirectory.mkdir();

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getCacheDir()).thenReturn(tmpDirectory);

        FitpayConfig.configure(context, getClientId());
        FitpayConfig.apiURL = System.getProperty(PROPERTY_API_BASE_URL, "https://api.fit-pay.com");
        FitpayConfig.authURL = System.getProperty(PROPERTY_AUTH_BASE_URL, "https://auth.fit-pay.com");
        FitpayConfig.redirectURL = System.getProperty(PROPROPERTY_REDIRECT_URLPERTY_CLIENT_ID, "https://webapp.fit-pay.com");
    }

    static void waitSomeActionsOnServer() throws InterruptedException {
        Thread.sleep(1000);
    }

    static void deleteDirectoryRecursion(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        Files.delete(path);
    }
}
