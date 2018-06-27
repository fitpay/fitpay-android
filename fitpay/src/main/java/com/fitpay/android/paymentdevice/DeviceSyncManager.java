package com.fitpay.android.paymentdevice;

import android.content.Context;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.paymentdevice.callbacks.DeviceSyncManagerCallback;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.paymentdevice.utils.sync.SyncThreadExecutor;
import com.fitpay.android.utils.FPLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Device sync manager which can work with multiple devices
 */
public class DeviceSyncManager {
    private final static int DEDUPE_LIMIT = 100;

    private final Context mContext;

    private final BlockingQueue<Runnable> requests;
    private final List<DeviceSyncManagerCallback> syncManagerCallbacks = new CopyOnWriteArrayList<>();

    private int queueSize;
    private int threadsCount;

    private SyncThreadExecutor worker;

    private List<String> dedupeSyncIds = new ArrayList<>(DEDUPE_LIMIT);

    public DeviceSyncManager(Context context) {
        this.mContext = context;
        queueSize = FitpayConfig.getInstance().get(FitpayConfig.PROPERTY_SYNC_QUEUE_SIZE);
        threadsCount = FitpayConfig.getInstance().get(FitpayConfig.PROPERTY_SYNC_THREADS_COUNT);
        requests = new ArrayBlockingQueue<>(queueSize);
    }

    public void onCreate() {
        worker = new SyncThreadExecutor(mContext, syncManagerCallbacks, queueSize, threadsCount, 5, TimeUnit.MINUTES, requests);
    }

    public void onDestroy() {
        if (requests != null) {
            requests.clear();
        }

        if (worker != null) {
            worker.shutdownNow();
        }
    }

    public void add(final SyncRequest request) {
        if (request == null) {
            return;
        }

        // if we have a syncId, dedupe it to avoid re-running syncs arriving through multiple channels
        if (request.getSyncId() != null) {
            if (dedupeSyncIds.contains(request.getSyncId())) {
                FPLog.i("duplicated sync received, skipping: " + request);
                return;
            }

            dedupeSyncIds.add(request.getSyncId());

            // ensure the dedupe size remains consistent and doesn't grow unbounded
            while (dedupeSyncIds.size() > DEDUPE_LIMIT) {
                dedupeSyncIds.remove(0);
            }
        }

        SyncInfo syncInfo = request.getSyncInfo();
        if (syncInfo != null) {
            syncInfo.sendAckSync(request.getSyncId(), new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    FPLog.i("ackSync has been sent successfully.");
                }

                @Override
                public void onFailure(int errorCode, String errorMessage) {
                    FPLog.w("ackSync failed to send.");
                }
            });
        }

        worker.addTask(request);

        FPLog.d("added sync request to queue for processing, current queue size [" + requests.size() + "]: " + request);

        for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
            callback.syncRequestAdded(request);
        }
    }

    public void removeDeviceSyncManagerCallback(DeviceSyncManagerCallback syncManagerCallback) {
        if (syncManagerCallback == null) {
            return;
        }

        syncManagerCallbacks.remove(syncManagerCallback);
    }

    public void registerDeviceSyncManagerCallback(DeviceSyncManagerCallback syncManagerCallback) {
        if (syncManagerCallback == null) {
            return;
        }

        syncManagerCallbacks.add(syncManagerCallback);
    }
}

