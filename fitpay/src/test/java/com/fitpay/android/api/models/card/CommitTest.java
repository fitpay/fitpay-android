package com.fitpay.android.api.models.card;

import com.fitpay.android.Steps;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommitTest {

    private static Steps steps = null;

    @BeforeClass
    public static void init() {
        steps = new Steps(CommitTest.class);
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
    public void test030_addDevice() throws InterruptedException {
        steps.createDevice();
    }

    @Test
    public void test031_getDevices() throws InterruptedException {
        steps.getDevices();
    }

    @Test
    public void test032_addCard() throws InterruptedException {
        steps.createCard();
    }

    @Test
    public void test04_getCommits() throws InterruptedException {
        steps.getCommits();
    }

    @Test
    public void test05_selfCommit() throws InterruptedException {
        steps.selfCommit();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        steps.destroy();
        steps = null;
    }

}
