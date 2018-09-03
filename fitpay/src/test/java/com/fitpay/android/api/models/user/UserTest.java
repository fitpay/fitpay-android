package com.fitpay.android.api.models.user;


import com.fitpay.android.Steps;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserTest {

    private static Steps steps = null;

    @BeforeClass
    public static void init() {
        steps = new Steps(UserTest.class);
    }

    @Test
    public void test00_createUser() throws InterruptedException {
        steps.createUser();
    }

    @Test
    public void test01_loginUser() throws InterruptedException {
        steps.login();
    }

    @Test
    public void test02_getUser() throws InterruptedException {
        steps.getUser();
    }

    @Test
    public void test03_selfUser() throws InterruptedException {
        steps.selfUser();
    }

    @Test
    @Ignore
    //TODO this test does not work since anonymous user is not valid.  There is an open ticket in Jira
    public void test04_updateUser() throws InterruptedException {
        steps.updateUser();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        steps.destroy();
        steps = null;
    }

}