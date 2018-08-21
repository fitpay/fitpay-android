package com.fitpay.android.api.models.card;

import com.fitpay.android.a2averification.A2AContext;
import com.fitpay.android.api.enums.VerificationMethod;
import com.fitpay.android.api.enums.VerificationState;
import com.fitpay.android.api.models.BaseModel;

/**
 * Verification method model
 */
abstract class VerificationMethodModel extends BaseModel {

    protected String verificationId;

    @VerificationState.Type
    protected String state;

    @VerificationMethod.Type
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

    @VerificationState.Type
    public String getState() {
        return state;
    }

    @VerificationMethod.Type
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
