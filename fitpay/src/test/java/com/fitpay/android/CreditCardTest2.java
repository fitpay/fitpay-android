package com.fitpay.android;

import android.media.Image;

import com.fitpay.android.api.callbacks.ResultProvidingCallback;
import com.fitpay.android.api.enums.CardInitiators;
import com.fitpay.android.api.models.Transaction;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.Reason;
import com.fitpay.android.api.models.card.VerificationMethod;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Device;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


/**
 * Created by tgs on 4/21/16.
 */
public class CreditCardTest2 extends TestActions {

    @Test
    public void testCanAddCreditCard() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);

        verifyCardContents(creditCard, createdCard);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Image> callback = new ResultProvidingCallback<>(latch);
        createdCard.getCardMetaData().getBrandLogo().get(0).self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        //TODO enable test for image retrieval
        //assertEquals(-1, callback.getErrorCode());
    }

    protected void verifyCardContents(CreditCard creditCard, CreditCard createdCard) {
        assertNotNull("card not created",createdCard);
        assertEquals("cvv should be masked", "###", createdCard.getCVV());
        assertEquals("exp month", creditCard.getExpMonth(), createdCard.getExpMonth());
        assertEquals("exp year", creditCard.getExpYear(), createdCard.getExpYear());
        assertEquals("street 1", creditCard.getAddress().getStreet1(), createdCard.getAddress().getStreet1());
        assertEquals("postal code", creditCard.getAddress().getPostalCode(), createdCard.getAddress().getPostalCode());
        assertNotNull("card meta data should be populated", createdCard.getCardMetaData());
        assertNotNull("brand logo should be populated", createdCard.getCardMetaData().getBrandLogo());
        assertTrue("brand logo should have at least one asset", createdCard.getCardMetaData().getBrandLogo().size() > 0);
        assertEquals("first brand logo mime type", "image/png", createdCard.getCardMetaData().getBrandLogo().get(0).getMimeType());
    }

    @Test
    public void testCantAddCreditCardWithNoDevice() throws Exception {
        String pan = "9999545454545454";
        CreditCard creditCard = getTestCreditCard(pan);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        user.createCreditCard(creditCard, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        CreditCard createdCard = callback.getResult();

        assertNull("created card",createdCard);
        assertEquals("error code", 400, callback.getErrorCode());
    }

    @Test
    public void testCanAcceptTerms() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999545454545454";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);

        assertNotNull("card not successfully updated by accept terms", createdCard);
        assertEquals("card state", "PENDING_VERIFICATION", createdCard.getState());

    }

    @Test
    public void testCanDeclineTerms() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999545454545454";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = declineTerms(createdCard);

        assertNotNull("card not successfully updated by decline terms", createdCard);
        assertEquals("card state", "DECLINED_TERMS_AND_CONDITIONS", createdCard.getState());

    }

    @Test
    @Ignore //TODO Looks like changes for last digit 7 (ineligible) have not been deployed to demo
    public void testCantAcceptTermsOnIneligibleCard() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545457";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "INELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);

        assertNotNull("card not successfully updated by accept terms", createdCard);
        assertEquals("card state", "DECLINED", createdCard.getState());

    }

    @Test
    public void testAcceptTermsOnDeclinedCardDoesNothing() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545459";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);

        assertNotNull("card not successfully updated by accept terms", createdCard);
        assertEquals("card state", "DECLINED", createdCard.getState());

        createdCard = acceptTerms(createdCard);
        assertNull("no result expected", createdCard);

    }

    @Test
    public void testCanGetCards1() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);

        Collections.CreditCardCollection creditCards = getCreditCards(user);
        assertNotNull("credit cards collection", creditCards);
        assertEquals("number of credit cards", 1, creditCards.getTotalResults());
        assertEquals("credit card connectorId", createdCard.getCreditCardId(), creditCards.getResults().get(0).getCreditCardId());

        verifyCardContents(creditCard, creditCards.getResults().get(0));
    }

    @Test
    public void testCanGetCards2() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);
        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);

        pan = "9999504454545451";
        creditCard = getTestCreditCard(pan);
        createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);

        Collections.CreditCardCollection creditCards = getCreditCards(user);
        assertNotNull("credit cards collection", creditCards);
        assertEquals("number of credit cards", 2, creditCards.getTotalResults());
        assertTrue("credit card connectorId", createdCard.getCreditCardId().equals(creditCards.getResults().get(0).getCreditCardId())
            || createdCard.getCreditCardId().equals(creditCards.getResults().get(1).getCreditCardId()));

    }

    @Test
    public void testCanDeleteFromCollection() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);
        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);

        pan = "9999504454545451";
        creditCard = getTestCreditCard(pan);
        createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);

        Collections.CreditCardCollection creditCards = getCreditCards(user);
        assertNotNull("credit cards collection", creditCards);
        assertEquals("number of credit cards", 2, creditCards.getTotalResults());

        deleteCard(createdCard);

        creditCards = getCreditCards(user);
        assertNotNull("credit cards collection", creditCards);
        assertEquals("number of credit cards", 1, creditCards.getTotalResults());

    }

    @Test
    public void testDeleteCard() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);
        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);

        deleteCard(createdCard);

        Collections.CreditCardCollection creditCards = getCreditCards(user);
        assertNotNull("credit cards collection", creditCards);
        assertEquals("number of credit cards", 0, creditCards.getTotalResults());

    }

    @Test
    public void testCanVerifyAndDeactivate() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999545454545454";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);

        assertNotNull("card not successfully updated by decline terms", createdCard);
        assertEquals("card state", "PENDING_VERIFICATION", createdCard.getState());
        assertTrue("no verification methods", createdCard.getVerificationMethods().size() > 0);

        assertEquals("verification state", "AVAILABLE_FOR_SELECTION", createdCard.getVerificationMethods().get(0).getState());

        VerificationMethod method = selectVerificationMethod(createdCard.getVerificationMethods().get(0));

        assertEquals("verification state after selection", "AWAITING_VERIFICATION", method.getState());

        CreditCard retrievedCard = getCreditCard(createdCard);
        assertEquals("number of verification methods", createdCard.getVerificationMethods().size(), retrievedCard.getVerificationMethods().size());
        VerificationMethod selectedMethod = null;
        for (VerificationMethod m : retrievedCard.getVerificationMethods()) {
            if (m.getMethodType().equals(method.getMethodType()) && m.getState().equals("AWAITING_VERIFICATION")) {
                selectedMethod = m;
                break;
            }
        }
        assertNotNull("No selected method found", selectedMethod);

        selectedMethod = verifyVerificationMethod(selectedMethod, "12345");
        assertEquals("post verification state", "VERIFIED", selectedMethod.getState());

        retrievedCard = waitForActivation(retrievedCard);

        assertEquals("post verification card state", "ACTIVE", retrievedCard.getState());

        Reason reason = new Reason();
        reason.setCausedBy(CardInitiators.INITIATOR_CARDHOLDER);
        reason.setReason("tired of racking up miles");
        retrievedCard = deactivateCard(retrievedCard, reason);

        assertEquals("post deactivation card state", "DEACTIVATED", retrievedCard.getState());

        retrievedCard = getCreditCard(retrievedCard);
        assertEquals("post deactivation card state", "DEACTIVATED", retrievedCard.getState());

        retrievedCard = reactivateCard(retrievedCard, reason);
        assertEquals("post verification card state", "ACTIVE", retrievedCard.getState());
        retrievedCard = getCreditCard(retrievedCard);
        assertEquals("post reactivation card state", "ACTIVE", retrievedCard.getState());

    }

    @Test
    public void canMakeDefault() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999545454545450";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);

        createdCard = waitForActivation(createdCard);

        assertEquals("post deactivation card state", "ACTIVE", createdCard.getState());
        assertTrue("should be default", createdCard.isDefault());

        pan = "9999504454545451";
        creditCard = getTestCreditCard(pan);
        CreditCard secondCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", secondCard);

        assertEquals("card not in expected state", "ELIGIBLE", secondCard.getState());

        secondCard = acceptTerms(secondCard);
        secondCard = waitForActivation(secondCard);

        assertEquals("post deactivation card state", "ACTIVE", secondCard.getState());
        assertFalse("second card should not be default", secondCard.isDefault());

        makeDefaultCard(secondCard);
        createdCard = getCreditCard(createdCard);
        assertFalse("first card should not be default", createdCard.isDefault());
        secondCard = getCreditCard(secondCard);
        assertTrue("second card should be default", secondCard.isDefault());

        makeDefaultCard(createdCard);
        createdCard = getCreditCard(createdCard);
        assertTrue("first card should be default", createdCard.isDefault());
        secondCard = getCreditCard(secondCard);
        assertFalse("second card should not be default", secondCard.isDefault());
    }


    @Test
    public void canGetCardTransactions() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999411111111112";
        CreditCard creditCard = getTestCreditCard(pan);

        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);
        createdCard = waitForActivation(createdCard);

        assertEquals("post deactivation card state", "ACTIVE", createdCard.getState());
        assertTrue("should be default", createdCard.isDefault());

        Collections.TransactionCollection transactions = getCardTransactions(createdCard);
        assertNotNull("card should have transactions", transactions);
        assertTrue("card should have at least one transaction", transactions.getResults().size() > 0);

        Transaction transaction = transactions.getResults().get(0);
        Transaction retreivedTransaction = getTransaction(transaction);
        assertEquals("should be the same transaction", transaction.getTransactionId(), retreivedTransaction.getTransactionId());

    }
}
