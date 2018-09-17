package com.fitpay.android.configs;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.TestConstants;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.NamedResource;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.InputStream;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FitpayConfigTest extends BaseTestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(FitpayConfigTest.class);

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
    public void test03_readFromFile() {
        String fileName = "fitpay_config.json";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        FitpayConfig.configure(mContext, inputStream);
        Assert.assertEquals("https://webapp.fit-pay.com", FitpayConfig.webURL);
        Assert.assertEquals("https://webapp.fit-pay.com", FitpayConfig.redirectURL);
        Assert.assertEquals("https://api.fit-pay.com", FitpayConfig.apiURL);
        Assert.assertEquals("https://auth.fit-pay.com", FitpayConfig.authURL);
        Assert.assertEquals("https://fitpaycss.github.io/pagare.css", FitpayConfig.Web.cssURL);
        Assert.assertTrue(FitpayConfig.supportApp2App);
        Assert.assertNotNull("demoCardGroup is missing", FitpayConfig.Web.demoCardGroup);
        Assert.assertEquals("demoCardGroup mismatch", FitpayConfig.Web.demoCardGroup, "visa_only");
    }
}
