package com.fitpay.android.cardscanner;

import com.fitpay.android.utils.Constants;

import org.junit.Assert;
import org.junit.Test;

public class CardScannerTest {
    @Test
    public void scannerCardInfo(){
        String cardInfoStr = "{\"cardNumber\":\"376680406791017\", \"expiryMonth\":10, \"expiryYear\":2020, \"cvv\":123}";
        ScannedCardInfo cardInfo = Constants.getGson().fromJson(cardInfoStr, ScannedCardInfo.class);

        Assert.assertNotNull(cardInfo);
        Assert.assertEquals("376680406791017", cardInfo.getCardNumber());
        Assert.assertEquals(10, (int)cardInfo.getExpiryMonth());
        Assert.assertEquals(2020, (int)cardInfo.getExpiryYear());
        Assert.assertEquals("123", cardInfo.getCvv());
    }
}
