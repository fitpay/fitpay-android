package com.fitpay.android.paymentdevice;

import android.content.Context;

import com.fitpay.android.R;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.CommitConfirm;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.callbacks.IListeners;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSkipped;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.paymentdevice.utils.DevicePreferenceData;
import com.fitpay.android.utils.EventCallback;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.events.DeviceStatusMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;

import static com.fitpay.android.utils.Constants.SYNC_DATA;

/**
 * Device sync manager can work with one device only, all new sync request will be put in a queue
 */
public class DeviceSyncManager {
    private final static String TAG = DeviceSyncManager.class.getSimpleName();

    private final Context mContext;
    private final BlockingQueue<SyncRequest> requests;

    private SyncWorkerThread worker = null;

    private List<DeviceSyncManagerCallback> syncManagerCallbacks = new ArrayList<>();

    /**
     * @param context
     * @param executor
     * @deprecated executor is no longer utilized
     */
    public DeviceSyncManager(Context context, Executor executor) {
        this(context);
    }

    public DeviceSyncManager(Context context) {
        this.mContext = context;

        int queueSize = Integer.parseInt(ApiManager.getConfig().get(ApiManager.PROPERTY_SYNC_QUEUE_SIZE));
        requests = new ArrayBlockingQueue<>(queueSize);
    }

    /**
     * This interface is more about testing and allowing unit tests to inject hooks into the process making this
     * class more testable.
     */
    public interface DeviceSyncManagerCallback {
        void syncRequestAdded(SyncRequest request);

        void syncTaskStarting(SyncRequest request);

        void syncTaskStarted(SyncRequest request);

        void syncTaskCompleted(SyncRequest request);
    }

    public void onCreate() {
        worker = new SyncWorkerThread(mContext, requests);
        worker.setName("DeviceSyncManagerWorkerThread");
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }

    public void onDestroy() {
        if (requests != null) {
            requests.clear();
        }

        if (worker != null) {
            try {
                worker.shutdown();
                worker.join();
            } catch (InterruptedException e) {
                FPLog.e("error while waiting for sync worker to properly shutdown", e);
            }
        }
    }

    public void add(final SyncRequest request) {
        if (request == null) {
            return;
        }

        try {
            requests.put(request);

            FPLog.d("added sync request to queue for processing, current queue size [" + requests.size() + "]: " + request);

            for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
                callback.syncRequestAdded(request);
            }
        } catch (InterruptedException e) {
            FPLog.w("interrupted exception while waiting to add sync request to processing queue");
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

    /**
     * This is the real work horse of sync, it's launched by the {@link SyncWorkerThread} for an individual {@link SyncRequest}
     * where it will establish a listener, kick off the sync, and orchastrate it's flow through a listener receiving events
     * back for each commit in the sync workflow.
     */
    private class SyncWorkerTask implements Runnable {
        private final Context mContext;
        private final SyncRequest syncRequest;
        private final ScheduledExecutorService timeoutWatcherExecutor;

        private List<Commit> commits;
        private final CountDownLatch completionLatch = new CountDownLatch(1);

        private ScheduledFuture<Void> commitWarningTimer;
        private ScheduledFuture<Void> commitTimeoutTimer;

        private final int commitWarningTimeout = Integer.parseInt(ApiManager.getConfig().get(ApiManager.PROPERTY_COMMIT_WARNING_TIMEOUT));
        private final int commitErrorTimeout = Integer.parseInt(ApiManager.getConfig().get(ApiManager.PROPERTY_COMMIT_ERROR_TIMEOUT));

        private String pendingCommitId;

        public SyncWorkerTask(Context mContext, ScheduledExecutorService timeoutWatcherExecutor, SyncRequest syncRequest) {
            this.mContext = mContext;
            this.syncRequest = syncRequest;
            this.timeoutWatcherExecutor = timeoutWatcherExecutor;
        }

        public void run() {
            SyncListener listener = new SyncListener();
            NotificationManager.getInstance().addListenerToCurrentThread(listener);

            Exception err = null;
            try {
                for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
                    callback.syncTaskStarted(syncRequest);
                }

                sync();

                completionLatch.await();

                // tell the connector we're done
                syncRequest.getConnector().syncComplete();

                FPLog.d(SYNC_DATA, "task completed for syncRequest: "
                        + syncRequest
                        + ", commitSuccess: "
                        + listener.getCommitSuccessCount()
                        + ", commitFailed: "
                        + listener.getCommitFailedCount()
                        + ", commitSkipped: "
                        + listener.getCommitSkippedCount());
            } catch (Exception e) {
                FPLog.e(TAG, e);
                err = e;
            } finally {
                NotificationManager.getInstance().removeListener(listener);
            }
        }

        public void sync() {
            if (syncRequest == null) {
                FPLog.d("sync skipped, syncRequest is null");
                return;
            }

            RxBus.getInstance().post(Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.STARTED)
                    .build());

            if (syncRequest.getUser() == null) {
                FPLog.w(TAG, "No user provided in syncRequest: " + syncRequest);

                RxBus.getInstance().post(Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .state(States.SKIPPED)
                        .message("No user provided in current syncRequest: " + syncRequest)
                        .build());

                return;
            }

            if (syncRequest.getDevice() == null) {
                FPLog.w(TAG, "No payment device connector configured in syncRequest: " + syncRequest);

                RxBus.getInstance().post(Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .state(States.SKIPPED)
                        .message("No payment device provided in current syncRequest: " + syncRequest)
                        .build());

                return;
            }

            if (syncRequest.getConnector() == null) {
                FPLog.w(TAG, "No payment device connector configured in syncRequest: " + syncRequest);

                RxBus.getInstance().post(Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .state(States.SKIPPED)
                        .message("No payment device provided in current syncRequest: " + syncRequest)
                        .build());
                return;
            }

            if (syncRequest.getConnector().getState() != States.CONNECTED) {
                FPLog.w(TAG, "Payment device is not in a CONNECTED state, syncRequest: " + syncRequest);

                RxBus.getInstance().post(Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .state(States.SKIPPED)
                        .message("Payment device is not currently connected (" + syncRequest.getConnector().getState() + ": " + syncRequest)
                        .build());

                return;
            }

            FPLog.d(TAG, "sync initiated from thread: " + Thread.currentThread() + ", " + Thread.currentThread().getName() + ", syncRequest: " + syncRequest);

            syncRequest.getConnector().setUser(syncRequest.getUser());

            syncDevice();
        }

        private void syncDevice() {
            String deviceId = syncRequest.getDevice().getDeviceIdentifier();

            RxBus.getInstance().post(
                    new DeviceStatusMessage(
                            mContext.getString(R.string.fp_checking_wallet_updates),
                            deviceId,
                            DeviceStatusMessage.SUCCESS));


            // tell connector the sync is starting so it can perform an pre-init work
            syncRequest.getConnector().syncInit();

            // load the stored device data so we can figure out exactly where the last sync left off
            DevicePreferenceData deviceData = DevicePreferenceData.load(mContext, deviceId);

            // get all the new commits from the last commit pointer processed
            FPLog.d(TAG, "retrieving commits from the lastCommitId: " + deviceData.getLastCommitId() + ", for syncRequest: " + syncRequest);

            Device device = syncRequest.getDevice();
            Observable<com.fitpay.android.api.models.collection.Collections.CommitsCollection> observable = null;

            String lastCommitId = deviceData.getLastCommitId();

            if (StringUtils.isEmpty(lastCommitId) && syncRequest.useLastAckCommit() && device.hasLastAckCommit()) {
                observable = device.getAllCommitsAfterLastAckCommit();
            } else {
                observable = device.getAllCommits(lastCommitId);
            }

            observable.compose(RxBus.applySchedulersExecutorThread())
                    .subscribe(
                            commitsCollection -> {
                                commits = commitsCollection.getResults();
                                commits = commits == null ? Collections.emptyList() : commits;

                                FPLog.i(SYNC_DATA, "Commits Received: " + commits.size());

                                if (commits.size() > 0) {
                                    RxBus.getInstance().post(new DeviceStatusMessage(
                                            mContext.getString(R.string.fp_updates_available),
                                            deviceId,
                                            DeviceStatusMessage.SUCCESS));

                                    RxBus.getInstance().post(new DeviceStatusMessage(
                                            mContext.getString(R.string.fp_sync_started),
                                            deviceId,
                                            DeviceStatusMessage.PROGRESS));

                                    processNextCommit();
                                } else {
                                    RxBus.getInstance().post(new DeviceStatusMessage(
                                            mContext.getString(R.string.fp_no_pending_updates),
                                            deviceId,
                                            DeviceStatusMessage.SUCCESS));

                                    RxBus.getInstance().post(Sync.builder()
                                            .syncId(syncRequest.getSyncId())
                                            .state(States.COMPLETED_NO_UPDATES)
                                            .build());
                                }
                            },
                            throwable -> {
                                FPLog.e(TAG, throwable);

                                if (throwable instanceof DeviceOperationException) {
                                    DeviceOperationException doe = (DeviceOperationException) throwable;
                                    FPLog.e(TAG, "get commits failed.  reasonCode: " + doe.getErrorCode() + ",  " + doe.getMessage());

                                } else {
                                    FPLog.e(TAG, "get commits failed. " + throwable.getMessage());
                                }

                                RxBus.getInstance().post(Sync.builder()
                                        .syncId(syncRequest.getSyncId())
                                        .state(States.FAILED)
                                        .message(throwable.getMessage())
                                        .build());
                            });
        }

        private void processNextCommit() {
            // cancel the current timers if they're set, this shouldn't occur... but just in case
            if (commitWarningTimer != null) {
                boolean result = commitWarningTimer.cancel(true);
                FPLog.d(TAG, "commitWarningTimer cancel: " + result);
            }

            if (commitTimeoutTimer != null) {
                boolean result = commitTimeoutTimer.cancel(true);
                FPLog.d(TAG, "commitTimeoutTimer cancel: " + result);
            }

            // make sure the device is still connected
            switch (syncRequest.getConnector().getState()) {
                case States.DISCONNECTED:
                case States.DISCONNECTING:
                    RxBus.getInstance().post(Sync.builder()
                            .syncId(syncRequest.getSyncId())
                            .state(States.FAILED)
                            .message("Error processing next commit, device is disconnected or disconnecting")
                            .build());
                    return;
            }

            if (commits.size() > 0) {

                RxBus.getInstance().post(Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .value(commits.size())
                        .build());

                Commit commit = commits.remove(0);

                FPLog.i(SYNC_DATA, "Process Next Commit: " + commit);

                pendingCommitId = commit.getCommitId();

                // start the watching timers, this first timer is responsible for producing a warning
                // if a commit isn't responded to in a timely manner
                boolean commitTimersEnabled = true;
                if (ApiManager.getConfig().containsKey(ApiManager.PROPERTY_COMMIT_TIMERS_ENABLED)) {
                    commitTimersEnabled = "true".equals(ApiManager.getConfig().get(ApiManager.PROPERTY_COMMIT_TIMERS_ENABLED));
                }

                if (commitTimersEnabled) {
                    commitWarningTimer = timeoutWatcherExecutor.schedule(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            FPLog.w(TAG, "warning, commit " + commit + " has not returned within " + commitWarningTimer + "ms");

                            return null;
                        }
                    }, commitWarningTimeout, TimeUnit.MILLISECONDS);

                    // this is the timeout timer that'll basically kill the sync if a commit isn't responded too
                    commitTimeoutTimer = timeoutWatcherExecutor.schedule(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            FPLog.e(TAG, "error, commit timeout " + commit + " has not returned within " + commitErrorTimeout + "ms");

                            RxBus.getInstance().post(Sync.builder()
                                    .syncId(syncRequest.getSyncId())
                                    .state(States.TIMEOUT)
                                    .message("sync timeout, this is typically due to a commit event being sent to the payment device connector and a commit event not being pushed to RxBus")
                                    .build());

                            return null;
                        }
                    }, commitErrorTimeout, TimeUnit.MILLISECONDS);
                } else {
                    FPLog.d(TAG, "skipped commit timers, they're turned off in ApiManager configuration");
                }

                // call the payment connector
                syncRequest.getConnector().processCommit(commit);

                // expose the commit out to others who may want to take action
                RxBus.getInstance().post(commit);
            } else {
                RxBus.getInstance().post(Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .state(States.COMPLETED)
                        .build());
            }
        }

        private class SyncListener extends Listener implements IListeners.SyncListener {
            private final AtomicInteger commitSuccessCounter = new AtomicInteger();
            private final AtomicInteger commitSkippedCounter = new AtomicInteger();
            private final AtomicInteger commitFailedCounter = new AtomicInteger();

            private SyncListener() {
                super();

                mCommands.put(Sync.class, data -> onSyncStateChanged((Sync) data));
                mCommands.put(CommitSuccess.class, data -> onCommitSuccess((CommitSuccess) data));
                mCommands.put(CommitFailed.class, data -> onCommitFailed((CommitFailed) data));
                mCommands.put(CommitSkipped.class, data -> onCommitSkipped((CommitSkipped) data));
            }

            @Override
            public void onSyncStateChanged(Sync syncEvent) {
                FPLog.d(SYNC_DATA, "onSyncStateChanged: " + syncEvent);

                switch (syncEvent.getState()) {
                    case States.STARTED:
                        FPLog.d(TAG, "sync started: " + syncEvent);
                        break;

                    case States.COMPLETED:
                    case States.COMPLETED_NO_UPDATES:
                    case States.FAILED:
                    case States.SKIPPED:
                    case States.TIMEOUT:
                        if (syncRequest == null) {
                            FPLog.i(TAG, "no current sync request on sync event: " + syncEvent);
                        }

                        completionLatch.countDown();

                        break;

                    case States.COMMIT_COMPLETED:
                        processNextCommit();
                        break;

                    default:
                        FPLog.d(TAG, "unrecognized/handled syncEvent: " + syncEvent);
                        break;

                }
            }

            @Override
            public void onCommitSuccess(CommitSuccess commitSuccess) {
                if (!commitSuccess.getCommitId().equals(pendingCommitId)) {
                    FPLog.w(SYNC_DATA, "Unexpected CommitSuccess " + commitSuccess + " received, expected commitId " + pendingCommitId);
                    return;
                }

                FPLog.i(SYNC_DATA, "Commit Success: " + commitSuccess);
                commitSuccessCounter.incrementAndGet();

                cancelCommitTimers();

                moveLastCommitPointer(commitSuccess.getCommitId());

                confirmCommit(commitSuccess.getCommit(), new CommitConfirm(ResponseState.SUCCESS));

                EventCallback eventCallback = new EventCallback.Builder()
                        .setCommand(EventCallback.getCommandForCommit(commitSuccess.getCommit()))
                        .setStatus(EventCallback.STATUS_OK)
                        .setTimestamp(commitSuccess.getCreatedTs())
                        .build();
                eventCallback.send();

                // move onto the next commit
                processNextCommit();
            }

            @Override
            public void onCommitFailed(CommitFailed commitFailed) {
                if (!commitFailed.getCommitId().equals(pendingCommitId)) {
                    FPLog.w(SYNC_DATA, "Unexpected CommitFailed " + commitFailed + " received, expected commitId " + pendingCommitId);
                    return;
                }

                FPLog.w(SYNC_DATA, "Commit Failed: " + commitFailed);
                commitFailedCounter.incrementAndGet();

                cancelCommitTimers();

                moveLastCommitPointer(commitFailed.getCommitId());

                confirmCommit(commitFailed.getCommit(), new CommitConfirm(ResponseState.FAILED));

                commits.clear();

                RxBus.getInstance().post(Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .state(States.FAILED)
                        .message(commitFailed.getErrorMessage())
                        .build());

                EventCallback eventCallback = new EventCallback.Builder()
                        .setCommand(EventCallback.getCommandForCommit(commitFailed.getCommit()))
                        .setReason(commitFailed.getErrorMessage())
                        .setStatus(EventCallback.STATUS_FAILED)
                        .setTimestamp(commitFailed.getCreatedTs())
                        .build();

                eventCallback.send();
            }

            @Override
            public void onCommitSkipped(CommitSkipped commitSkipped) {
                if (!commitSkipped.getCommitId().equals(pendingCommitId)) {
                    FPLog.w(SYNC_DATA, "Unexpected CommitSkipped " + commitSkipped + " received, expected commitId " + pendingCommitId);
                    return;
                }
                FPLog.i(SYNC_DATA, "Commit Skipped: " + commitSkipped);
                commitSkippedCounter.incrementAndGet();

                cancelCommitTimers();

                moveLastCommitPointer(commitSkipped.getCommitId());

                confirmCommit(commitSkipped.getCommit(), new CommitConfirm(ResponseState.SKIPPED));

                EventCallback eventCallback = new EventCallback.Builder()
                        .setCommand(EventCallback.getCommandForCommit(commitSkipped.getCommit()))
                        .setStatus(EventCallback.STATUS_OK)
                        .setTimestamp(commitSkipped.getCreatedTs())
                        .build();

                eventCallback.send();

                processNextCommit();
            }

            /**
             * Utilized {@link DevicePreferenceData} to move the individual devices last processed commitId forward
             *
             * @param lastCommitId
             */
            private void moveLastCommitPointer(String lastCommitId) {
                FPLog.d(TAG, "moving lastCommitId for deviceId " + syncRequest.getDevice().getDeviceIdentifier() + " to " + lastCommitId);

                DevicePreferenceData deviceData = DevicePreferenceData.load(
                        mContext, syncRequest.getDevice().getDeviceIdentifier());

                deviceData.setLastCommitId(lastCommitId);

                DevicePreferenceData.store(mContext, deviceData);
            }

            private void confirmCommit(final Commit commit, final CommitConfirm confirm) {
                if (commit.canConfirmCommit()) {
                    commit.confirm(confirm, new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            FPLog.i("commit " + commit + " successfully confirmed with " + confirm);
                        }

                        @Override
                        public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                            FPLog.e("error confirming commit " + commit + ", errorCode: " + errorCode + ", errorMessage: " + errorMessage);
                        }
                    });
                } else {
                    FPLog.i("skipping commit confirm for commit " + commit + ", no confirm link available");
                }
            }

            private void cancelCommitTimers() {
                if (commitWarningTimer != null) {
                    boolean result = commitWarningTimer.cancel(true);
                    commitWarningTimer = null;

                    FPLog.d(TAG, "canceled commitWarningTimer: " + result);
                }

                if (commitTimeoutTimer != null) {
                    boolean result = commitTimeoutTimer.cancel(true);
                    commitTimeoutTimer = null;

                    FPLog.d(TAG, "canceled commitTimeoutTimer: " + result);
                }
            }

            public int getCommitSuccessCount() {
                return commitSuccessCounter.get();
            }

            public int getCommitSkippedCount() {
                return commitSkippedCounter.get();
            }

            public int getCommitFailedCount() {
                return commitFailedCounter.get();
            }
        }
    }

    /**
     * This thread is responsible for listening for requests to sync, then launching a {@link SyncWorkerTask} to perform the
     * actual work of one individual sync.
     */
    private class SyncWorkerThread extends Thread {
        private final Context mContext;
        private final ScheduledExecutorService timeoutWatcherExecutor;

        private final BlockingQueue<SyncRequest> requests;
        private volatile boolean running = true;

        public SyncWorkerThread(Context mContext, BlockingQueue<SyncRequest> requests) {
            this.requests = requests;
            this.mContext = mContext;
            this.timeoutWatcherExecutor = Executors.newScheduledThreadPool(1);
        }

        public void run() {
            while (running) {
                try {
                    FPLog.d(TAG, "waiting for new sync request");
                    SyncRequest syncRequest = requests.take();

                    FPLog.d(TAG, "sync request received, launching sync task: " + syncRequest);

                    for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
                        callback.syncTaskStarting(syncRequest);
                    }

                    final long startTime = System.currentTimeMillis();
                    SyncWorkerTask task = new SyncWorkerTask(mContext, timeoutWatcherExecutor, syncRequest);
                    task.run();

                    FPLog.i("syncRequest: " + syncRequest + " completed in " + (System.currentTimeMillis() - startTime) + "ms");

                    for (DeviceSyncManagerCallback callback : syncManagerCallbacks) {
                        callback.syncTaskCompleted(syncRequest);
                    }
                } catch (InterruptedException e) {
                    FPLog.d("sync worker thread interrupted, shutting down");
                    running = false;
                }
            }
        }

        public void shutdown() {
            FPLog.i(TAG, "sync worker thread has been requested to shutdown");
            running = false;
            interrupt();
        }
    }
}
