package com.fitpay.android.paymentdevice.utils.sync;

import android.content.Context;

import com.fitpay.android.paymentdevice.callbacks.DeviceSyncManagerCallback;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.models.SyncProcess;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vlad on 24.07.2017.
 */

public class SyncThreadExecutor extends ThreadPoolExecutor {

    private static final String TAG = SyncThreadExecutor.class.getSimpleName();

    private final HashMap<String, BlockingQueue<SyncRequest>> syncBuffer;

    private final List<String> inWork = new ArrayList<>();

    private final Context mContext;
    private final List<DeviceSyncManagerCallback> syncManagerCallbacks;

    private final int queueSize;

    public SyncThreadExecutor(Context context, List<DeviceSyncManagerCallback> syncManagerCallbacks, int queueSize, int threadsCount, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(threadsCount, threadsCount, keepAliveTime, unit, workQueue);
        this.mContext = context;
        this.syncManagerCallbacks = syncManagerCallbacks;
        this.queueSize = queueSize;
        this.syncBuffer = new HashMap<>(threadsCount);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        SyncWorkerTask task = (SyncWorkerTask) r;
        inWork.add(task.getSyncRequest().getDevice().getDeviceIdentifier());

        for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
            callback.syncTaskStarting(task.getSyncRequest());
        }

        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        SyncWorkerTask task = (SyncWorkerTask) r;
        inWork.remove(task.getSyncRequest().getDevice().getDeviceIdentifier());

        for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
            callback.syncTaskCompleted(task.getSyncRequest());
        }

        for (String key : syncBuffer.keySet()) {
            if (!inWork.contains(key)) {
                execute(new SyncWorkerTask(mContext, syncManagerCallbacks, syncBuffer.get(key).poll()));
            }
            break;
        }

        super.afterExecute(r, t);
    }

    /**
     * Add task to the queue
     *
     * @param request
     */
    public void addTask(SyncRequest request) {
        if (canExecuteRequest(request)) {
            for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
                callback.syncRequestAdded(request);
            }

            String deviceId = request.getDevice().getDeviceIdentifier();
            if (inWork.contains(deviceId)) {
                BlockingQueue<SyncRequest> deviceQueue = syncBuffer.get(deviceId);
                if (deviceQueue == null) {
                    deviceQueue = new ArrayBlockingQueue<>(queueSize);
                }
                deviceQueue.add(request);
            } else {
                execute(new SyncWorkerTask(mContext, syncManagerCallbacks, request));
            }

        } else {
            for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
                callback.syncRequestFailed(request);
            }

            //we can't process current sync request. lets send empty metrics
            SyncProcess emptyProcess = new SyncProcess(request);
            emptyProcess.start();
            emptyProcess.finish();
        }
    }

    /**
     * Can we execute current request
     *
     * @param syncRequest
     * @return true/false
     */
    private boolean canExecuteRequest(SyncRequest syncRequest) {
        if (syncRequest == null) {
            FPLog.w(TAG, "No syncRequest provided");
            return false;
        }

        String errorMsg = null;
        String syncId = syncRequest.getSyncId();

        if (syncRequest.getConnector() == null) {
            errorMsg = "No payment device connector configured in syncRequest: " + syncId;

            FPLog.w(TAG, errorMsg);

            RxBus.getInstance().post(Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.SKIPPED)
                    .message(errorMsg)
                    .build());

            return false;
        }

        if (syncRequest.getUser() == null) {
            errorMsg = "No user provided in syncRequest: " + syncId;
        }

        if (syncRequest.getDevice() == null) {
            errorMsg = "No payment device connector configured in syncRequest: " + syncId;
        }

        if (!StringUtils.isEmpty(errorMsg)) {
            FPLog.w(TAG, errorMsg);

            RxBus.getInstance().post(syncRequest.getConnector().id(), Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.SKIPPED)
                    .message(errorMsg)
                    .build());

            return false;
        }

        return true;
    }
}
