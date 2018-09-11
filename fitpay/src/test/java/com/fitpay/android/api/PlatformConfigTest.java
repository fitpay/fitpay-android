package com.fitpay.android.api;

import com.fitpay.android.BaseTestActions;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by ssteveli on 4/2/18.
 */

public class PlatformConfigTest extends BaseTestActions {

    @Test
    public void ensurePlatformConfigurationIsSetupCorrectly() {
        ApiManager api = ApiManager.getInstance();
        assertNotNull(api.getPlatformConfig());
    }
}
