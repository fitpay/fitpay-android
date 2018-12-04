package com.fitpay.android.webview.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated
 * Enum for relative web path
 */
@Deprecated
public class RelativeWebPath {

    public final static String PAGE_DEFAULT = "";
    public final static String PAGE_ADD_CARD = "/addCard";
    public final static String PAGE_PRIVACY_POLICY = "/privacyPolicy";
    public final static String PAGE_TERMS = "/terms";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PAGE_DEFAULT, PAGE_ADD_CARD, PAGE_PRIVACY_POLICY, PAGE_TERMS})
    @Deprecated
    public @interface Value {
    }
}
