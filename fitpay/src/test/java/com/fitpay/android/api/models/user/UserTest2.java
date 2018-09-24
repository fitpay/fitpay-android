package com.fitpay.android.api.models.user;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestUtils;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ResultProvidingCallback;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.utils.NamedResource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UserTest2 extends TestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(UserTest2.class);

    @Override
    @Before
    public void before() throws Exception {
        userName = "userName@useremail.com";
        pin = TestUtils.getRandomLengthNumber(4, 4);

        this.user = createUser(getNewTestUser(userName, pin));
        assertNotNull(this.user);

        loginIdentity = getTestLoginIdentity(userName, pin);
        doLogin(loginIdentity);
    }

    @Test
    public void testCanGetUser() throws Exception {
        this.user = getUser();
        assertNotNull(user);
        assertEquals("userName", userName, user.getUsername());
        assertEquals("email", userName, user.getEmail());
        assertNotNull("user connectorId", user.getId());
        assertNotNull("created ts", user.getCreatedTsEpoch());
    }

    @Test
    @Ignore  // this test does not work in demo environment since does auto-login
    public void testCantLoginWithDifferentPassword() throws Exception {
        this.user = getUser();
        assertNotNull(user);
        LoginIdentity badCredentials = getTestLoginIdentity(userName, TestUtils.getRandomLengthNumber(4, 4));

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<OAuthToken> callback = new ResultProvidingCallback<>(latch);
        ApiManager.getInstance().login(badCredentials, callback);
        boolean completed = latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertTrue("login did not complete successfully", completed);
        assertEquals("login error code. (message: " + callback.getErrorMessage() + ")", 401, callback.getErrorCode());
    }


    @Test
    public void testCanRepeatLogin() throws Exception {
        this.user = getUser();
        assertNotNull(user);
        doLogin(loginIdentity);
        User user2  = getUser();
        assertEquals("should be the same user", user.getId(), user2.getId());
    }

    @Test
    public void testUserCanGetSelf() throws Exception {
        this.user = getUser();

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<User> callback = new ResultProvidingCallback<>(latch);
        user.self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        User user2 = callback.getResult();

        assertEquals("user self had error code (message: " + callback.getErrorMessage() + ")", -1, callback.getErrorCode());
        assertEquals("user connectorId", user.getId(), user2.getId());
        assertEquals("email", user.getEmail(), user2.getEmail());
        assertEquals("user name", user.getUsername(), user2.getUsername());
        assertEquals("build ts", user.getCreatedTsEpoch(), user2.getCreatedTsEpoch());
    }

    @Test
    public void testNewUserCanGetCards() throws Exception {
        this.user = getUser();

        Collections.CreditCardCollection creditCards = getCreditCards(user);

        assertNotNull("credit card collection", creditCards);
        assertEquals("number of cards a new user has", 0, creditCards.getTotalResults());

    }

    @Test
    public void testNewUserCanGetDevices() throws Exception {
        this.user = getUser();

        Collections.DeviceCollection collection = getDevices(user);

        assertNotNull("device collection", collection);
        assertEquals("number of devices a new user has", 0, collection.getTotalResults());

    }


    @Test
    @Ignore
    //TODO Comparing to edge tests, should not be able to get user after delete.   Why can we here?
    public void testCantGetDeletedUser() throws Exception {
        this.user = getUser();
        assertNotNull(user);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Void> callback = new ResultProvidingCallback<>(latch);
        this.user.deleteUser(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("delete error code", -1, callback.getErrorCode());

        User deletedUser = getUser();
        assertNull("user should not exist", deletedUser);
        this.user = null;
    }


}