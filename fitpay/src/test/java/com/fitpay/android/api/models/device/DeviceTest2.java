package com.fitpay.android.api.models.device;

import com.fitpay.android.TestActions;
import com.fitpay.android.api.callbacks.ResultProvidingCallback;
import com.fitpay.android.api.enums.ResetDeviceStatus;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.CreditCardInfo;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.utils.NamedResource;

import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by tgs on 4/21/16.
 */
public class DeviceTest2 extends TestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(DeviceTest2.class);

    @Test
    public void testCanAddDevice() throws Exception {
        Device device = getTestDevice();

        Device createdDevice = createDevice(user, device);

        assertNotNull("device", createdDevice);
        assertNotNull("device connectorId", createdDevice.getDeviceIdentifier());
        assertEquals("device name", device.getDeviceName(), createdDevice.getDeviceName());
        assertEquals("device type", device.getDeviceType(), createdDevice.getDeviceType());
        assertEquals("firmware version", device.getFirmwareRevision(), createdDevice.getFirmwareRevision());
    }

    @Test
    public void testCantAddDeviceWithMissingType() throws Exception {
        Device device = getPoorlyDefinedDevice();

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Device> callback = new ResultProvidingCallback<>(latch);
        user.createDevice(device, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Device createdDevice = callback.getResult();

        assertNull("created device", createdDevice);
        assertEquals("error code", 400, callback.getErrorCode());
    }

    @Test
    public void testCantAddDeviceWithMissingInfo() throws Exception {
        Device device = getPoorlyDeviceTestSmartStrapDevice();

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Device> callback = new ResultProvidingCallback<>(latch);
        user.createDevice(device, callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Device createdDevice = callback.getResult();

        assertNull("created device", createdDevice);
        assertEquals("error code", 400, callback.getErrorCode());
    }

    @Test
    public void testCanGetDevice() throws Exception {
        Device device = getTestDevice();

        Device createdDevice = createDevice(user, device);

        assertNotNull("device", createdDevice);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Device> callback = new ResultProvidingCallback<>(latch);
        createdDevice.self(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Device retrievedDevice = callback.getResult();

        assertNotNull("retrieved device", retrievedDevice);
        assertEquals("device connectorId", createdDevice.getDeviceIdentifier(), retrievedDevice.getDeviceIdentifier());

    }

    @Test
    public void testCanGetDeviceById() throws Exception {
        Device device = getTestDevice();

        Device createdDevice = createDevice(user, device);

        assertNotNull("device", createdDevice);

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Device> callback = new ResultProvidingCallback<>(latch);
        user.getDevice(createdDevice.getDeviceIdentifier(), callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Device retrievedDevice = callback.getResult();

        assertNotNull("device should have been retrieved", retrievedDevice);
        assertEquals("device connectorId", createdDevice.getDeviceIdentifier(), retrievedDevice.getDeviceIdentifier());

    }


    @Test
    public void testCanDevicesWhenOnlyOneInCollection() throws Exception {
        Device device = getTestDevice();

        Device createdDevice = createDevice(user, device);
        assertNotNull("device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);

        assertNotNull("retrieved devices", devices);
        assertEquals("number of devices", 1, devices.getTotalResults());

        Device firstDevice = devices.getResults().get(0);
        assertNotNull("first device", firstDevice);
        assertEquals("device connectorId", createdDevice.getDeviceIdentifier(), firstDevice.getDeviceIdentifier());

    }

    @Test
    public void testCanGetDeviceFromCollection() throws Exception {
        Device device = getTestDevice();

        Device createdDevice = createDevice(user, device);
        assertNotNull("device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);

        assertNotNull("retrieved devices", devices);
        assertEquals("number of devices", 1, devices.getTotalResults());

        Device firstDevice = devices.getResults().get(0);
        assertNotNull("first device", firstDevice);

        final CountDownLatch latch4 = new CountDownLatch(1);
        ResultProvidingCallback<Device> callback4 = new ResultProvidingCallback<>(latch4);
        createdDevice.self(callback4);
        latch4.await(TIMEOUT, TimeUnit.SECONDS);
        Device retrievedDevice = callback4.getResult();

        assertNotNull("retrieved device", retrievedDevice);
        assertEquals("device connectorId", firstDevice.getDeviceIdentifier(), retrievedDevice.getDeviceIdentifier());
    }


    @Test
    public void testCanDevicesWhenTwoInCollection() throws Exception {
        Device phone = getTestDevice(false);
        Device watch = getTestDevice(true);

        Device createdDevice = createDevice(user, phone);
        assertNotNull("device", createdDevice);

        Device anotherCreatedDevice = createDevice(user, watch);
        assertNotNull("device", anotherCreatedDevice);

        Collections.DeviceCollection devices = getDevices(user);

        assertNotNull("retrieved devices", devices);
        assertEquals("number of devices", 2, devices.getTotalResults());

        Device firstDevice = devices.getResults().get(0);
        assertNotNull("first device", firstDevice);
        //TODO: sometimes server returns second device in position [0]
        //assertEquals("device connectorId", createdDevice.getDeviceIdentifier(), firstDevice.getDeviceIdentifier());

        Device secondDevice = devices.getResults().get(1);
        assertNotNull("second device", secondDevice);
        assertFalse("device ids in collection should not be equal", firstDevice.getDeviceIdentifier().equals(secondDevice.getDeviceIdentifier()));

    }

    @Test
    public void testCanDeleteDeviceFromCollection() throws Exception {
        Device phone = getTestDevice(false);
        Device watch = getTestDevice(true);

        Device createdDevice = createDevice(user, phone);
        assertNotNull("device", createdDevice);

        Device anotherCreatedDevice = createDevice(user, watch);
        assertNotNull("device", anotherCreatedDevice);

        Collections.DeviceCollection devices = getDevices(user);

        assertNotNull("retrieved devices", devices);
        assertEquals("number of devices", 2, devices.getTotalResults());

        final CountDownLatch latch = new CountDownLatch(1);
        ResultProvidingCallback<Void> callback = new ResultProvidingCallback<>(latch);
        createdDevice.deleteDevice(callback);
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("delete device error code", -1, callback.getErrorCode());

        devices = getDevices(user);

        assertNotNull("retrieved devices", devices);
        assertEquals("number of devices after delete of 1", 1, devices.getTotalResults());

        assertEquals("remaining device in collection", anotherCreatedDevice.getDeviceIdentifier(), devices.getResults().get(0).getDeviceIdentifier());
    }

    @Test
    public void testCanResetDevice() throws Exception {
        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);

        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        Collections.CreditCardCollection creditCards = getCreditCards(user);
        assertNotNull("credit cards collection", creditCards);
        assertEquals("number of credit cards", 1, creditCards.getTotalResults());

        CountDownLatch latch = new CountDownLatch(1);

        final AtomicReference<String> status = new AtomicReference<>();

        createdDevice.resetDevice().flatMap(resetDeviceResult -> resetDeviceResult.getStatus()
                .toFlowable()
                .repeatWhen(objectFlowable -> objectFlowable.delay(2, TimeUnit.SECONDS))
                .takeUntil(o -> !ResetDeviceStatus.IN_PROGRESS.equals(status.get()))
                .lastOrError())
                .subscribe(resetDeviceResult -> {
                    status.set(resetDeviceResult.getResetStatus());
                    latch.countDown();
                }, throwable -> {
                });

        latch.await(60, TimeUnit.SECONDS);
        assertEquals("reset device status", ResetDeviceStatus.RESET_COMPLETE, status.get());
    }
}
