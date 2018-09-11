package com.fitpay.android;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.webview.models.IdVerification;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IdVerificationTest {

    @Test
    public void test01_emptySetsLocale() {
        IdVerification verificationResponse = new IdVerification.Builder().build();
        Assert.assertEquals(verificationResponse.getLocale(), "en-US");
    }

    @Test
    public void test02_verificationParsing() {
        IdVerification verificationResponse = Constants.getGson().fromJson("{\"suspendedCardsInAccount\": 1, \"daysSinceLastAccountActivity\": 6, \"deviceLostMode\": 7, \"deviceWithActiveTokens\": 2, \"activeTokenOnAllDevicesForAccount\": 3, \"accountScore\": 4, \"deviceScore\": 5, \"nfcCapable\": false, \"oemAccountCountryCode\": \"US\", \"deviceCountry\": \"US\", \"oemAccountUserName\": \"(someName)\", \"deviceTimeZone\": \"CST\", \"deviceTimeZoneSetBy\": 0, \"deviceIMEI\": \"123456\"}", IdVerification.class);
        Assert.assertNotNull(verificationResponse);
        Assert.assertEquals(verificationResponse.getDeviceCountry(), "US");
        Assert.assertNotNull(verificationResponse.getSuspendedCardsInOemAccount());
        Assert.assertEquals(verificationResponse.getSuspendedCardsInOemAccount().intValue(), 1);
        Assert.assertNotNull(verificationResponse.getDeviceLostModeDate());
        Assert.assertEquals(verificationResponse.getDeviceLostModeDate().intValue(), 7);
        Assert.assertEquals(verificationResponse.getDeviceIMEI(), "123456");
    }

    @Test
    public void test03_daysBetweenDatesTest() {
        Date date = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5));
        Date date2 = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(12));

        IdVerification verificationResponse = new IdVerification.Builder()
                .setDeviceLostModeDate(date)
                .setLastOemAccountActivityDate(date2)
                .build();

        Assert.assertNotNull(verificationResponse.getDeviceLostModeDate());
        Assert.assertEquals(verificationResponse.getDeviceLostModeDate().intValue(), 5);
        Assert.assertEquals(verificationResponse.getLastOemAccountActivityDate().intValue(), 12);
    }
}
