package com.fitpay.android.paymentdevice.utils.sync;

import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;

import com.fitpay.android.paymentdevice.callbacks.DeviceSyncManagerCallback;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncProcess;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private Handler handler;

    public SyncThreadExecutor(Context context, List<DeviceSyncManagerCallback> syncManagerCallbacks, int queueSize, int threadsCount, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(threadsCount, threadsCount, keepAliveTime, unit, workQueue);
        this.mContext = context;
        this.syncManagerCallbacks = syncManagerCallbacks;
        this.queueSize = queueSize;
        this.syncBuffer = new HashMap<>(threadsCount);
        handler = new Handler();
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
        super.afterExecute(r, t);

        SyncWorkerTask task = (SyncWorkerTask) r;
        inWork.remove(task.getSyncRequest().getDevice().getDeviceIdentifier());

        for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
            callback.syncTaskCompleted(task.getSyncRequest());
        }

        for (Iterator<Map.Entry<String, BlockingQueue<SyncRequest>>> it = syncBuffer.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, BlockingQueue<SyncRequest>> entry = it.next();
            if (!inWork.contains(entry.getKey())) {
                BlockingQueue<SyncRequest> queue = entry.getValue();
                SyncRequest request = entry.getValue().poll();
                if(queue.size() == 0){
                    it.remove();
                }
                if (request != null) {
                    handler.postDelayed(()-> {
                        if(!isShutdown() && !isTerminated() && !isTerminating()) {
                            execute(new SyncWorkerTask(mContext, syncManagerCallbacks, request));
                        }
                    }, 100);
                    break;
                }
            }
        }
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
                if(deviceQueue.size() < queueSize) {
                    deviceQueue.add(request);
                    syncBuffer.put(deviceId, deviceQueue);
                } else {
                    Log.w(TAG, "Queue is full");
                }
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

        SyncInfo syncInfo = syncRequest.getSyncInfo();
        if (syncInfo != null){
            String userId = syncInfo.getUserId();
            String deviceId = syncInfo.getDeviceId();
            if((!StringUtils.isEmpty(userId) && !userId.equals(syncRequest.getUser().getId())) ||
                    (!StringUtils.isEmpty(deviceId) && !deviceId.equals(syncRequest.getDevice().getDeviceIdentifier()))){
                errorMsg = "Skip that sync. It was from another connector";
            }
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
