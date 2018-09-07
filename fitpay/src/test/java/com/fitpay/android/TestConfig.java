package com.fitpay.android;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Test config data
 */
public class TestConfig {
    public static final String TESTS_FOLDER;

    static {
        TESTS_FOLDER = System.getProperty("java.io.tmpdir").concat("Fitpay").concat(File.separator);
    }

    @FPLog.LogLevel
    int logLevel = FPLog.DEBUG;
    boolean showHTTPLogs = false;
    boolean useRealTests = false;
    boolean saveRealTests = false;
    String testsVersion = "0.0.1";

    @FPLog.LogLevel
    public int getLogLevel() {
        return logLevel;
    }

    public boolean showHTTPLogs() {
        return showHTTPLogs;
    }

    public boolean useRealTests() {
        return useRealTests;
    }

    public boolean saveRealTests() {
        return saveRealTests;
    }

    public String testsVersion() {
        return testsVersion;
    }

    public static TestConfig init(Class clazz) {
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream("test_config.json");
        TestConfig configModel = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            configModel = Constants.getGson().fromJson(in, TestConfig.class);
        } catch (IOException e) {
            configModel = new TestConfig();
        }

        return configModel;
    }
}
