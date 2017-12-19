package com.fitpay.android;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ResultProvidingCallback;
import com.fitpay.android.api.enums.DeviceTypes;
import com.fitpay.android.api.models.Transaction;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.card.Address;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.Reason;
import com.fitpay.android.api.models.card.VerificationMethod;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.device.PaymentDevice;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.api.models.user.LoginIdentity;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.models.user.UserCreateRequest;
import com.fitpay.android.paymentdevice.impl.mock.SecureElementDataProvider;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.TimestampUtils;
import com.fitpay.android.utils.ValidationException;
import com.google.gson.Gson;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.security.Security;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class TestActions {

    protected final int TIMEOUT = 30;

    protected String userName = null;
    protected String pin = null;
    protected LoginIdentity loginIdentity = null;

    protected User user;

    @BeforeClass
    public static void init() {
        FPLog.addLogImpl(new FPLog.ILog() {
            @Override
            public void d(String tag, String text) {
                System.out.println(tag + " DEBUG (" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void i(String tag, String text) {
                System.out.println(tag + " INFO(" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void w(String tag, String text) {
                System.out.println(tag + " WARN(" + Thread.currentThread().getName() + "): " + text);
            }

            @Override
            public void e(String tag, Throwable throwable) {
                System.out.println(tag + " ERROR (" + Thread.currentThread().getName() + "): " + tag);

                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }

            @Override
            public int logLevel() {
                return FPLog.DEBUG;
            }
        });
        FPLog.setShowHTTPLogs(false);

        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        ApiManager.init(TestConstants.getConfig());

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook(){
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        RxJavaHooks.setOnIOScheduler(scheduler -> Schedulers.immediate());
        RxJavaHooks.setOnComputationScheduler(scheduler -> Schedulers.immediate());
        RxJavaHooks.setOnNewThreadScheduler(scheduler -> Schedulers.immediate());
    }

    @Before
    public void testActionsSetup() throws Exception {
        userName = TestUtils.getRandomLengthString(5, 10) + "@"
                + TestUtils.getRandomLengthString(5, 10) + "." + TestUtils.getRandomLengthString(4, 10);
        pin = TestUtils.getRandomLengthNumber(4, 4);

        this.user = createUser(getNewTestUser(userName, pin));
        assertNotNull(this.user);

        loginIdentity = getTestLoginIdentity(userName, pin);
        doLogin(loginIdentity);

        this.user = getUser();
        assertNotNull(user);
    }

    @After
    public void deleteUser() throws Exception {
        if (null != this.user) {
            final CountDownLatch latch = new CountDownLatch(1);
            ResultProvidingCallback<Void> callback = new ResultProvidingCallback<>(latch);
            this.user.deleteUser(callback);
            latch.await(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    protected User createUser(UserCreateRequest user) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<User> callback = new ResultProvidingCallback<>(latch);
        ApiManager.getInstance().createUser(user, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return callback.getResult();
    }

    protected boolean doLogin(LoginIdentity loginIdentity) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<OAuthToken> callback = new ResultProvidingCallback<>(latch);
        ApiManager.getInstance().loginUser(loginIdentity, callback);
        boolean completed = latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertTrue("login did not complete successfully", completed);
        assertEquals("login error code. (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return completed;
    }


    protected User getUser() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        ResultProvidingCallback<User> callback = new ResultProvidingCallback<>(latch);

        ApiManager.getInstance().getUser(callback);

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        User user = callback.getResult();
        Assert.assertNotNull(user);
        return user;
    }

    protected LoginIdentity getTestLoginIdentity(String userName, String pin) throws ValidationException {

        LoginIdentity loginIdentity = new LoginIdentity.Builder()
                .setUsername(userName)
                .setPassword(pin)
                .build();
        return loginIdentity;

    }

    protected UserCreateRequest getNewTestUser(String userName, String pin) throws ValidationException {
        return new UserCreateRequest.Builder()
                .email(userName)
                .pin(pin)
                .build();
    }

    protected CreditCard getTestCreditCard(String pan) {
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
        return creditCard;
    }

    public Device getTestDevice() {
        return getTestDevice(true);
    }

    public Device getTestDevice(boolean paymentDevice) {

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

        Device.Builder builder = new Device.Builder()
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
                .setPairingTs(pairingTs);


        if (paymentDevice) {
            builder = builder
                        .setSecureElement(new PaymentDevice.SecureElement(
                                SecureElementDataProvider.generateCasd(),
                                SecureElementDataProvider.generateRandomSecureElementId()));
        }

        return builder.build();

    }

    public Device getPoorlyDefinedDevice() {

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
                .setDeviceName(deviceName)
                .build();

        return newDevice;

    }

    public Device getPoorlyDeviceTestSmartStrapDevice() {

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
                .setDeviceType(DeviceTypes.SMART_STRAP)
                .setDeviceName(deviceName)
                .build();

        return newDevice;

    }

    protected Device createDevice(User user, Device device) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Device> callback = new ResultProvidingCallback<>(latch);
        user.createDevice(device, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        return callback.getResult();
    }

    protected CreditCard createCreditCard(User user, CreditCard creditCard) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        user.createCreditCard(creditCard, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return callback.getResult();
    }

    protected CreditCard getCreditCard(CreditCard creditCard) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        creditCard.self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("get credit card had error code.  (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return callback.getResult();
    }


    protected Collections.CreditCardCollection getCreditCards(User user) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Collections.CreditCardCollection> callback = new ResultProvidingCallback<>(latch);
        user.getCreditCards(10, 0, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("get credit cards had error code.  (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return callback.getResult();
    }

    protected CreditCard acceptTerms(CreditCard creditCard) throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        creditCard.acceptTerms(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        CreditCard acceptedCard = callback.getResult();

        if (acceptedCard == null) {
            return null;
        }

        TestConstants.waitSomeActionsOnServer();

        //getSelf
        ResultProvidingCallback<CreditCard> callbackSelf = new ResultProvidingCallback<>(latch);
        acceptedCard.self(callbackSelf);
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        return callbackSelf.getResult();
    }

    protected CreditCard declineTerms(CreditCard creditCard) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        creditCard.declineTerms(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return callback.getResult();
    }

    protected CreditCard deactivateCard(CreditCard creditCard, Reason reason) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        creditCard.deactivate(reason, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return callback.getResult();
    }

    protected CreditCard reactivateCard(CreditCard creditCard, Reason reason) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<CreditCard> callback = new ResultProvidingCallback<>(latch);
        creditCard.reactivate(reason, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return callback.getResult();
    }


    protected void makeDefaultCard(CreditCard creditCard) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Void> callback = new ResultProvidingCallback<>(latch);
        creditCard.makeDefault(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("make default error code", -1, callback.getErrorCode());
    }


    protected void deleteCard(CreditCard creditCard) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Void> callback = new ResultProvidingCallback<>(latch);
        creditCard.deleteCard(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("delete error code", -1, callback.getErrorCode());
    }

    protected VerificationMethod selectVerificationMethod(VerificationMethod method) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<VerificationMethod> callback = new ResultProvidingCallback<>(latch);
        method.select(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return callback.getResult();
    }

    protected VerificationMethod verifyVerificationMethod(VerificationMethod method, String verificationCode) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<VerificationMethod> callback = new ResultProvidingCallback<>(latch);
        method.verify(verificationCode, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return callback.getResult();
    }

    protected Collections.TransactionCollection getCardTransactions(CreditCard card) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Collections.TransactionCollection> callback = new ResultProvidingCallback<>(latch);
        card.getTransactions(10, 0, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("get device transactions error code.  (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return callback.getResult();
    }

    protected Transaction getTransaction(Transaction transaction) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Transaction> callback = new ResultProvidingCallback<>(latch);
        transaction.self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("get device transaction error code.  (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return callback.getResult();
    }


    protected Collections.DeviceCollection getDevices(User user) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Collections.DeviceCollection> callback = new ResultProvidingCallback<>(latch);
        user.getDevices(10, 0, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("get devices error code.  (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return callback.getResult();
    }


    protected Collections.CommitsCollection getCommits(Device device, String lastCommitId) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Collections.CommitsCollection> callback = new ResultProvidingCallback<>(latch);
        device.getCommits(lastCommitId, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("get commits error code.  (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return callback.getResult();
    }


    protected Collections.CommitsCollection getAllCommits(Device device, String lastCommitId) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Collections.CommitsCollection> callback = new ResultProvidingCallback<>(latch);
        device.getAllCommits(lastCommitId, callback);
        latch.await(TIMEOUT * 3, TimeUnit.SECONDS);
        assertEquals("get commits error code.  (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        return callback.getResult();
    }

    protected ApduPackage getTestApduPackage() {

        String apduJson = "{  \n" +
                "   \"seIdType\":\"iccid\",\n" +
                "   \"targetDeviceType\":\"fitpay.gandd.model.Device\",\n" +
                "   \"targetDeviceId\":\"72425c1e-3a17-4e1a-b0a4-a41ffcd00a5a\",\n" +
                "   \"packageId\":\"baff08fb-0b73-5019-8877-7c490a43dc64\",\n" +
                "   \"seId\":\"333274689f09352405792e9493356ac880c44444442\",\n" +
                "   \"targetAid\":\"8050200008CF0AFB2A88611AD51C\",\n" +
                "   \"commandApdus\":[  \n" +
                "      {  \n" +
                "         \"commandId\":\"5f2acf6f-536d-4444-9cf4-7c83fdf394bf\",\n" +
                "         \"groupId\":0,\n" +
                "         \"sequence\":0,\n" +
                "         \"command\":\"00E01234567890ABCDEF\",\n" +
                "         \"type\":\"CREATE FILE\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"00df5f39-7627-447d-9380-46d8574e0643\",\n" +
                "         \"groupId\":1,\n" +
                "         \"sequence\":1,\n" +
                "         \"command\":\"8050200008CF0AFB2A88611AD51C\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"9c719928-8bb0-459c-b7c0-2bc48ec53f3c\",\n" +
                "         \"groupId\":1,\n" +
                "         \"sequence\":2,\n" +
                "         \"command\":\"84820300106BBC29E6A224522E83A9B26FD456111500\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"b148bea5-6d98-4c83-8a20-575b4edd7a42\",\n" +
                "         \"groupId\":1,\n" +
                "         \"sequence\":3,\n" +
                "         \"command\":\"8800E01234567890ABCDEF84820300106BBC29E6A224522E83A9B26FD456111500\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"905fc5ab-4b15-4704-889b-2c5ffcfb2d68\",\n" +
                "         \"groupId\":2,\n" +
                "         \"sequence\":4,\n" +
                "         \"command\":\"84F2200210F25397DCFB728E25FBEE52E748A116A800\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"8e87ff12-dfc2-472a-bbf1-5f2e891e864c\",\n" +
                "         \"groupId\":3,\n" +
                "         \"sequence\":5,\n" +
                "         \"command\":\"84F2200210F25397DCFB728E25FBEE52E748A116A800\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"validUntil\":\"2020-12-11T21:22:58.691Z\",\n" +
                "   \"apduPackageUrl\":\"http://localhost:9103/transportservice/v1/apdupackages/baff08fb-0b73-5019-8877-7c490a43dc64\"\n" +
                "}";

        Gson gson = new Gson();
        ApduPackage apduPackage = gson.fromJson(apduJson, ApduPackage.class);
        return apduPackage;

    }

    protected ApduPackage getFailingTestApduPackage() {

        String apduJson = "{  \n" +
                "   \"seIdType\":\"iccid\",\n" +
                "   \"targetDeviceType\":\"fitpay.gandd.model.Device\",\n" +
                "   \"targetDeviceId\":\"72425c1e-3a17-4e1a-b0a4-a41ffcd00a5a\",\n" +
                "   \"packageId\":\"baff08fb-0b73-5019-8877-7c490a43dc64\",\n" +
                "   \"seId\":\"333274689f09352405792e9493356ac880c44444442\",\n" +
                "   \"targetAid\":\"8050200008CF0AFB2A88611AD51C\",\n" +
                "   \"commandApdus\":[  \n" +
                "      {  \n" +
                "         \"commandId\":\"5f2acf6f-536d-4444-9cf4-7c83fdf394bf\",\n" +
                "         \"groupId\":0,\n" +
                "         \"sequence\":0,\n" +
                "         \"command\":\"00E01234567890ABCDEF\",\n" +
                "         \"type\":\"CREATE FILE\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"00df5f39-7627-447d-9380-46d8574e0643\",\n" +
                "         \"groupId\":1,\n" +
                "         \"sequence\":1,\n" +
                "         \"command\":\"8050200008CF0AFB2A88611AD51C\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"9c719928-8bb0-459c-b7c0-2bc48ec53f3c\",\n" +
                "         \"groupId\":1,\n" +
                "         \"sequence\":2,\n" +
                "         \"command\":\"999900\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"b148bea5-6d98-4c83-8a20-575b4edd7a42\",\n" +
                "         \"groupId\":1,\n" +
                "         \"sequence\":3,\n" +
                "         \"command\":\"9800E01234567890ABCDEF84820300106BBC29E6A224522E83A9B26FD456111500\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"905fc5ab-4b15-4704-889b-2c5ffcfb2d68\",\n" +
                "         \"groupId\":2,\n" +
                "         \"sequence\":4,\n" +
                "         \"command\":\"84F2200210F25397DCFB728E25FBEE52E748A116A800\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"commandId\":\"8e87ff12-dfc2-472a-bbf1-5f2e891e864c\",\n" +
                "         \"groupId\":3,\n" +
                "         \"sequence\":5,\n" +
                "         \"command\":\"84F2200210F25397DCFB728E25FBEE52E748A116A800\",\n" +
                "         \"type\":\"UNKNOWN\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"validUntil\":\"2020-12-11T21:22:58.691Z\",\n" +
                "   \"apduPackageUrl\":\"http://localhost:9103/transportservice/v1/apdupackages/baff08fb-0b73-5019-8877-7c490a43dc64\"\n" +
                "}";

        Gson gson = new Gson();
        ApduPackage apduPackage = gson.fromJson(apduJson, ApduPackage.class);
        return apduPackage;

    }


    protected CreditCard waitForActivation(CreditCard card) throws Exception {
        assertNotNull("no card to wait for activation on", card);

        CreditCard retrievedCard = card;
        for (int x=0; x<30; x++) {
            retrievedCard = getCreditCard(retrievedCard);
            if ("ACTIVE".equals(retrievedCard.getState())) {
                break;
            }

            Thread.sleep(1000);
        }

        assertEquals("card never transitioned to ACTIVE state", "ACTIVE", retrievedCard.getState());
        return retrievedCard;
    }

}