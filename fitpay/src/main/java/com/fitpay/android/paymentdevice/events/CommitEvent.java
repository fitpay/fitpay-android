package com.fitpay.android.paymentdevice.events;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.models.device.Commit;

abstract class CommitEvent {

    private Commit commit;

    CommitEvent(Commit commit) {
        this.commit = commit;
    }

    public Commit getCommit() {
        return commit;
    }

    public String getCommitId() {
        if (commit != null) {
            return commit.getCommitId();
        }

        return null;
    }

    @CommitTypes.Type
    public String getCommitType() {
        if (commit != null) {
            return commit.getCommitType();
        }

        return null;
    }

    public long getCreatedTs() {
        if (commit != null) {
            return commit.getCreatedTs();
        }

        return -1;
    }


    @Override
    public String toString() {
        return "CommitSuccess{" +
                "commitId='" + getCommitId() + '\'' +
                ", createdTs='" + getCreatedTs() + '\'' +
                ", commitType='" + getCommitType() + '\'' +
                '}';
    }
}
