package com.fitpay.android.paymentdevice;

import android.content.Context;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.paymentdevice.callbacks.DeviceSyncManagerCallback;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.paymentdevice.utils.sync.SyncThreadExecutor;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;

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
    private final static int SYNC_QUEUE_SIZE = 10;
    private final static int SYNC_THREADS_COUNT = 4;

    private static DeviceSyncManager sInstance;

    private final Context mContext;

    private final BlockingQueue<Runnable> requests;
    private final List<DeviceSyncManagerCallback> syncManagerCallbacks = new CopyOnWriteArrayList<>();

    private int queueSize;
    private int threadsCount;

    private SyncThreadExecutor worker;

    private List<String> dedupeSyncIds = new ArrayList<>(DEDUPE_LIMIT);

    private MessageListener mSyncListener;

    private boolean subscribed;

    /**
     * Initialize.
     * <p>
     * You should call {@link #subscribe()} to start listening to events and {@link #unsubscribe()} if you don't need it anymore
     *
     * @param context app context
     */
    public static DeviceSyncManager init(Context context) {
        if (sInstance == null) {
            sInstance = new DeviceSyncManager(context);
        }

        return getInstance();
    }

    public static DeviceSyncManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("DeviceSyncManager is not initialized. Call init() at first");
        }

        return sInstance;
    }

    private DeviceSyncManager(Context context) {
        this.mContext = context;
        queueSize = SYNC_QUEUE_SIZE;
        threadsCount = SYNC_THREADS_COUNT;
        requests = new ArrayBlockingQueue<>(queueSize);
        mSyncListener = new MessageListener();
    }

    /**
     * Subscribe to sync request events.
     * Start sync worker.
     */
    public void subscribe() {
        if (!subscribed) {
            subscribed = true;

            NotificationManager.getInstance().addListenerToCurrentThread(mSyncListener);

            worker = new SyncThreadExecutor(mContext, syncManagerCallbacks, queueSize, threadsCount, 5, TimeUnit.MINUTES, requests);
        }
    }

    /**
     * Unsubscribe from sync request events.
     * Stop sync worker.
     */
    public void unsubscribe() {
        if (subscribed) {
            subscribed = false;

            NotificationManager.getInstance().removeListener(mSyncListener);

            if (requests != null) {
                requests.clear();
            }

            dedupeSyncIds.clear();

            if (worker != null) {
                worker.shutdownNow();
            }
        }
    }

    /**
     * Add sync request to the queue
     *
     * @param request sync request
     */
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

    /**
     * Add sync callback
     *
     * @param syncManagerCallback callback
     */
    public void removeDeviceSyncManagerCallback(DeviceSyncManagerCallback syncManagerCallback) {
        if (syncManagerCallback == null) {
            return;
        }

        syncManagerCallbacks.remove(syncManagerCallback);
    }

    /**
     * Remove sync callback
     *
     * @param syncManagerCallback callback
     */
    public void registerDeviceSyncManagerCallback(DeviceSyncManagerCallback syncManagerCallback) {
        if (syncManagerCallback == null) {
            return;
        }

        syncManagerCallbacks.add(syncManagerCallback);
    }

    /**
     * Listen to Apdu and Sync callbacks
     */
    private class MessageListener extends Listener {

        private MessageListener() {
            super();
            mCommands.put(SyncRequest.class, data -> add((SyncRequest) data));
        }
    }
}

