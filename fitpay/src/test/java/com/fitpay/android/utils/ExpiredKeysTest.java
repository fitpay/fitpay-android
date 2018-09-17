package com.fitpay.android.utils;

import com.fitpay.android.Steps;
import com.fitpay.android.api.models.security.ECCKeyPair;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import mockit.Mock;
import mockit.MockUp;
import mockit.internal.state.SavePoint;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExpiredKeysTest {

    private static Steps steps = null;
    private int expiredCounter;

    @BeforeClass
    public static void init() {
        steps = new Steps(ExpiredKeysTest.class);
    }

    @Test
    public void test00_createUser() throws InterruptedException {
        steps.createUser();
    }

    @Test
    public void test01_loginUser() throws InterruptedException {
        SavePoint sp = new SavePoint();
        //emulateOneHourDelay();

        emulateExpiredKey();

        steps.login();
        steps.getUser();

        sp.rollback();
    }

    private void emulateExpiredKey() {
        new MockUp<ECCKeyPair>() {
            @Mock
            public boolean isExpired() {
                return expiredCounter++ == 0;
            }
        };
    }
}