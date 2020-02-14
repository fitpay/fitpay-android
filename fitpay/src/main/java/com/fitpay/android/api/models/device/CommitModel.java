package com.fitpay.android.api.models.device;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.api.models.Payload;
import com.google.gson.annotations.SerializedName;

/**
 * Commit model
 */
abstract class CommitModel extends BaseModel {

    protected String commitId;
    @CommitTypes.Type
    protected String commitType;
    protected Long createdTs;

    @SerializedName("encryptedData")
    protected Payload payload;

    @CommitTypes.Type
    public String getCommitType() {
        return commitType;
    }

    public long getCreatedTs() {
        return createdTs;
    }

    public String getCommitId() {
        return commitId;
    }

    public Object getPayload() {
        if (payload != null) {
            return payload.getData(commitType);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "commitId='" + commitId + '\'' +
                ", commitType='" + commitType + '\'' +
                ", createdTs=" + createdTs +
                '}';
    }
}
