package com.fitpay.android.webview.impl;

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
}
