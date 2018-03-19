package com.fitpay.android.paymentdevice.impl;

import com.fitpay.android.TestActions;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.callbacks.ApduExecutionListener;
import com.fitpay.android.paymentdevice.callbacks.PaymentDeviceListener;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.events.PaymentDeviceOperationFailed;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by tgs on 5/3/16.
 */
public class MockPaymentDeviceTest extends TestActions {

    private final static String TAG = MockPaymentDeviceTest.class.getSimpleName();

    private IPaymentDeviceConnector paymentDeviceService;

    private NotificationManager manager;
    private Listener listener;

    @Before
    public void setUp() throws Exception {
        paymentDeviceService = new MockPaymentDeviceConnector();
        manager = NotificationManager.getInstance();
    }

    @Override
    public void testActionsSetup() throws Exception {
    }

    @After
    public void teardown() {
        if (null != listener) {
            manager.removeListener(listener);
        }
    }

    @Test
    public void canConnect() {
        assertEquals("payment service is not initialized", States.INITIALIZED, paymentDeviceService.getState());
        CountDownLatch latch = new CountDownLatch(1);
        ConnectPaymentDeviceListener listener = new ConnectPaymentDeviceListener(paymentDeviceService.id(), latch);
        this.listener = listener;

        manager.addListenerToCurrentThread(listener);

        paymentDeviceService.connect();

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("payment service should be connected", States.CONNECTED, paymentDeviceService.getState());
        assertEquals("connection state as captured by listener", States.CONNECTED, listener.getState());
    }

    @Test
    public void canDetectConnectTimeout() {
        Properties props = new Properties();
        props.put(MockPaymentDeviceConnector.CONFIG_CONNECTED_RESPONSE_TIME, "0");
        paymentDeviceService.init(props);
        assertEquals("payment service is not initialized", States.INITIALIZED, paymentDeviceService.getState());
        CountDownLatch latch = new CountDownLatch(1);
        ConnectPaymentDeviceListener listener = new ConnectPaymentDeviceListener(paymentDeviceService.id(), latch);
        this.listener = listener;

        manager.addListenerToCurrentThread(listener);

        paymentDeviceService.connect();

        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("payment service should be connected", States.CONNECTED, paymentDeviceService.getState());
        assertEquals("connection state as captured by listener", States.CONNECTED, listener.getState());
    }

    @Test
    public void canReadDeviceInfo() {
        CountDownLatch latch = new CountDownLatch(1);
        ReadDeviceInfoPaymentDeviceListener listener = new ReadDeviceInfoPaymentDeviceListener(paymentDeviceService.id(), latch);
        this.listener = listener;

        manager.addListenerToCurrentThread(listener);

        paymentDeviceService.readDeviceInfo();

        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull("device info as captured by listener", listener.getDevice());
    }

    @Test
    public void canProcessApduPackage() {
        CountDownLatch latch = new CountDownLatch(1);
        ApduListener listener = new ApduListener(latch);
        this.listener = listener;

        manager.addListenerToCurrentThread(listener);

        ApduPackage apduPackage = getTestApduPackage();

        paymentDeviceService.executeApduPackage(apduPackage);

        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull("apdu execution result as captured by listener", listener.getResult());
        assertEquals("apdu execution state", ResponseState.PROCESSED, listener.getResult().getState());
    }

    @Test
    public void canProcessApduPackageFailure() {
        CountDownLatch latch = new CountDownLatch(1);
        ApduListener listener = new ApduListener(latch);
        this.listener = listener;

        manager.addListenerToCurrentThread(listener);

        ApduPackage apduPackage = getFailingTestApduPackage();

        paymentDeviceService.executeApduPackage(apduPackage);

        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull("apdu execution result as captured by listener", listener.getResult());
        assertEquals("apdu execution state", ResponseState.FAILED, listener.getResult().getState());
    }


    protected class ConnectPaymentDeviceListener extends PaymentDeviceListener {

        protected CountDownLatch latch;
        protected int state;
        protected PaymentDeviceOperationFailed failure;

        public ConnectPaymentDeviceListener(String filter, CountDownLatch latch) {
            super(filter);
            this.latch = latch;
        }

        public int getState() {
            return state;
        }

        public PaymentDeviceOperationFailed getFailure() {
            return failure;
        }

        @Override
        public void onDeviceStateChanged(@Connection.State int state) {
            this.state = state;
            if (States.CONNECTED == state) {
                latch.countDown();
            }
        }

        @Override
        public void onDeviceOperationFailed(PaymentDeviceOperationFailed failure) {

        }

        @Override
        public void onDeviceInfoReceived(Device device) {
            // do nothing
        }

        @Override
        public void onNFCStateReceived(boolean isEnabled, byte errorCode) {
            // do nothing
        }

        @Override
        public void onNotificationReceived(byte[] data) {
            // do nothing
        }

        @Override
        public void onApplicationControlReceived(byte[] data) {
            // do nothing
        }

    }

    protected class ReadDeviceInfoPaymentDeviceListener extends ConnectPaymentDeviceListener {

        private Device device;

        public ReadDeviceInfoPaymentDeviceListener(String filter, CountDownLatch latch) {
            super(filter, latch);
        }

        public Device getDevice() {
            return device;
        }

        @Override
        public void onDeviceStateChanged(@Connection.State int state) {
            this.state = state;
        }

        @Override
        public void onDeviceOperationFailed(PaymentDeviceOperationFailed failure) {

        }

        @Override
        public void onDeviceInfoReceived(Device device) {
            this.device = device;
            latch.countDown();
        }
    }


    protected class ApduListener extends ApduExecutionListener {

        protected CountDownLatch latch;
        protected ApduExecutionResult result;

        public ApduListener(CountDownLatch latch) {
            this.latch = latch;
        }

        public ApduExecutionResult getResult() {
            return result;
        }

        @Override
        public void onApduPackageErrorReceived(ApduExecutionResult result) {
            this.result = result;
            latch.countDown();

        }

        @Override
        public void onApduPackageResultReceived(ApduExecutionResult result) {
            this.result = result;
            latch.countDown();

        }
    }
}
