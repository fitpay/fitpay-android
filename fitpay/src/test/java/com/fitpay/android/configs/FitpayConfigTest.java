package com.fitpay.android.configs;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestConstants;
import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.Constants;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.InputStream;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FitpayConfigTest extends TestActions {

    @Override
    @Before
    public void before(){}

    @Test
    public void test01_checkDefaultApiUrl() {
        Assert.assertEquals("api base url mismatch", FitpayConfig.apiURL, Constants.CONFIG_API_BASE_URL);
    }

    @Test
    public void test02_checkClientIdOverride() {
        Assert.assertEquals("clientId mismatch", FitpayConfig.clientId, TestConstants.getClientId());
        FitpayConfig.configure(mContext, "newClientIdString");
        Assert.assertEquals("clientId mismatch", FitpayConfig.clientId, "newClientIdString");
        FitpayConfig.configure(mContext, TestConstants.getClientId()); //restore correct value
    }

    @Test
    public void test03_readFromFile() throws IOException {
        String fileName = "fitpayconfig.json";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        FitpayConfig.configure(mContext, inputStream);
        Assert.assertNotNull("demoCardGroup is missing", FitpayConfig.Web.demoCardGroup);
        Assert.assertEquals("demoCardGroup mismatch", FitpayConfig.Web.demoCardGroup, "visa_only");
    }
}
