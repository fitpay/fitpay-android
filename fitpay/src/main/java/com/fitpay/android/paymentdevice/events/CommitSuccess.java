package com.fitpay.android.paymentdevice.events;

import com.fitpay.android.api.models.device.Commit;

/**
 * Created by tgs on 5/15/16.
 */
public class CommitSuccess extends CommitEvent {

    private CommitSuccess(Commit commit) {
        super(commit);
    }

    public static class Builder {
        private Commit commit;

        public Builder commit(Commit commit) {
            this.commit = commit;
            return this;
        }

        public CommitSuccess build() {
            return new CommitSuccess(commit);
        }
    }
}
