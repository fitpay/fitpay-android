package com.fitpay.android.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.annotations.Nullable;

/**
 * Generated server links. HATEOAS representation
 */
public final class Links implements Parcelable {

    private final Map<String, Link> links;

    public Links() {
        links = new HashMap<>();
    }

    public void setLink(String key, String value) {
        links.put(key, new Link(value, false));
    }

    public void setLink(String key, Link link) {
        links.put(key, link);
    }

    @Nullable
    public String getLink(String key) {
        if (links.containsKey(key)) {
            Link link = links.get(key);
            return link.getHref();
        }

        return null;
    }

    public String getReadableKeys() {
        if (links.keySet().size() > 0) {
            return links.keySet().toString();
        }

        return "none";
    }

    @Nullable
    Link getFullLink(String key) {
        if (links.containsKey(key)) {
            return links.get(key);
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final int N = links.size();
        dest.writeInt(N);
        if (N > 0) {
            for (Map.Entry<String, Link> entry : links.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeParcelable(entry.getValue(), flags);
            }
        }
    }

    private Links(Parcel in) {
        final int N = in.readInt();
        links = new HashMap<>();
        for (int i = 0; i < N; i++) {
            String key = in.readString();
            Link value = in.readParcelable(Links.class.getClassLoader());
            links.put(key, value);
        }
    }

    public static final Parcelable.Creator<Links> CREATOR = new Parcelable.Creator<Links>() {
        @Override
        public Links createFromParcel(Parcel source) {
            return new Links(source);
        }

        @Override
        public Links[] newArray(int size) {
            return new Links[size];
        }
    };
}
