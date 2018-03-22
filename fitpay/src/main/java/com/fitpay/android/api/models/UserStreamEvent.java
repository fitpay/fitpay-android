package com.fitpay.android.api.models;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.Map;

/**
 * This model class encapsulates an event received over a {@link com.fitpay.android.api.sse.UserEventStream}, the payload is specifically determined dynamically
 * by the type.  It's the consumers responsibility to cast the payload to the type expected.
 *
 * Brief Example:
 *
 * if (type.equals("SYNC") {
 *     SyncRequest request = Constants.getGson().fromJson(payload, SyncRequest.class);
 * }
 *
 * Created by ssteveli on 3/20/18.
 */

public class UserStreamEvent {
    private String type;
    private Date timestamp;
    private Long timestampEpoch;
    private String message;
    private Map<String, String> metadata;
    private JsonObject payload;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestampEpoch() {
        return timestampEpoch;
    }

    public void setTimestampEpoch(Long timestampEpoch) {
        this.timestampEpoch = timestampEpoch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserStreamEvent that = (UserStreamEvent) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
            return false;
        if (timestampEpoch != null ? !timestampEpoch.equals(that.timestampEpoch) : that.timestampEpoch != null)
            return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null)
            return false;
        return payload != null ? payload.equals(that.payload) : that.payload == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (timestampEpoch != null ? timestampEpoch.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("UserStreamEvent{");
        sb.append("type='").append(type).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", timestampEpoch=").append(timestampEpoch);
        sb.append(", message='").append(message).append('\'');
        sb.append(", metadata=").append(metadata);
        sb.append(", payload=").append(payload);
        sb.append('}');
        return sb.toString();
    }
}
