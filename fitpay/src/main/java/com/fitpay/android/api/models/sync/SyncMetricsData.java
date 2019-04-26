package com.fitpay.android.api.models.sync;

import android.os.Build;
import androidx.annotation.NonNull;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.FPLog;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Sync metrics data
 */
public class SyncMetricsData {
    private String syncId;
    private String userId;
    private String deviceId;
    private final String sdkVersion = BuildConfig.SDK_VERSION;
    private final String osVersion = String.valueOf(Build.VERSION.SDK_INT);
    @SyncInitiator.Initiator
    private String initiator;
    private long totalProcessingTimeMs;
    @SerializedName("commits")
    private List<MetricsData> metricsData;

    private SyncMetricsData() {
    }

    public void sendData(@NonNull final SyncRequest request) {
        if (request.getSyncInfo() != null) {
            request.getSyncInfo().sendSyncMetrics(this, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    FPLog.i("MetricsData has been sent successfully. syncId:" + request.getSyncId());
                }

                @Override
                public void onFailure(int errorCode, String errorMessage) {
                    FPLog.e("MetricsData failed to send. syncId:" + request.getSyncId());
                }
            });
        }
    }

    @Override
    public String toString() {
        return "SyncMetricsData{" +
                "syncId=" + syncId +
                ", userId=" + userId +
                ", deviceId=" + deviceId +
                ", sdkVersion=" + sdkVersion +
                ", osVersion=" + osVersion +
                ", initiator=" + initiator +
                ", totalProcessingTimeMs=" + totalProcessingTimeMs +
                ", commits=" + metricsData +
                '}';
    }

    public static class Builder {
        private String syncId;
        private String userId;
        private String deviceId;
        @SyncInitiator.Initiator
        private String initiator;
        private long totalProcessingTimeMS;
        @SerializedName("commits")
        private List<MetricsData> metricsData;

        /**
         * Set sync Id
         *
         * @param syncId sync id
         * @return this
         */
        public Builder setSyncId(String syncId) {
            this.syncId = syncId;
            return this;
        }

        /**
         * Set user id
         *
         * @param userId user id
         * @return this
         */
        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Set device id
         *
         * @param deviceId device id
         * @return this
         */
        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        /**
         * Set sync initiator
         *
         * @param initiator initiator
         * @return this
         */
        public Builder setInitiator(@SyncInitiator.Initiator String initiator) {
            this.initiator = initiator;
            return this;
        }

        /**
         * Set metrics data
         *
         * @param metricsData metrics data
         * @return this
         */
        public Builder setMetricsData(List<MetricsData> metricsData) {
            this.metricsData = metricsData;
            return this;
        }

        /**
         * Set total processing time
         *
         * @param totalProcessingTime total processing time in ms
         * @return this
         */
        public Builder setTotalProcessingTime(long totalProcessingTime) {
            this.totalProcessingTimeMS = totalProcessingTime;
            return this;
        }

        /**
         * Read data from request {@link SyncRequest}
         *
         * @param request sync request data
         * @return this
         */
        public Builder readDataFromRequest(@NonNull SyncRequest request) {
            this.syncId = request.getSyncId();
            this.userId = request.getUser() != null ? request.getUser().getId() : null;
            this.deviceId = request.getDevice() != null ? request.getDevice().getDeviceIdentifier() : null;
            this.initiator = request.getSyncInfo() != null ? request.getSyncInfo().getInitiator() : null;
            return this;
        }

        public SyncMetricsData build() {
            SyncMetricsData smd = new SyncMetricsData();
            smd.syncId = syncId;
            smd.userId = userId;
            smd.deviceId = deviceId;
            smd.initiator = initiator;
            smd.metricsData = metricsData;
            smd.totalProcessingTimeMs = totalProcessingTimeMS;
            return smd;
        }
    }
}
