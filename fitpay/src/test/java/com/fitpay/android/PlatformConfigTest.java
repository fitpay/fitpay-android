package com.fitpay.android;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.utils.FPLog;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by ssteveli on 4/2/18.
 */

public class PlatformConfigTest {

    @BeforeClass
    public static void initApiManager() {
        FPLog.clean(); //in tests only one log impl should be used
        FPLog.addLogImpl(new FPLog.ILog() {
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
                return FPLog.DEBUG;
            }
        });
        FPLog.setShowHTTPLogs(false);
        TestConstants.configureFitpay();
    }

    @Test
    public void ensurePlatformConfigurationIsSetupCorrectly() {
        ApiManager api = ApiManager.getInstance();
        assertNotNull(api.getPlatformConfig());
    }
}
