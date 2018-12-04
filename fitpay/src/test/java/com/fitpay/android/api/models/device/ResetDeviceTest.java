package com.fitpay.android.api.models.device;

import com.fitpay.android.TestActions;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResetDeviceStatus;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.NamedResource;

import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResetDeviceTest extends TestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(ResetDeviceTest.class);

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

        Single.<ResetDeviceResult>create(emitter -> {
            ApiManager.getInstance().resetPaymentDevice(user.getId(), createdDevice.getDeviceIdentifier(), new ApiCallback<ResetDeviceResult>() {
                @Override
                public void onSuccess(ResetDeviceResult result) {
                    emitter.onSuccess(result);
                }

                @Override
                public void onFailure(int errorCode, String errorMessage) {
                    emitter.onError(new Exception(errorMessage));
                }
            });
        }).flatMap(resetDeviceResult -> Single.create(emitter -> {
            ApiManager.getInstance().getResetPaymentDeviceStatus(resetDeviceResult.getResetId(), new ApiCallback<ResetDeviceResult>() {
                @Override
                public void onSuccess(ResetDeviceResult result) {
                    String resetStatus = result.getResetStatus();
                    status.set(resetStatus);
                    emitter.onSuccess(resetStatus);
                }

                @Override
                public void onFailure(int errorCode, String errorMessage) {
                    emitter.onError(new Exception(errorMessage));
                }
            });
        }).repeatWhen(objectFlowable -> objectFlowable.delay(10, TimeUnit.SECONDS))
                .takeUntil(o -> status.get() != null && !ResetDeviceStatus.IN_PROGRESS.equals(status.get()))
                .lastOrError()).subscribe(resetStatus -> latch.countDown(), throwable -> FPLog.e(throwable.toString()));

        latch.await(120, TimeUnit.SECONDS);

        assertEquals("reset device status", ResetDeviceStatus.RESET_COMPLETE, status.get());
    }

}