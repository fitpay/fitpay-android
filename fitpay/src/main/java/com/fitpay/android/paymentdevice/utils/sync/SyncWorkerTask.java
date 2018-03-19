package com.fitpay.android.paymentdevice.utils.sync;

import android.content.Context;

import com.fitpay.android.R;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.CommitConfirm;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.DeviceOperationException;
import com.fitpay.android.paymentdevice.callbacks.DeviceSyncManagerCallback;
import com.fitpay.android.paymentdevice.callbacks.IListeners;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSkipped;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.models.SyncProcess;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.paymentdevice.utils.DevicePreferenceData;
import com.fitpay.android.utils.EventCallback;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.events.DeviceStatusMessage;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;

import static com.fitpay.android.utils.Constants.SYNC_DATA;

/**
 * This is the real work horse of sync, it's launched by the {@link SyncThreadExecutor} for an individual {@link SyncRequest}
 * where it will establish a listener, kick off the sync, and orchastrate it's flow through a listener receiving events
 * back for each commit in the sync workflow.
 */
public final class SyncWorkerTask implements Runnable {
    private final static String TAG = SyncWorkerTask.class.getSimpleName();

    private final Context mContext;
    private final SyncRequest syncRequest;
    private final ScheduledExecutorService timeoutWatcherExecutor;

    private final String connectorId;

    private final CountDownLatch completionLatch = new CountDownLatch(1);

    private ScheduledFuture<Void> commitWarningTimer;
    private ScheduledFuture<Void> commitTimeoutTimer;

    private final int commitWarningTimeout = Integer.parseInt(ApiManager.getConfig().get(ApiManager.PROPERTY_COMMIT_WARNING_TIMEOUT));
    private final int commitErrorTimeout = Integer.parseInt(ApiManager.getConfig().get(ApiManager.PROPERTY_COMMIT_ERROR_TIMEOUT));

    private SyncProcess syncProcess;

    private final List<DeviceSyncManagerCallback> syncManagerCallbacks;

    public SyncWorkerTask(Context mContext, List<DeviceSyncManagerCallback> syncManagerCallbacks, SyncRequest syncRequest) {
        this.mContext = mContext;
        this.syncRequest = syncRequest;
        this.syncManagerCallbacks = syncManagerCallbacks;
        this.timeoutWatcherExecutor = Executors.newScheduledThreadPool(1);
        this.connectorId = syncRequest.getConnector().id();
    }

    public SyncRequest getSyncRequest() {
        return syncRequest;
    }

    @Override
    public void run() {
        SyncListener listener = new SyncListener(connectorId);
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

        RxBus.getInstance().post(connectorId, Sync.builder()
                .syncId(syncRequest.getSyncId())
                .state(States.STARTED)
                .build());

        if (syncRequest.getUser() == null) {
            FPLog.w(TAG, "No user provided in syncRequest: " + syncRequest);

            RxBus.getInstance().post(connectorId, Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.SKIPPED)
                    .message("No user provided in current syncRequest: " + syncRequest)
                    .build());

            return;
        }

        if (syncRequest.getDevice() == null) {
            FPLog.w(TAG, "No payment device connector configured in syncRequest: " + syncRequest);

            RxBus.getInstance().post(connectorId, Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.SKIPPED)
                    .message("No payment device provided in current syncRequest: " + syncRequest)
                    .build());

            return;
        }

        if (syncRequest.getConnector() == null) {
            FPLog.w(TAG, "No payment device connector configured in syncRequest: " + syncRequest);

            RxBus.getInstance().post(connectorId, Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.SKIPPED)
                    .message("No payment device provided in current syncRequest: " + syncRequest)
                    .build());
            return;
        }

        if (syncRequest.getConnector().getState() != States.CONNECTED) {
            FPLog.w(TAG, "Payment device is not in a CONNECTED state, syncRequest: " + syncRequest);

            RxBus.getInstance().post(connectorId, Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.SKIPPED)
                    .message("Payment device is not currently connected (" + syncRequest.getConnector().getState() + ": " + syncRequest)
                    .build());

            return;
        }

        FPLog.d(TAG, "sync initiated from thread: " + Thread.currentThread() + ", " + Thread.currentThread().getName() + ", syncRequest: " + syncRequest);

        syncDevice();
    }

    private void syncDevice() {
        Device device = syncRequest.getDevice();
        String deviceId = device.getDeviceIdentifier();

        RxBus.getInstance().post(connectorId, new DeviceStatusMessage(
                mContext.getString(R.string.fp_checking_wallet_updates),
                deviceId,
                DeviceStatusMessage.SUCCESS));


        // tell connector the sync is starting so it can perform an pre-init work
        syncRequest.getConnector().syncInit();

        // load the stored device data so we can figure out exactly where the last sync left off
        DevicePreferenceData deviceData = DevicePreferenceData.load(mContext, deviceId);

        // get all the new commits from the last commit pointer processed
        FPLog.d(TAG, "retrieving commits from the lastCommitId: " + deviceData.getLastCommitId() + ", for syncRequest: " + syncRequest);

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
                            syncProcess.setCommits(commitsCollection.getResults());

                            FPLog.i(SYNC_DATA, "Commits Received: " + syncProcess.size());

                            if (syncProcess.size() > 0) {
                                RxBus.getInstance().post(connectorId, new DeviceStatusMessage(
                                        mContext.getString(R.string.fp_updates_available),
                                        deviceId,
                                        DeviceStatusMessage.SUCCESS));

                                RxBus.getInstance().post(connectorId, new DeviceStatusMessage(
                                        mContext.getString(R.string.fp_sync_started),
                                        deviceId,
                                        DeviceStatusMessage.PROGRESS));

                                processNextCommit();
                            } else {
                                RxBus.getInstance().post(connectorId, new DeviceStatusMessage(
                                        mContext.getString(R.string.fp_no_pending_updates),
                                        deviceId,
                                        DeviceStatusMessage.SUCCESS));

                                RxBus.getInstance().post(connectorId, Sync.builder()
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

                            RxBus.getInstance().post(connectorId, Sync.builder()
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
                RxBus.getInstance().post(connectorId, Sync.builder()
                        .syncId(syncRequest.getSyncId())
                        .state(States.FAILED)
                        .message("Error processing next commit, device is disconnected or disconnecting")
                        .build());
                return;
        }

        if (syncProcess.size() > 0) {
            RxBus.getInstance().post(connectorId, Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .value(syncProcess.size())
                    .state(States.IN_PROGRESS)
                    .build());

            Commit commit = syncProcess.startCommitProcessing();

            FPLog.i(SYNC_DATA, "Process Next Commit: " + commit);

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
                        final String errorMessage = "error, commit timeout " + commit.getCommitId() + " has not returned within " + commitErrorTimeout + "ms";
                        FPLog.e(TAG, errorMessage);

                        CommitFailed.Builder builder = new CommitFailed.Builder().commit(commit);
                        builder.errorMessage(errorMessage);
                        RxBus.getInstance().post(builder.build());

                        return null;
                    }
                }, commitErrorTimeout, TimeUnit.MILLISECONDS);
            } else {
                FPLog.d(TAG, "skipped commit timers, they're turned off in ApiManager configuration");
            }

            // call the payment connector
            syncRequest.getConnector().processCommit(commit);

            // expose the commit out to others who may want to take action
            RxBus.getInstance().post(connectorId, commit);
        } else {
            RxBus.getInstance().post(connectorId, Sync.builder()
                    .syncId(syncRequest.getSyncId())
                    .state(States.COMPLETED)
                    .build());
        }
    }

    private class SyncListener extends Listener implements IListeners.SyncListener {
        private final AtomicInteger commitSuccessCounter = new AtomicInteger();
        private final AtomicInteger commitSkippedCounter = new AtomicInteger();
        private final AtomicInteger commitFailedCounter = new AtomicInteger();

        private SyncListener(String connectorId) {
            super(connectorId);

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
                    syncProcess = new SyncProcess(syncRequest);
                    syncProcess.start();
                    break;

                case States.COMPLETED:
                case States.COMPLETED_NO_UPDATES:
                case States.FAILED:
                case States.SKIPPED:
                case States.TIMEOUT:
                    if (syncRequest == null) {
                        FPLog.i(TAG, "no current sync request on sync event: " + syncEvent);
                    }

                    syncProcess.finish();

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
            if (!commitSuccess.getCommitId().equals(syncProcess.getPendingCommitId())) {
                FPLog.w(SYNC_DATA, "Unexpected CommitSuccess " + commitSuccess + " received, expected commitId " + syncProcess.getPendingCommitId());
                return;
            }

            syncProcess.finishCommitProcessing();

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
            eventCallback.send(connectorId);

            // move onto the next commit
            processNextCommit();
        }

        @Override
        public void onCommitFailed(CommitFailed commitFailed) {
            if (!commitFailed.getCommitId().equals(syncProcess.getPendingCommitId())) {
                FPLog.w(SYNC_DATA, "Unexpected CommitFailed " + commitFailed + " received, expected commitId " + syncProcess.getPendingCommitId());
                return;
            }

            syncProcess.finishCommitProcessing(String.valueOf(commitFailed.getErrorCode()), commitFailed.getErrorMessage());

            FPLog.w(SYNC_DATA, "Commit Failed: " + commitFailed);
            commitFailedCounter.incrementAndGet();

            cancelCommitTimers();

            moveLastCommitPointer(commitFailed.getCommitId());

            confirmCommit(commitFailed.getCommit(), new CommitConfirm(ResponseState.FAILED));

            EventCallback eventCallback = new EventCallback.Builder()
                    .setCommand(EventCallback.getCommandForCommit(commitFailed.getCommit()))
                    .setReason(commitFailed.getErrorMessage())
                    .setStatus(EventCallback.STATUS_FAILED)
                    .setTimestamp(commitFailed.getCreatedTs())
                    .build();

            eventCallback.send(connectorId);

            processNextCommit();
        }

        @Override
        public void onCommitSkipped(CommitSkipped commitSkipped) {
            if (!commitSkipped.getCommitId().equals(syncProcess.getPendingCommitId())) {
                FPLog.w(SYNC_DATA, "Unexpected CommitSkipped " + commitSkipped + " received, expected commitId " + syncProcess.getPendingCommitId());
                return;
            }

            syncProcess.finishCommitProcessing();

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
            eventCallback.send(connectorId);

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
