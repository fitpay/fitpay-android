package com.fitpay.android.utils;

import android.support.annotation.StringDef;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.models.device.Commit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Vlad on 23.11.2016.
 */

public class EventCallback {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_FAILED = "FAILED";

    public static final String CREDITCARD_CREATED = "CREDITCARD_CREATED";
    public static final String CREDITCARD_ACTIVATED = "CREDITCARD_ACTIVATED";
    public static final String CREDITCARD_DEACTIVATED = "CREDITCARD_DEACTIVATED";
    public static final String CREDITCARD_REACTIVATED = "CREDITCARD_REACTIVATED";
    public static final String CREDITCARD_PENDING_VERIFICATION = "CREDITCARD_PENDING_VERIFICATION";
    public static final String CREDITCARD_DELETED = "CREDITCARD_DELETED";
    public static final String CREDITCARD_METADATA_UPDATED = "CREDITCARD_METADATA_UPDATED";
    public static final String CREDITCARD_PROVISION_FAILED = "CREDITCARD_PROVISION_FAILED";
    public static final String SET_DEFAULT_CREDITCARD = "SET_DEFAULT_CREDITCARD";
    public static final String RESET_DEFAULT_CREDITCARD = "RESET_DEFAULT_CREDITCARD";
    public static final String TOKEN_INITIAL_ACTIVATION = "TOKEN_INITIAL_ACTIVATION";
    public static final String TOKEN_DEACTIVATED = "TOKEN_DEACTIVATED";
    public static final String TOKEN_REACTIVATED = "TOKEN_REACTIVATED";
    public static final String USER_CREATED = "USER_CREATED";
    public static final String GET_USER_AND_DEVICE = "GET_USER_AND_DEVICE";
    public static final String APDU_COMMANDS_SENT = "APDU_COMMANDS_SENT";
    public static final String SYNC_COMPLETED = "SYNC_COMPLETED";
    public static final String DATA_PARSED = "DATA_PARSED";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            CREDITCARD_CREATED,
            CREDITCARD_ACTIVATED,
            CREDITCARD_DEACTIVATED,
            CREDITCARD_REACTIVATED,
            CREDITCARD_PENDING_VERIFICATION, //?
            CREDITCARD_DELETED,
            CREDITCARD_METADATA_UPDATED,
            CREDITCARD_PROVISION_FAILED,
            SET_DEFAULT_CREDITCARD,
            RESET_DEFAULT_CREDITCARD,
            TOKEN_INITIAL_ACTIVATION, //?
            TOKEN_DEACTIVATED, //?
            TOKEN_REACTIVATED, //?
            USER_CREATED,
            GET_USER_AND_DEVICE,
            APDU_COMMANDS_SENT,
            SYNC_COMPLETED,
            DATA_PARSED
    })
    public @interface Command {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            STATUS_OK,
            STATUS_FAILED
    })
    public @interface Status {
    }

    private static final Format format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS", Locale.getDefault());

    @Command
    private String command;
    @Status
    private String status;
    private String reason;
    private String timestamp;

    private EventCallback() {
        super();
    }

    /**
     * Send data without filter
     */
    public void send() {
        RxBus.getInstance().post(this);
    }

    /**
     * Send data using connectorId as a filter for RxBux
     *
     * @param connectorId connectorId
     */
    public void send(String connectorId) {
        RxBus.getInstance().post(connectorId, this);
    }

    @Command
    public String getCommand() {
        return command;
    }

    @Status
    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "EventCallback{" +
                "command='" + command + '\'' +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    public static class Builder {
        @Command
        private String command;
        @Status
        private String status;
        private String reason;
        private String timestamp;

        public Builder setCommand(@Command String command) {
            this.command = command;
            return this;
        }

        public Builder setStatus(@Status String status) {
            this.status = status;
            return this;
        }

        public Builder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            Date date = new Date(timestamp);
            this.timestamp = format.format(date);
            return this;
        }

        public EventCallback build() {
            EventCallback ec = new EventCallback();

            if (timestamp == null) {
                Date date = new Date(System.currentTimeMillis());
                timestamp = format.format(date);
            }

            ec.command = command;
            ec.status = status;
            ec.reason = reason;
            ec.timestamp = timestamp;

            return ec;
        }
    }

    @EventCallback.Command
    public static String getCommandForCommit(Commit commit) {
        @EventCallback.Command String command = null;
        switch (commit.getCommitType()) {
            case CommitTypes.CREDITCARD_CREATED:
                command = CREDITCARD_CREATED;
                break;
            case CommitTypes.CREDITCARD_DELETED:
                command = CREDITCARD_DELETED;
                break;
            case CommitTypes.CREDITCARD_ACTIVATED:
                command = CREDITCARD_ACTIVATED;
                break;
            case CommitTypes.CREDITCARD_DEACTIVATED:
                command = CREDITCARD_DEACTIVATED;
                break;
            case CommitTypes.CREDITCARD_REACTIVATED:
                command = CREDITCARD_REACTIVATED;
                break;
            case CommitTypes.RESET_DEFAULT_CREDITCARD:
                command = RESET_DEFAULT_CREDITCARD;
                break;
            case CommitTypes.SET_DEFAULT_CREDITCARD:
                command = SET_DEFAULT_CREDITCARD;
                break;
            case CommitTypes.CREDITCARD_METADATA_UPDATED:
                command = CREDITCARD_METADATA_UPDATED;
            case CommitTypes.CREDITCARD_PROVISION_FAILED:
                command = CREDITCARD_PROVISION_FAILED;
                break;
        }
        return command;
    }
}
