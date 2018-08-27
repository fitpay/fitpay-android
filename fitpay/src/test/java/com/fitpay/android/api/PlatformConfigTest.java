package com.fitpay.android.api;

import android.content.Context;

import com.fitpay.android.TestConstants;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.utils.FPLog;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

/**
 * Created by ssteveli on 4/2/18.
 */

public class PlatformConfigTest {

    @BeforeClass
    public static void initApiManager() {
        TestConstants.configureFitpay(Mockito.mock(Context.class));
    }

    @Test
    public void ensurePlatformConfigurationIsSetupCorrectly() {
        ApiManager api = ApiManager.getInstance();
        assertNotNull(api.getPlatformConfig());
    }
}
