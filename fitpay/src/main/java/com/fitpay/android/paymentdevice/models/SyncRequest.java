package com.fitpay.android.paymentdevice.models;

import android.support.annotation.NonNull;

import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.utils.StringUtils;

import java.util.UUID;

/**
 * Data for sync request
 */
public final class SyncRequest {
    private final String syncId;
    private final User user;
    private final Device device;
    private final boolean useLastAckCommit;
    private final IPaymentDeviceConnector connector;
    private final SyncInfo syncInfo;

    private SyncRequest(
            String syncId,
            User user,
            Device device,
            boolean useLastAckCommit,
            IPaymentDeviceConnector connector,
            SyncInfo syncInfo) {
        this.syncId = !StringUtils.isEmpty(syncId) ? syncId : UUID.randomUUID().toString();
        this.user = user;
        this.device = device;
        this.useLastAckCommit = useLastAckCommit;
        this.connector = connector;
        this.syncInfo = syncInfo;
    }

    public User getUser() {
        return user;
    }

    public Device getDevice() {
        return device;
    }

    public IPaymentDeviceConnector getConnector() {
        return connector;
    }

    public String getSyncId() {
        return syncId;
    }

    public boolean useLastAckCommit() {
        return useLastAckCommit;
    }

    public SyncInfo getSyncInfo() {
        return syncInfo;
    }

    @Override
    public String toString() {
        return "SyncRequest{" +
                "syncId='" + syncId + '\'' +
                ", user=" + user +
                ", device=" + device +
                ", useLastAckCommit=" + useLastAckCommit +
                ", connector=" + connector +
                ", links=" + syncInfo +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String syncId;
        private User user;
        private Device device;
        private boolean useLastAckCommit = true;
        private IPaymentDeviceConnector connector;
        private SyncInfo syncInfo;

        /**
         * Set sync id
         *
         * @param syncId sync id
         * @return this
         */
        public Builder setSyncId(String syncId) {
            this.syncId = syncId;
            return this;
        }

        /**
         * Set current user
         *
         * @param user current user
         * @return this
         */
        public Builder setUser(@NonNull User user) {
            this.user = user;
            return this;
        }

        /**
         * Set current device
         *
         * @param device current device
         * @return this
         */
        public Builder setDevice(@NonNull Device device) {
            this.device = device;
            return this;
        }

        /**
         * Use last ack commit
         *
         * @param useLastAckCommit true/false
         * @return this
         */
        public Builder setUseLastAckCommit(boolean useLastAckCommit) {
            this.useLastAckCommit = useLastAckCommit;
            return this;
        }

        /**
         * Set current connector {@link IPaymentDeviceConnector}
         *
         * @param connector payment device connector
         * @return this
         */
        public Builder setConnector(@NonNull IPaymentDeviceConnector connector) {
            this.connector = connector;
            return this;
        }

        /**
         * Set sync links
         *
         * @param syncInfo sync links
         * @return this
         */
        public Builder setSyncInfo(SyncInfo syncInfo) {
            this.syncInfo = syncInfo;

            if(syncInfo != null){
                this.syncId = syncInfo.getSyncId();
            }

            return this;
        }

        public SyncRequest build() {
            return new SyncRequest(syncId, user, device, useLastAckCommit, connector, syncInfo);
        }
    }
}
