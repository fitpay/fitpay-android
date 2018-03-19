package com.fitpay.android.paymentdevice.events;

import com.fitpay.android.api.models.device.Commit;

/**
 * Created by ssteveli on 6/3/16.
 */
public class CommitSkipped extends CommitNotProcessed {

    private CommitSkipped(Commit commit) {
        super(commit);
    }

    public static class Builder {
        private Commit commit;
        private String errorMessage;
        private int errorCode;

        public Builder commit(Commit commit) {
            this.commit = commit;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder errorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public CommitSkipped build() {
            CommitSkipped commitSkipped = new CommitSkipped(commit);
            commitSkipped.errorCode = this.errorCode;
            commitSkipped.errorMessage = this.errorMessage;
            return commitSkipped;
        }
    }
}
