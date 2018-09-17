package com.fitpay.android.api;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.a2averification.A2AVerificationTest;
import com.fitpay.android.utils.NamedResource;

import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by ssteveli on 4/2/18.
 */

public class PlatformConfigTest extends BaseTestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(PlatformConfigTest.class);

    @Test
    public void ensurePlatformConfigurationIsSetupCorrectly() {
        ApiManager api = ApiManager.getInstance();
        assertNotNull(api.getPlatformConfig());
    }
}
