package com.fitpay.android;

import com.fitpay.android.api.ApiManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreditCardTest {

    private static Steps steps = null;

    @BeforeClass
    public static void init() {
        steps = new Steps();
        ApiManager.init(TestConstants.getConfig());
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
    public void test03_addDevice() throws InterruptedException {
        steps.createDevice();
    }

    @Test
    public void test04_createCard() throws InterruptedException {
        steps.createCard();
    }

    @Test
    public void test05_acceptCard() throws InterruptedException {
        steps.acceptTerms();
        steps.selfCard();
    }

    @Test
    public void test06_selectCard() throws InterruptedException {
        steps.selectCard();
        steps.selfCard();
    }

    @Test
    public void test07_verifyCard() throws InterruptedException {
        steps.verifyCard();
        steps.selfCard();
    }

    @Test
    @Ignore
    public void test08_updateCard() throws InterruptedException {
        steps.updateCard();
    }

    @Test
    public void test09_deactivateCard() throws InterruptedException {
        steps.deactivateCard();
    }

    @Test
    public void test10_reactivateCard() throws InterruptedException {
        steps.reactivateCard();
    }

    @Test
    public void test11_createCard() throws InterruptedException {
        steps.createCard("9999545454545455");
    }

    @Test
    public void test12_acceptCard() throws InterruptedException {
        steps.acceptTerms();
        steps.selfCard();
    }

    @Test
    public void test13_selectCard() throws InterruptedException {
        steps.selectCard();
        steps.selfCard();
    }

    @Test
    public void test14_verifyCard() throws InterruptedException {
        steps.verifyCard();
        steps.selfCard();
    }


    @Test
    public void test15_makeDefault() throws InterruptedException {
        steps.makeDefault();
    }

    @Test
    public void test16_getTransactions() throws InterruptedException {
        steps.getTransactions();
    }

    @Test
    public void test16_declineTerms() throws InterruptedException {
        steps.createCard("9999545454545456");
        steps.declineTerms();
    }

    @Test
    public void test99_deleteTestCards() throws InterruptedException {
        steps.deleteTestCards();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        steps.destroy();
        steps = null;
    }
}
