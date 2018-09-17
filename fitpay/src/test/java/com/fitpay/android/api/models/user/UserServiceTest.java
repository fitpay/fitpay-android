package com.fitpay.android.api.models.user;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestUtils;
import com.fitpay.android.api.models.apdu.ApduExecutionResultTest;
import com.fitpay.android.utils.NamedResource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 * Created by tgs on 4/27/16.
 */
public class UserServiceTest extends TestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(UserServiceTest.class);

    @Override
    @Before
    public void before() {
    }

    @Test
    public void canCreateUser() throws Exception {
        userName = TestUtils.getRandomLengthString(5, 10) + "@"
                + TestUtils.getRandomLengthString(5, 10) + "." + TestUtils.getRandomLengthString(4, 10);
        pin = TestUtils.getRandomLengthNumber(4, 4);

        UserCreateRequest user = getNewTestUser(userName, pin);
        User createdUser = createUser(user);
        assertNotNull("user should have been created", createdUser);

    }

    @Test
    public void canCreateFullyDefinedUser() throws Exception {
        userName = TestUtils.getRandomLengthString(5, 10) + "@"
                + TestUtils.getRandomLengthString(5, 10) + "." + TestUtils.getRandomLengthString(4, 10);
        pin = TestUtils.getRandomLengthNumber(4, 4);

        UserCreateRequest user = new UserCreateRequest.Builder()
                .email(userName)
                .pin(pin)
                .birthDate("2015-09-15")
                .firstName(TestUtils.getRandomLengthString(5, 10))
                .lastName(TestUtils.getRandomLengthString(5, 10))
                .origin("fitpay")
                .originAccountCreatedAtEpoch(new Date().getTime())
                .termsVersion("01")
                .termsAcceptedAtEpoch(new Date().getTime())
                .build();

        User createdUser = createUser(user);
        assertNotNull("user should have been created", createdUser);

    }

}
