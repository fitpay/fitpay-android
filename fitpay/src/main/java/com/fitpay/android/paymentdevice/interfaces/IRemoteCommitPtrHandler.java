package com.fitpay.android.paymentdevice.interfaces;

/**
 * Use this interface for storing lastCommitId on OEM device
 */

public interface IRemoteCommitPtrHandler {
    String getLastCommitId(String deviceId);

    void setLastCommitId(String deviceId, String lastCommitId);
}
