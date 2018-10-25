package com.fitpay.android.api.models;

import android.support.annotation.NonNull;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.utils.StringUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Base model of API objects
 */
public class BaseModel {

    protected static final String SELF = "self";

    @Expose(serialize = false)
    @SerializedName("_links")
    protected Links links;

    protected BaseModel() {
    }

    public void self(@NonNull ApiCallback callback) {
        makeGetCall(SELF, null, getClass(), callback);
    }

    public String getLinkUrl(String key) {
        if (null == links) {
            return null;
        }

        return links.getLink(key);
    }

    public Link getLink(String key) {
        if (null == links) {
            return null;
        }

        return links.getFullLink(key);
    }

    private <T> String getLink(String key, ApiCallback<T> callback) {
        if (null == links) {
            callback.onFailure(ResultCode.NOT_FOUND, "The hypermedia link relationship [" + key + "] was not found, no links available for: " + this);
            return null;
        }

        String url = links.getLink(key);

        if (StringUtils.isEmpty(url)) {
            callback.onFailure(ResultCode.NOT_FOUND, "The hypermedia link relationship [" + key + "] was not found, available links are:" + links.getReadableKeys());
            url = null;
        }

        return url;
    }

    protected <T> void makeGetCall(String key, Map<String, Object> queryMap, Type type, ApiCallback<T> callback) {
        String url = getLink(key, callback);
        if (url != null) {
            ApiManager.getInstance().get(url, queryMap, type, callback);
        }
    }

    protected <T> void makeGetCall(String key, String additionalPath, Map<String, Object> queryMap, Type type, ApiCallback<T> callback) {
        String url = getLink(key, callback);
        if (url != null) {
            if (null != additionalPath && additionalPath.trim().length() > 0) {
                url = url.concat("/" + additionalPath);
            }
            ApiManager.getInstance().get(url, queryMap, type, callback);
        }
    }

    protected <T, U> void makePostCall(String key, U data, Type type, ApiCallback<T> callback) {
        String url = getLink(key, callback);
        if (url != null) {
            ApiManager.getInstance().post(url, data, type, callback);
        }
    }

    protected <U> void makeNoResponsePostCall(String key, U data, ApiCallback<Void> callback) {
        String url = getLink(key, callback);
        if (url != null) {
            ApiManager.getInstance().post(url, data, callback);
        }
    }

    protected <T, U> void makePatchCall(U data, boolean encrypt, Type type, ApiCallback<T> callback) {
        makePatchCall(data, false, encrypt, type, callback);
    }

    protected <T, U> void makePatchCall(U data, boolean add, boolean encrypt, Type type, ApiCallback<T> callback) {
        String url = getLink(SELF, callback);
        if (url != null) {
            ApiManager.getInstance().patch(url, data, add, encrypt, type, callback);
        }
    }

    protected void makeDeleteCall(ApiCallback<Void> callback) {
        String url = getLink(SELF, callback);
        if (url != null) {
            ApiManager.getInstance().delete(url, callback);
        }
    }

    public boolean hasLink(String key) {
        return links != null && !StringUtils.isEmpty(links.getLink(key));
    }
}
