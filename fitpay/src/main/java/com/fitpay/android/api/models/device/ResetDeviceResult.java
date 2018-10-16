package com.fitpay.android.api.models.device;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResetDeviceStatus;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.paymentdevice.DeviceOperationException;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;

/**
 * Response data for {@link Device#resetDevice}
 */
public class ResetDeviceResult extends BaseModel {
    private String resetId;
    @ResetDeviceStatus.Status
    private String status;
    @ResetDeviceStatus.Status
    private String seStatus;

    public String getResetId() {
        return resetId;
    }

    @ResetDeviceStatus.Status
    public String getResetStatus() {
        return status;
    }

    @ResetDeviceStatus.Status
    public String getSeStatus() {
        return seStatus;
    }

    /**
     * Get status updates
     *
     * @param callback result callback
     */
    public void getStatus(final ApiCallback<ResetDeviceResult> callback) {
        Map<String, Object> queryMap = new HashMap<>();
        makeGetCall(SELF, queryMap, ResetDeviceResult.class, callback);
    }

    /**
     * Get status updates
     *
     * @return observable
     */
    public Single<ResetDeviceResult> getStatus() {
        return Single.create(subscriber -> getStatus(new ApiCallback<ResetDeviceResult>() {
            @Override
            public void onSuccess(ResetDeviceResult result) {
                subscriber.onSuccess(result);
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                subscriber.onError(new DeviceOperationException(errorMessage, errorCode));
            }
        }));
    }
}
