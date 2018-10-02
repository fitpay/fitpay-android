package com.fitpay.android.api.models;

import android.net.Uri;
import android.os.Parcel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Image asset reference
 */
public final class ImageAssetWithOptionsReference extends ImageAssetReference {

    public String getUrl(ImageAssetOptions options) {
        Uri uri = Uri.parse(getUrl());

        final Set<String> oldParams = new HashSet<>(uri.getQueryParameterNames());
        final Uri.Builder newUri = uri.buildUpon().clearQuery();

        Map<String, String> paramsMap = options.getParamToValueMap();

        for (ImageAssetOptions.ImageAssetParams p : ImageAssetOptions.ImageAssetParams.values()) {
            oldParams.remove(p.value);
            if (null != paramsMap.get(p.value)) {
                newUri.appendQueryParameter(p.value, paramsMap.get(p.value));
            }
        }

        for (String param : oldParams) {
            newUri.appendQueryParameter(param, uri.getQueryParameter(param));
        }

        return newUri.build().toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public ImageAssetWithOptionsReference() {
    }

    protected ImageAssetWithOptionsReference(Parcel in) {
        super(in);
    }

    public static final Creator<ImageAssetWithOptionsReference> CREATOR = new Creator<ImageAssetWithOptionsReference>() {
        @Override
        public ImageAssetWithOptionsReference createFromParcel(Parcel source) {
            return new ImageAssetWithOptionsReference(source);
        }

        @Override
        public ImageAssetWithOptionsReference[] newArray(int size) {
            return new ImageAssetWithOptionsReference[size];
        }
    };
}
