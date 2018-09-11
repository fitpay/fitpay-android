package com.fitpay.android.api.models.card;

import android.media.Image;
import android.os.Debug;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestConstants;
import com.fitpay.android.api.callbacks.ResultProvidingCallback;
import com.fitpay.android.api.enums.CardInitiators;
import com.fitpay.android.api.models.Transaction;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.utils.FPLog;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);

        verifyCardContents(creditCardInfo, createdCard);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Image> callback = new ResultProvidingCallback<>(latch);
        createdCard.getCardMetaData().getBrandLogo().get(0).self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        //TODO enable test for image retrieval
        //assertEquals(-1, callback.getErrorCode());
    }

    @Test
    public void testCantAddCreditCardWithNoDevice() throws Exception {
        String pan = "9999545454545454";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        user.createCreditCard(creditCardInfo, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        CreditCard createdCard = callback.getResult();

        assertNull("created card",createdCard);
        assertThat("error code",callback.getErrorCode() == 400 || callback.getErrorCode() == 500);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created",createdCard);

        Collections.CreditCardCollection creditCards = getCreditCards(user);
        assertNotNull("credit cards collection", creditCards);
        assertEquals("number of credit cards", 1, creditCards.getTotalResults());
        assertEquals("credit card connectorId", createdCard.getCreditCardId(), creditCards.getResults().get(0).getCreditCardId());

        verifyCardContents(creditCardInfo, creditCards.getResults().get(0));
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created",createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created",createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);

        assertNotNull("card not successfully updated by decline terms", createdCard);
        assertEquals("card state", "PENDING_VERIFICATION", createdCard.getState());
        assertTrue("no verification methods", createdCard.getVerificationMethods().size() > 0);

        assertEquals("verification state", "AVAILABLE_FOR_SELECTION", createdCard.getVerificationMethods().get(0).getState());

        //TODO: uncomment when issue with port will be fixed on backend
        /*
        VerificationMethods methods = getVerificationMethods(user.getId(), createdCard.getCreditCardId());
        assertNotNull("verification methods response", methods);
        assertNotNull("verification methods", methods.getVerificationMethods());
        VerificationMethod method = selectVerificationMethod(methods.getVerificationMethods().get(0));
        */
        VerificationMethod method = selectVerificationMethod(createdCard.getVerificationMethods().get(0));

        assertEquals("verification state after selection", "AWAITING_VERIFICATION", method.getState());

        CreditCard retrievedCard = getCreditCard(createdCard);

        VerificationMethod selectedMethod = getSelectedVerificationMethod(retrievedCard);
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);

        createdCard = waitForActivation(createdCard);

        assertEquals("post deactivation card state", "ACTIVE", createdCard.getState());
        assertTrue("should be default", !createdCard.canMakeDefault());

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard secondCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", secondCard);

        assertEquals("card not in expected state", "ELIGIBLE", secondCard.getState());

        secondCard = acceptTerms(secondCard);
        secondCard = waitForActivation(secondCard);

        assertEquals("post deactivation card state", "ACTIVE", secondCard.getState());
        assertTrue("second card should not be default", secondCard.canMakeDefault());

        makeDefaultCard(createdDevice.getDeviceIdentifier(), secondCard);
        TestConstants.waitSomeActionsOnServer();
        CreditCard defaultCard = getCreditCardSelf(secondCard);
        assertFalse("second card should be default", defaultCard.canMakeDefault());

        Device updatedDevice = getDeviceSelf(createdDevice);
        CreditCard defaultCardFromDevice = getDefaultCreditCard(updatedDevice);

        assertEquals("device has wrong default card", updatedDevice.getDefaultCreditCardId(), defaultCardFromDevice.getCreditCardId());
        assertEquals("default card mismatch", defaultCard.getCreditCardId(), defaultCardFromDevice.getCreditCardId());
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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created",createdCard);
        assertEquals("card not in expected state", "ELIGIBLE", createdCard.getState());

        createdCard = acceptTerms(createdCard);
        createdCard = waitForActivation(createdCard);

        assertEquals("post deactivation card state", "ACTIVE", createdCard.getState());
        assertTrue("should be default", !createdCard.canMakeDefault());


        Collections.TransactionCollection transactions = getCardTransactions(createdCard);
        assertNotNull("card should have transactions", transactions);
        assertTrue("card should have at least one transaction", transactions.getResults().size() > 0);

        Transaction transaction = transactions.getResults().get(0);
        Transaction retreivedTransaction = getTransaction(transaction);
        assertEquals("should be the same transaction", transaction.getTransactionId(), retreivedTransaction.getTransactionId());
    }

    @Test
    public void canAddCreditCardWithoutExpOrCvv() throws Exception {
        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan, null, null, null);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);

        verifyCardContents(creditCardInfo, createdCard);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Image> callback = new ResultProvidingCallback<>(latch);
        createdCard.getCardMetaData().getBrandLogo().get(0).self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void canAddCreditCardWithoutCvv() throws Exception {
        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan, null, 10, 2018);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);

        verifyCardContents(creditCardInfo, createdCard);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Image> callback = new ResultProvidingCallback<>(latch);
        createdCard.getCardMetaData().getBrandLogo().get(0).self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void canAddCardWithYearAndNoMonth() throws Exception {
        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan, "133", null, 2022);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);

        verifyCardContents(creditCardInfo, createdCard);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Image> callback = new ResultProvidingCallback<>(latch);
        createdCard.getCardMetaData().getBrandLogo().get(0).self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void expirationValidationNegativeTests() throws Exception {
        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        try {
            CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan, "133", 11, null);
        } catch (Exception e) {
            assertTrue("should throw illegalArgument if card has month and no year", e instanceof IllegalArgumentException);
        }

        try {
            CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan, "133", 11, 2001);
        } catch (Exception e) {
            assertTrue("should throw illegalArgument if card has full exp date in past", e instanceof IllegalArgumentException);
        }

        try {
            CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan, "133", null, 2001);
        } catch (Exception e) {
            assertTrue("should throw illegalArgument if card has exp year in past", e instanceof IllegalArgumentException);
        }
    }

    protected void verifyCardContents(CreditCardInfo creditCardInfo, CreditCard createdCard) {
        assertNotNull("card not created",createdCard);
        if (null != creditCardInfo.getCVV()) {
            assertEquals("cvv should be masked", "###", createdCard.getCVV());
        }
        if (creditCardInfo.getExpMonth() != null) {
            assertEquals("exp month", creditCardInfo.getExpMonth(), createdCard.getExpMonth());
        }
        if (creditCardInfo.getExpYear() != null) {
            assertEquals("exp year", creditCardInfo.getExpYear(), createdCard.getExpYear());
        }
        assertEquals("street 1", creditCardInfo.getAddress().getStreet1(), createdCard.getAddress().getStreet1());
        assertEquals("postal code", creditCardInfo.getAddress().getPostalCode(), createdCard.getAddress().getPostalCode());
        assertNotNull("card meta data should be populated", createdCard.getCardMetaData());
        assertNotNull("brand logo should be populated", createdCard.getCardMetaData().getBrandLogo());
        assertTrue("brand logo should have at least one asset", createdCard.getCardMetaData().getBrandLogo().size() > 0);
        assertEquals("first brand logo mime type", "image/png", createdCard.getCardMetaData().getBrandLogo().get(0).getMimeType());
    }
}
