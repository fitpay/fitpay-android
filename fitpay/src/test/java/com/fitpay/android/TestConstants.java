package com.fitpay.android;

import android.content.Context;

import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.StringUtils;

import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Test constants
 */
public final class TestConstants {
    private final static String PROPERTY_API_BASE_URL = "apiBaseUrl";
    private final static String PROPERTY_AUTH_BASE_URL = "authBaseUrl";
    private final static String PROPERTY_CLIENT_ID = "clientId";
    private final static String PROPERTY_REDIRECT_URL = "redirectUrl";

    public static TestConfig testConfig;

    public static String getClientId() {
        return System.getProperty(PROPERTY_CLIENT_ID, "fp_webapp_pJkVp2Rl");
    }

    public static void configureFitpay(Context context) {
        initTestConfig(context.getClass());

        addLogs();

        Mockito.when(context.getCacheDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("fitpay_config.json");
        FitpayConfig.configure(context, inputStream);
        FitpayConfig.apiURL = System.getProperty(PROPERTY_API_BASE_URL, FitpayConfig.apiURL);
        FitpayConfig.authURL = System.getProperty(PROPERTY_AUTH_BASE_URL, FitpayConfig.authURL);
        FitpayConfig.redirectURL = System.getProperty(PROPERTY_REDIRECT_URL, FitpayConfig.redirectURL);
    }

    private static void initTestConfig(Class clazz) {
        testConfig = TestConfig.init(clazz);
        String realTests = System.getenv("USE_REAL_TESTS");
        if (!StringUtils.isEmpty(realTests)) {
            testConfig.useRealTests = Boolean.valueOf(realTests);
        }

        if (!testConfig.useRealTests) {
            prepareTests(clazz);
        }
    }

    public static void waitForAction() throws InterruptedException {
        waitForAction(1000);
    }

    public static void waitForAction(long delay) throws InterruptedException {
        Thread.sleep(testConfig.useRealTests ? delay : 10);
    }

    public static void addLogs() {
        FPLog.clean(); //in tests only one log impl should be used
        FPLog.addLogImpl(new FPLog.ILog() {
            @Override
            public void v(String tag, String text) {
                System.out.println(tag + " VERBOSE (" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void d(String tag, String text) {
                System.out.println(tag + " DEBUG (" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void i(String tag, String text) {
                System.out.println(tag + " INFO(" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void w(String tag, String text) {
                System.out.println(tag + " WARN(" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void e(String tag, Throwable throwable) {
                System.out.println(tag + " ERROR (" + Thread.currentThread().getName() + "): " + tag);

                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }

            @Override
            public int logLevel() {
                return testConfig.logLevel;
            }
        });
        FPLog.setShowHTTPLogs(testConfig.showHTTPLogs);
    }

    private static void prepareTests(Class clazz) {
        File testDir = new File(TestConfig.TESTS_FOLDER.concat(testConfig.testsVersion).concat(File.separator));
        if (!testDir.exists()) {
            try {
                Files.walk(FileSystems.getDefault().getPath(TestConfig.TESTS_FOLDER))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }

            testDir.mkdirs();

            try {
                decompressZipTests(testDir, clazz);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void decompressZipTests(File file, Class clazz) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(clazz.getClassLoader().getResourceAsStream("tests.zip"));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(file.getAbsolutePath().concat(File.separator).concat(fileName));
            if (zipEntry.isDirectory()) {
                if (!newFile.exists()) {
                    newFile.mkdir();
                }
            } else {
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }
}
