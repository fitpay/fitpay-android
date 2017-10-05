package com.fitpay.android.api.models.security;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ssteveli on 10/5/17.
 */

public class AccessDenied {
    public final static int INVALID_TOKEN_RESPONSE_CODE = 400;

    public final static int EXPIRED_TOKEN = 1;
    public final static int UNAUTHORIZED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            AccessDenied.EXPIRED_TOKEN,
            AccessDenied.UNAUTHORIZED
    })
    public @interface Reason {
    }

    @Reason
    private final int reason;

    private AccessDenied(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }

    public static AccessDenied.Builder builder() {
        return new AccessDenied.Builder();
    }

    public static class Builder {
        @Reason
        private int reason;

        public AccessDenied.Builder reason(@Reason int reason) {
            this.reason = reason;
            return this;
        }

        public AccessDenied build() {
            return new AccessDenied(reason);
        }
    }
}