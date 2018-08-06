package com.fitpay.android.paymentdevice.models;

import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.sync.MetricsData;
import com.fitpay.android.api.models.sync.SyncMetricsData;
import com.fitpay.android.utils.FPLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Support class for sync logic
 */
public final class SyncProcess {

    private static final String TAG = SyncProcess.class.getSimpleName();

    private final SyncRequest request;
    private List<Commit> commits;
    private Set<String> processedCommitIds;
    private List<MetricsData> commitsData;

    private long syncStartTime;

    private Commit pendingCommit;
    private MetricsData pendingCommitMD;

    public SyncProcess(final SyncRequest request) {
        this.request = request;
    }

    public void start() {
        syncStartTime = System.currentTimeMillis();
    }

    public void finish() {
        if (commits != null) {
            commits.clear();
        }

        if(processedCommitIds != null) {
            processedCommitIds.clear();
        }

        final SyncMetricsData smd = new SyncMetricsData.Builder()
                .readDataFromRequest(request)
                .setMetricsData(commitsData)
                .setTotalProcessingTime(System.currentTimeMillis() - syncStartTime)
                .build();

        smd.sendData(request);
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits == null ? Collections.emptyList() : commits;
        commitsData = new ArrayList<>(this.commits.size());
        processedCommitIds = new HashSet<>(this.commits.size());
    }

    public Commit startCommitProcessing() {
        pendingCommit = commits.remove(0);
        pendingCommitMD = new MetricsData(pendingCommit.getCommitId());
        return pendingCommit;
    }

    public void finishCommitProcessing() {
        finishCommitProcessing(null, null);
    }

    public void finishCommitProcessing(String error, String errorDescription) {
        pendingCommitMD.setEndTime();
        pendingCommitMD.setError(error);
        pendingCommitMD.setErrorDescription(errorDescription);
        commitsData.add(pendingCommitMD);
        pendingCommitMD = null;
        processedCommitIds.add(pendingCommit.getCommitId());
    }

    public String getPendingCommitId() {
        return pendingCommit != null ? pendingCommit.getCommitId() : "";
    }

    public int size() {
        return commits.size();
    }

    public boolean isCommitProcessed(String commitId){
        if(processedCommitIds.contains(commitId)){
            FPLog.w(TAG, "Commit " + commitId + " has already been processed");
            return true;
        }
        return false;
    }

}
