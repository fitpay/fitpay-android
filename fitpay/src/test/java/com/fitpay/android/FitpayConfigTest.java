package com.fitpay.android;

import android.content.Context;
import android.content.res.AssetManager;

import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.Constants;

import junit.framework.Assert;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FitpayConfigTest extends TestActions {
    @Test
    public void test01_checkDefaultApiUrl() {
        Assert.assertEquals("api base url mismatch", FitpayConfig.apiURL, Constants.CONFIG_API_BASE_URL);
    }

    @Test
    public void test02_checkClientIdOverride() {
        Context context = Mockito.mock(Context.class);
        Assert.assertEquals("clientId mismatch", FitpayConfig.clientId, TestConstants.getClientId());
        FitpayConfig.configure(context, "newClientIdString");
        Assert.assertEquals("clientId mismatch", FitpayConfig.clientId, "newClientIdString");
        FitpayConfig.configure(context, TestConstants.getClientId()); //restore correct value
    }

    @Test
    public void test03_readFromFile() throws IOException {
        String fileName = "fitpayconfig.json";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        Context context = Mockito.mock(Context.class);
        FitpayConfig.configure(context, inputStream);
        Assert.assertNotNull("demoCardGroup is missing", FitpayConfig.Web.demoCardGroup);
        Assert.assertEquals("demoCardGroup mismatch", FitpayConfig.Web.demoCardGroup, "visa_only");
    }
}
