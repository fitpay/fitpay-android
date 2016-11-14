package com.fitpay.android;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.DeviceTypes;
import com.fitpay.android.api.enums.CardInitiators;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.user.LoginIdentity;
import com.fitpay.android.api.models.Relationship;
import com.fitpay.android.api.models.card.Address;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.Reason;
import com.fitpay.android.api.models.card.VerificationMethod;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.models.user.UserCreateRequest;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.utils.TimestampUtils;
import com.fitpay.android.utils.ValidationException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;

import java.security.Security;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

/***
 * Created by Vlad on 16.03.2016.
 */
public class Steps {
    private final int TIMEOUT = 10;

    private String userName;
    private String password;

    private User currentUser;
    private int currentErrorCode;
    private String currentErrorMessage;
    private Collections.CreditCardCollection cardsCollection;
    private Collections.DeviceCollection devicesCollection;
    private CreditCard currentCard;
    private Device currentDevice;
    private Commit currentCommit;

    protected Steps() {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        userName = TestUtils.getRandomLengthString(5, 10) + "@"
                + TestUtils.getRandomLengthString(5, 10) + "." + TestUtils.getRandomLengthString(4, 10);
        password = TestUtils.getRandomLengthNumber(4, 4);
    }

    public void destroy() {
        currentUser = null;
        cardsCollection = null;
        currentDevice = null;
        currentCommit = null;
    }


    public User createUser() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        UserCreateRequest ucr = new UserCreateRequest.Builder()
                .email(userName)
                .pin(password)
                .build();

        ApiManager.getInstance().createUser(ucr, new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
                resetErrorFields();
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                currentErrorCode = errorCode;
                currentErrorMessage = errorMessage;
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(currentUser);
        Assert.assertNotNull(currentUser.getUsername());
        return currentUser;

    }

    public void login() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        LoginIdentity loginIdentity = null;

        try {
            loginIdentity = new LoginIdentity.Builder()
                    .setUsername(userName)
                    .setPassword(password)
                    .build();
        } catch (ValidationException ignored) {
        }

        Assert.assertNotNull(loginIdentity);

        ApiManager.getInstance().loginUser(loginIdentity, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isRequestSuccess[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
    }

    public User getUser() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        ApiManager.getInstance().getUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
                resetErrorFields();
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                currentErrorCode = errorCode;
                currentErrorMessage = errorMessage;
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(currentUser);
        Assert.assertNotNull(currentUser.getUsername());
        return currentUser;
    }

    private void resetErrorFields() {
        currentErrorCode = -1;
        currentErrorMessage = null;
    }

    public User selfUser() throws InterruptedException {
        Assert.assertNotNull(currentUser);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        currentUser.self(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                resetErrorFields();
                isRequestSuccess[0] = true;
                currentUser = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                currentErrorCode = errorCode;
                currentErrorMessage = errorMessage;
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentUser);
        Assert.assertNotNull(currentUser.getUsername());
        return currentUser;
    }

    public void updateUser() throws InterruptedException {
        Assert.assertNotNull(currentUser);

        final CountDownLatch latch = new CountDownLatch(1);

        String firstName = "John";
        String lastName = "Doe";
        long currentTimestamp = System.currentTimeMillis();
        String timestampString = TimestampUtils.getISO8601StringForTime(currentTimestamp);
        String termsVersion = "0.0.2";
        User patchingUser = new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setBirthDate(currentTimestamp)
                .setTermsVersion(termsVersion)
                .build();

        currentUser.updateUser(patchingUser, new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                Assert.assertNotNull(result);
                currentUser = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        Assert.assertNotNull(currentUser);
        Assert.assertEquals(firstName, currentUser.getFirstName());
        Assert.assertEquals(lastName, currentUser.getLastName());
        Assert.assertEquals(timestampString, currentUser.getBirthDate());
        Assert.assertEquals(termsVersion, currentUser.getTermsVersion());
    }

    public void createCard() throws InterruptedException {
        String pan = "9999545454545454";
        createCard(pan);
    }

    public void createCard(String pan) throws InterruptedException {

        Assert.assertNotNull(currentUser);

        final CountDownLatch latch = new CountDownLatch(1);

        String cardName = "TEST CARD";
        int expYear = 2018;
        int expMonth = 10;
        String city = "Boulder";
        String state = "CO";
        String postalCode = "80302";
        String countryCode = "US";
        String street1 = "1035 Pearl St";
        String cvv = "133";

        Address address = new Address();
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountryCode(countryCode);
        address.setStreet1(street1);

        CreditCard creditCard = new CreditCard.Builder()
                .setCVV(cvv)
                .setPAN(pan)
                .setExpDate(expYear, expMonth)
                .setAddress(address)
                .setName(cardName)
                .build();

        currentUser.createCreditCard(creditCard, new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                currentCard = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(currentCard);
        Assert.assertEquals(cardName, currentCard.getName());
        Assert.assertEquals(city, currentCard.getAddress().getCity());
        Assert.assertEquals(state, currentCard.getAddress().getState());
        Assert.assertEquals(postalCode, currentCard.getAddress().getPostalCode());
        Assert.assertEquals(countryCode, currentCard.getAddress().getCountryCode());
        Assert.assertEquals(street1, currentCard.getAddress().getStreet1());
        Assert.assertNotNull(currentCard.getCVV());
        Assert.assertTrue(currentCard.getPan().endsWith(pan.substring(12)));
        Assert.assertTrue(expYear == currentCard.getExpYear());
        Assert.assertTrue(expMonth == currentCard.getExpMonth());
        Assert.assertEquals("ELIGIBLE", currentCard.getState());
    }

    public void acceptTerms() throws InterruptedException {
        getDevices();
        Assert.assertNotNull(currentUser);
        Assert.assertNotNull(currentCard);
        Assert.assertNotNull(currentDevice);

        final CountDownLatch latch = new CountDownLatch(2);
        final boolean[] isRequestSuccess = {false};

        ApiManager.getInstance().createRelationship(currentUser.getId(), currentCard.getCreditCardId(),
                currentDevice.getDeviceIdentifier(), new ApiCallback<Relationship>() {
                    @Override
                    public void onSuccess(Relationship result) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        latch.countDown();
                    }
                });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        currentCard.acceptTerms(new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                isRequestSuccess[0] = true;
                currentCard = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(currentCard);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertEquals("PENDING_VERIFICATION", currentCard.getState());
    }

    public void declineTerms() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        currentCard.declineTerms(new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                isRequestSuccess[0] = true;
                currentCard = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentCard);
        Assert.assertEquals(currentCard.getState(), "DECLINED_TERMS_AND_CONDITIONS");
    }

    public void selectCard() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final VerificationMethod[] verificationMethods = {null};
        final boolean[] isRequestSuccess = {false};

        currentCard.getVerificationMethods().get(0).select(new ApiCallback<VerificationMethod>() {
            @Override
            public void onSuccess(VerificationMethod result) {
                isRequestSuccess[0] = true;
                verificationMethods[0] = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentCard);
        Assert.assertEquals("AWAITING_VERIFICATION", verificationMethods[0].getState());
    }

    public void verifyCard() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final VerificationMethod[] verificationMethods = {null};
        final boolean[] isRequestSuccess = {false};

        currentCard.getVerificationMethods().get(0).verify("12345", new ApiCallback<VerificationMethod>() {
            @Override
            public void onSuccess(VerificationMethod result) {
                isRequestSuccess[0] = true;
                verificationMethods[0] = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentCard);
        Assert.assertTrue("VERIFIED".equals(verificationMethods[0].getState()) || "AWAITING_VERIFICATION".equals(verificationMethods[0].getState()));
    }

    public void updateCard() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        String city = "New York";
        String state = "NY";
        Address address = new Address();
        address.setCity(city);
        address.setState(state);

        CreditCard creditCard = new CreditCard.Builder()
//                .setAddress(address)
                .setName("Hello")
                .build();

        currentCard.updateCard(creditCard, new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                isRequestSuccess[0] = true;
                currentCard = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();//{"errors":[{"message":"patch request contains unsupported operations: [SecureJsonPatchOperation(op=replace, path=addresscity, value=New York), SecureJsonPatchOperation(op=replace, path=addressstate, value=NY)]"}]}
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(currentCard);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertEquals(state, currentCard.getAddress().getState());
        Assert.assertEquals(city, currentCard.getAddress().getCity());
    }

    public void deactivateCard() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        Reason reason = new Reason();
        reason.setReason("lost");
        reason.setCausedBy(CardInitiators.INITIATOR_CARDHOLDER);

        currentCard.deactivate(reason, new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                isRequestSuccess[0] = true;
                currentCard = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentCard);
    }

    public void reactivateCard() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        Reason reason = new Reason();
        reason.setReason("found");
        reason.setCausedBy(CardInitiators.INITIATOR_CARDHOLDER);

        currentCard.reactivate(reason, new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                isRequestSuccess[0] = true;
                currentCard = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(currentCard);
        Assert.assertTrue(isRequestSuccess[0]);
    }

    public void makeDefault() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};
        final String[] theError = {""};

        currentCard.makeDefault(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isRequestSuccess[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                theError[0] = "code: " + errorCode + ", message: " + errorMessage;
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue("make default was not successful.  reason: " + theError[0], isRequestSuccess[0]);
    }

    public void selfCard() throws InterruptedException {
        Assert.assertNotNull(currentCard);

        TestConstants.waitSomeActionsOnServer();

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        currentCard.self(new ApiCallback<CreditCard>() {
            @Override
            public void onSuccess(CreditCard result) {
                isRequestSuccess[0] = true;
                currentCard = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentCard);
    }

    public void getCards() throws InterruptedException {
        Assert.assertNotNull(currentUser);

        final CountDownLatch latch = new CountDownLatch(1);

        currentUser.getCreditCards(10, 0, new ApiCallback<Collections.CreditCardCollection>() {
            @Override
            public void onSuccess(Collections.CreditCardCollection result) {
                cardsCollection = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(cardsCollection);
    }

    public void deleteTestCards() throws InterruptedException {
        getCards();

        List<CreditCard> cards = cardsCollection.getResults();
        int size = cards.size();

        final CountDownLatch latch = new CountDownLatch(size);
        final int[] success = {0};

        int isNotTestCard = 0;
        for (int i = 0; i < size; i++) {
            CreditCard card = cards.get(i);

            if (card.getName() == null || !card.getName().equals("TEST CARD")) {
                isNotTestCard++;
                latch.countDown();
            } else {
                card.deleteCard(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        success[0]++;
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await(TIMEOUT * size, TimeUnit.SECONDS);

        getCards();
        Assert.assertEquals(isNotTestCard, cardsCollection.getResults().size());
    }

    public void getTransactions() throws InterruptedException {
        getCards();

        currentCard = cardsCollection.getResults().get(0);
        Assert.assertNotNull(currentCard);

        final CountDownLatch latch = new CountDownLatch(1);
        final Collections.TransactionCollection[] transactionCollection = {null};


        currentCard.getTransactions(10, 0, new ApiCallback<Collections.TransactionCollection>() {
            @Override
            public void onSuccess(Collections.TransactionCollection result) {
                transactionCollection[0] = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull(cardsCollection);
        Assert.assertNotNull(transactionCollection[0]);
        Assert.assertNotNull(transactionCollection[0].getResults());
    }

    public void createDevice() throws InterruptedException {
        Assert.assertNotNull(currentUser);

        final CountDownLatch latch = new CountDownLatch(1);

        String manufacturerName = "X111";
        String deviceName = "TEST_DEVICE";
        String firmwareRevision = "111.111";
        String hardwareRevision = "1.1.1";
        String modelNumber = "AB111";
        String serialNumber = "1111AB";
        String softwareRevision = "1.1.1";
        String systemId = "0x111AA";
        String oSName = "A1111";
        String licenseKey = "aaaaaa-1111-1111-1111-111111111111";
        String bdAddress = "bbbbbb-1111-1111-1111-111111111111";
        long pairingTs = System.currentTimeMillis();
        String stringTimestamp = TimestampUtils.getISO8601StringForTime(pairingTs);
        String secureElementId = "cccccc-1111-1111-1111-1111111111";
        Device newDevice = new Device.Builder()
                .setDeviceType(DeviceTypes.ACTIVITY_TRACKER)
                .setManufacturerName(manufacturerName)
                .setDeviceName(deviceName)
                .setFirmwareRevision(firmwareRevision)
                .setHardwareRevision(hardwareRevision)
                .setModelNumber(modelNumber)
                .setSerialNumber(serialNumber)
                .setSoftwareRevision(softwareRevision)
                .setSystemId(systemId)
                .setOSName(oSName)
                .setLicenseKey(licenseKey)
                .setBdAddress(bdAddress)
                .setPairingTs(pairingTs)
                .setSecureElementId(secureElementId)
                .build();
        final String[] errors = { "" };
        final int[] errorCodes = {-1};
        currentUser.createDevice(newDevice, new ApiCallback<Device>() {
            @Override
            public void onSuccess(Device result) {
                currentDevice = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                errors[0] = errorMessage;
                errorCodes[0] = errorCode;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("build device had an error.  (Message: " + errors[0] + ")", -1, errorCodes[0]);
        Assert.assertNotNull(currentDevice);
        Assert.assertEquals(manufacturerName, currentDevice.getManufacturerName());
        Assert.assertEquals(deviceName, currentDevice.getDeviceName());
        Assert.assertEquals(firmwareRevision, currentDevice.getFirmwareRevision());
        Assert.assertEquals(hardwareRevision, currentDevice.getHardwareRevision());
        Assert.assertEquals(modelNumber, currentDevice.getModelNumber());
        Assert.assertEquals(serialNumber, currentDevice.getSerialNumber());
        Assert.assertEquals(softwareRevision, currentDevice.getSoftwareRevision());
        Assert.assertEquals(systemId, currentDevice.getSystemId());
        Assert.assertEquals(oSName, currentDevice.getOsName());
        if (DeviceTypes.SMART_STRAP.equals(currentDevice.getDeviceType())) {
            Assert.assertEquals(licenseKey, currentDevice.getLicenseKey());//todo check
            Assert.assertEquals(bdAddress, currentDevice.getBdAddress());
        }
        Assert.assertEquals(stringTimestamp, currentDevice.getPairingTs());
        Assert.assertEquals(secureElementId, currentDevice.getSecureElementId());
    }

    public void getDevices() throws InterruptedException {
        Assert.assertNotNull(currentUser);

        final CountDownLatch latch = new CountDownLatch(1);

        currentUser.getDevices(10, 0, new ApiCallback<Collections.DeviceCollection>() {
            @Override
            public void onSuccess(Collections.DeviceCollection result) {
                devicesCollection = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull("Device collection should not be null", devicesCollection);
        if (currentDevice == null && devicesCollection.getTotalResults() > 0) {
            currentDevice = devicesCollection.getResults().get(0);
        }
    }

    public void selfDevice() throws InterruptedException {
        Assert.assertNotNull(currentDevice);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        currentDevice.self(new ApiCallback<Device>() {
            @Override
            public void onSuccess(Device result) {
                isRequestSuccess[0] = true;
                currentDevice = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentDevice);
    }

    public void updateDevice() throws InterruptedException {
        Assert.assertNotNull(currentDevice);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        String firmwareRevision = "222.222";
        String softwareRevision = "2.2.2";
        Device newDevice = new Device.Builder()
                .setFirmwareRevision(firmwareRevision)
                .setSoftwareRevision(softwareRevision)
                .build();

        currentDevice.updateDevice(newDevice, new ApiCallback<Device>() {
            @Override
            public void onSuccess(Device result) {
                isRequestSuccess[0] = true;
                currentDevice = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentDevice);
        Assert.assertEquals(firmwareRevision, currentDevice.getFirmwareRevision());
        Assert.assertEquals(softwareRevision, currentDevice.getSoftwareRevision());
    }

    public void getDeviceUser() throws InterruptedException {
        Assert.assertNotNull(currentDevice);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        currentDevice.getUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                isRequestSuccess[0] = true;
                currentUser = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentUser);
    }

    public void deleteDevice() throws InterruptedException {
        Assert.assertNotNull(currentDevice);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        currentDevice.deleteDevice(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isRequestSuccess[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
    }

    public void deleteTestDevices() throws InterruptedException {
        getDevices();

        List<Device> devices = devicesCollection.getResults();
        int size = devices.size();

        final CountDownLatch latch = new CountDownLatch(size);
        final int[] success = {0};
        int isNotTestDevice = 0;

        for (int i = 0; i < size; i++) {
            Device device = devices.get(i);

            if ("TEST_DEVICE".equals(device.getDeviceName())) {
                device.deleteDevice(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        success[0]++;
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        latch.countDown();
                    }
                });
            } else {
                isNotTestDevice++;
                latch.countDown();
            }
        }

        latch.await(TIMEOUT * size, TimeUnit.SECONDS);

        getDevices();
        Assert.assertEquals(isNotTestDevice, devicesCollection.getResults().size());

    }

    public void getCommits() throws InterruptedException {
        if (currentDevice == null && devicesCollection != null && devicesCollection.getTotalResults() > 0) {
            currentDevice = devicesCollection.getResults().get(0);
        }
        Assert.assertNotNull(currentDevice);

        final CountDownLatch latch = new CountDownLatch(2);
        final boolean[] isRequestSuccess = {false};

        currentDevice.getCommits(10, 0, new ApiCallback<Collections.CommitsCollection>() {
            @Override
            public void onSuccess(Collections.CommitsCollection result) {
                isRequestSuccess[0] = true;
                Assert.assertNotNull("commit collection should not be null", result);
                Assert.assertNotNull("commits collection results should not be null", result.getResults());
                Assert.assertTrue("Device should have at least one commit (card was added)", result.getTotalResults() > 0);
                currentCommit = result.getResults().get(0);
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentCommit);
    }

    public void selfCommit() throws InterruptedException {
        Assert.assertNotNull(currentCommit);

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isRequestSuccess = {false};

        currentCommit.self(new ApiCallback<Commit>() {
            @Override
            public void onSuccess(Commit result) {
                isRequestSuccess[0] = true;
                currentCommit = result;
                latch.countDown();
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertTrue(isRequestSuccess[0]);
        Assert.assertNotNull(currentCommit);
    }

}
