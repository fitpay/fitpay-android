package com.fitpay.android.webview.impl;

import android.support.annotation.Nullable;

import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.enums.RelativePath;

public enum WebRelativePath implements RelativePath {
    PAGE_DEFAULT(""),
    PAGE_ADD_CARD("/addCard"),
    PAGE_PRIVACY_POLICY("/privacyPolicy"),
    PAGE_TERMS("/terms");

    private String value;

    WebRelativePath(String value) {
        this.value = value;
    }

    @Override
    public String valueOf() {
        return value;
    }

    public static WebRelativePath getPath(@Nullable String value) {
        if (!StringUtils.isEmpty(value)) {
            for (WebRelativePath path : WebRelativePath.values()) {
                if (value.equals(path.name())) {
                    return path;
                }
            }
        }
        return PAGE_DEFAULT;
    }
}
