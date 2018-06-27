package com.fitpay.android;

import com.fitpay.android.configs.FitpayConfig;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeUnit;

import mockit.Mock;
import mockit.MockUp;
import mockit.internal.state.SavePoint;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExpiredKeysTest {

    private static Steps steps = null;

    @BeforeClass
    public static void init() {
        steps = new Steps();
        FitpayConfig.getInstance().init(TestConstants.getConfig());
    }

    @Test
    public void test00_createUser() throws InterruptedException {
        steps.createUser();
    }

    @Test
    public void test01_loginUser() throws InterruptedException {
        SavePoint sp = new SavePoint();
        emulateOneHourDelay();

        steps.login();
        steps.getUser();

        sp.rollback();
    }

    private void emulateOneHourDelay() {
        final long curTime = System.currentTimeMillis();

        new MockUp<System>() {
            @Mock
            long currentTimeMillis() {
                return curTime + TimeUnit.HOURS.toMillis(1);
            }
        };
    }
}