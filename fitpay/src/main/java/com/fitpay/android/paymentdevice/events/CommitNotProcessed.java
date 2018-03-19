package com.fitpay.android.paymentdevice.events;

import com.fitpay.android.api.models.device.Commit;

/**
 * Created by tgs on 5/15/16.
 */
abstract class CommitNotProcessed extends CommitEvent {

    protected String errorMessage;
    protected int errorCode;

    CommitNotProcessed(Commit commit) {
        super(commit);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "CommitSkipped{" +
                "commitId='" + getCommitId() + '\'' +
                ", commitType='" + getCommitType() + '\'' +
                ", errorCode='" + getErrorCode() + '\'' +
                ", errorMessage='" + getErrorMessage() + '\'' +
                '}';
    }
}
