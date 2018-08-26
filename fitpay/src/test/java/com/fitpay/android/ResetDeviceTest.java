package com.fitpay.android;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResetDeviceStatus;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.device.ResetDeviceResult;

import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.functions.Func1;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class ResetDeviceTest extends TestActions {

    @Test
    public void testResetDevice() throws Exception {
        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("device", createdDevice);

        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        CountDownLatch latch = new CountDownLatch(1);

        final AtomicReference<String> status = new AtomicReference<>();

        rx.Observable.create((Observable.OnSubscribe<ResetDeviceResult>) subscriber ->
                ApiManager.getInstance().resetPaymentDevice(user.getId(), createdDevice.getDeviceIdentifier(), new ApiCallback<ResetDeviceResult>() {
                    @Override
                    public void onSuccess(ResetDeviceResult result) {
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMessage) {
                        subscriber.onError(new Exception(errorMessage));
                    }
                })).flatMap(resetDeviceResult -> Observable.create((Observable.OnSubscribe<String>) subscriber ->
                ApiManager.getInstance().getResetPaymentDeviceStatus(resetDeviceResult.getResetId(), new ApiCallback<ResetDeviceResult>() {
                    @Override
                    public void onSuccess(ResetDeviceResult result) {
                        String resetStatus = result.getResetStatus();
                        status.set(resetStatus);

                        subscriber.onNext(resetStatus);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMessage) {
                        subscriber.onError(new Exception(errorMessage));
                    }
                }))
                .repeatWhen(observable -> observable.flatMap((Func1<Void, Observable<?>>) aVoid -> {
                    if (status.get() == null || ResetDeviceStatus.IN_PROGRESS.equals(status.get())) {
                        return Observable.timer(10, TimeUnit.SECONDS);
                    } else {
                        return Observable.just(null);
                    }
                }).takeWhile(Objects::nonNull)))
                .subscribe(resetStatus -> {
                }, throwable -> {
                }, latch::countDown);

        latch.await(120, TimeUnit.SECONDS);
        assertEquals("reset device status", ResetDeviceStatus.RESET_COMPLETE, status.get());
    }
}