package com.fitpay.android.webview.models;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.TimestampUtils;
import com.fitpay.android.webview.enums.DeviceTimeZone;

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

        IdVerification verificationResponse = new IdVerification.Builder()
                .setDeviceLostModeDate(date)
                .build();
        Assert.assertNotNull(verificationResponse.getDeviceLostModeDate());
        Assert.assertEquals(verificationResponse.getDeviceLostModeDate().intValue(), 5);
    }

    @Test
    public void test04_fullComparison() {
        Date oemAccountInfoUpdatedDate = new Date(5000000);
        Date oemAccountCreatedDate = new Date(4000000);
        Integer suspendedCardsInOemAccount = 1;
        Date lastOemAccountActivityDate = new Date(3000000);
        Date deviceLostModeDate = new Date(2000000);
        Integer oemAccountScore = 9;
        Integer deviceScore = 8;
        Integer devicesWithIdenticalActiveToken = 2;
        Integer activeTokensOnAllDevicesForOemAccount = 3;
        boolean nfcCapable = true;
        String oemAccountCountryCode = "en-US";
        String deviceCountry = "US";
        String oemAccountUserName = "AccountUserName";
        Date devicePairedToOemAccountDate = new Date(1000000);
        String deviceTimeZone = "PDT";
        @DeviceTimeZone.SetBy Integer deviceTimeZoneSetBy = DeviceTimeZone.SET_BY_NETWORK;
        String deviceIMEI = "AABBCC123";

        IdVerification idVerification = new IdVerification.Builder()
                .setOemAccountInfoUpdatedDate(oemAccountInfoUpdatedDate)
                .setOemAccountCreatedDate(oemAccountCreatedDate)
                .setSuspendedCardsInOemAccount(suspendedCardsInOemAccount)
                .setLastOemAccountActivityDate(lastOemAccountActivityDate)
                .setDeviceLostModeDate(deviceLostModeDate)
                .setOemAccountScore(oemAccountScore)
                .setDevicesWithIdenticalActiveToken(devicesWithIdenticalActiveToken)
                .setActiveTokensOnAllDevicesForOemAccount(activeTokensOnAllDevicesForOemAccount)
                .setDeviceScore(deviceScore)
                .setNfcCapable(nfcCapable)
                .setOemAccountCountryCode(oemAccountCountryCode)
                .setDeviceCountry(deviceCountry)
                .setOemAccountUserName(oemAccountUserName)
                .setDevicePairedToOemAccountDate(devicePairedToOemAccountDate)
                .setDeviceTimeZone(deviceTimeZone)
                .setDeviceTimeZoneSetBy(deviceTimeZoneSetBy)
                .setDeviceIMEI(deviceIMEI)
                .build();

        Assert.assertEquals(idVerification.getOemAccountInfoUpdatedDate(), oemAccountInfoUpdatedDate);
        Assert.assertEquals(idVerification.getOemAccountCreatedDate(), oemAccountCreatedDate);
        Assert.assertEquals(idVerification.getSuspendedCardsInOemAccount(), suspendedCardsInOemAccount);
        Assert.assertEquals(idVerification.getLastOemAccountActivityDate(), TimestampUtils.getDaysBetweenDates(lastOemAccountActivityDate));
        Assert.assertEquals(idVerification.getDeviceLostModeDate(), TimestampUtils.getDaysBetweenDates(deviceLostModeDate));
        Assert.assertEquals(idVerification.getOemAccountScore(), oemAccountScore);
        Assert.assertEquals(idVerification.getDevicesWithIdenticalActiveToken(), devicesWithIdenticalActiveToken);
        Assert.assertEquals(idVerification.getActiveTokensOnAllDevicesForOemAccount(), activeTokensOnAllDevicesForOemAccount);
        Assert.assertEquals(idVerification.getDeviceScore(), deviceScore);
        Assert.assertEquals(idVerification.getNfcCapable(), nfcCapable);
        Assert.assertEquals(idVerification.getOemAccountCountryCode(), oemAccountCountryCode);
        Assert.assertEquals(idVerification.getDeviceCountry(), deviceCountry);
        Assert.assertEquals(idVerification.getOemAccountUserName(), oemAccountUserName);
        Assert.assertEquals(idVerification.getDevicePairedToOemAccountDate(), devicePairedToOemAccountDate);
        Assert.assertEquals(idVerification.getDeviceTimeZone(), deviceTimeZone);
        Assert.assertEquals(idVerification.getDeviceTimeZoneSetBy(), deviceTimeZoneSetBy);
        Assert.assertEquals(idVerification.getDeviceIMEI(), deviceIMEI);
    }
}
