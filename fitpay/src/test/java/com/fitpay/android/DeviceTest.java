package com.fitpay.android;

import com.fitpay.android.utils.HttpLogging;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceTest {

    private static Steps steps = null;

    @BeforeClass
    public static void init() {
        steps = new Steps(DeviceTest.class);
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
    public void test03_createDevice() throws InterruptedException {
        steps.createDevice();
    }

    @Test
    public void test04_getDevices() throws InterruptedException {
        steps.getDevices();
    }

    @Test
    public void test05_selfDevice() throws InterruptedException {
        steps.selfDevice();
    }

    @Test
    public void test06_updateDevice() throws InterruptedException {
        steps.updateDevice();
    }

    @Test
    public void test07_getDeviceUser() throws InterruptedException {
        steps.getDeviceUser();
    }

    @Test
    public void test08_getPaymentDevice() throws InterruptedException {
        steps.getPaymentDevice();
    }

    @Test
    public void test09_deleteDevices() throws InterruptedException {
        steps.deleteTestDevices();
    }


    @AfterClass
    public static void tearDown() throws Exception {
        steps.destroy();
        steps = null;
    }


}