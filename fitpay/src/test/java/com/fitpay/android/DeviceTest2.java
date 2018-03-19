package com.fitpay.android;

import com.fitpay.android.api.callbacks.ResultProvidingCallback;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Device;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Created by tgs on 4/21/16.
 */
public class DeviceTest2 extends TestActions {

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
        assertEquals("device connectorId", createdDevice.getDeviceIdentifier(), firstDevice.getDeviceIdentifier());

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


}
