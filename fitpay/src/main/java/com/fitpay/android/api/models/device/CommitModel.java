package com.fitpay.android.api.models.device;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.enums.SourceTypes;
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

    @SourceTypes.Type
    protected String source;

    protected Long createdTs;

    @SerializedName("encryptedData")
    protected Payload payload;

    @CommitTypes.Type
    public String getCommitType() {
        return commitType;
    }

    @SourceTypes.Type
    public String getSource() { return source; }

    public long getCreatedTs() {
        return createdTs;
    }

    public String getCommitId() {
        return commitId;
    }

    public Object getPayload() {
        return payload.getData(commitType);
    }

    @Override
    public String toString() {
        return "Commit{" +
                "commitId='" + commitId + '\'' +
                ", commitType='" + commitType + '\'' +
                ", source='" + source + '\'' +
                ", createdTs=" + createdTs +
                '}';
    }
}
