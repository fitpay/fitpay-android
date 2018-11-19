package com.fitpay.android.api.models.card;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CreditCardInfoTests {

    @Test
    public void testCreditCardInfoParsing() {
        Integer expYear = 2018;
        Integer expMonth = 10;

        CreditCardInfo creditCardInfo = getTestCreditCardInfo();
        assertEquals("9999504454545450", creditCardInfo.getPan());
        assertEquals("TEST CARD", creditCardInfo.getName());
        assertEquals(expYear, creditCardInfo.getExpYear());
        assertEquals(expMonth, creditCardInfo.getExpMonth());
        assertEquals("de", creditCardInfo.getLanguage());
        assertEquals("133", creditCardInfo.getCVV());

        assertEquals("Boulder", creditCardInfo.getAddress().getCity());
        assertEquals("CO", creditCardInfo.getAddress().getState());
        assertEquals("80302", creditCardInfo.getAddress().getPostalCode());
        assertEquals("US", creditCardInfo.getAddress().getCountryCode());
        assertEquals("1035 Pearl St", creditCardInfo.getAddress().getStreet1());

        assertNull(creditCardInfo.getAddress().getStreet2());
        assertNull(creditCardInfo.getAddress().getStreet3());
    }

    private CreditCardInfo getTestCreditCardInfo() {
        String pan = "9999504454545450";
        String cardName = "TEST CARD";
        int expYear = 2018;
        int expMonth = 10;
        String language = "de";
        String cvv = "133";

        String city = "Boulder";
        String state = "CO";
        String postalCode = "80302";
        String countryCode = "US";
        String street1 = "1035 Pearl St";

        Address address = new Address();
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountryCode(countryCode);
        address.setStreet1(street1);

        return new CreditCardInfo.Builder()
                .setCVV(cvv)
                .setPAN(pan)
                .setExpDate(expYear, expMonth)
                .setAddress(address)
                .setLanguage(language)
                .setName(cardName)
                .build();
    }
}
