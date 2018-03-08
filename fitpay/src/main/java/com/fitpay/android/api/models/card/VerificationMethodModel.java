package com.fitpay.android.api.models.card;

import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.webview.models.a2a.A2AContext;

/**
 * Verification method model
 */
abstract class VerificationMethodModel extends BaseModel {

    protected String verificationId;
    protected String state;
    protected String methodType;
    protected String value;
    protected String verificationResult;
    protected long createdTsEpoch;
    protected long lastModifiedTsEpoch;
    protected long verifiedTsEpoch;
    protected A2AContext appToAppContext;

    public String getVerificationId() {
        return verificationId;
    }

    public String getState() {
        return state;
    }

    public String getMethodType() {
        return methodType;
    }

    public String getValue() {
        return value;
    }

    public String getVerificationResult() {
        return verificationResult;
    }

    public long getCreatedTsEpoch() {
        return createdTsEpoch;
    }

    public long getLastModifiedTsEpoch() {
        return lastModifiedTsEpoch;
    }

    public long getVerifiedTsEpoch() {
        return verifiedTsEpoch;
    }

    public A2AContext getAppToAppContext() {
        return appToAppContext;
    }
}
