package com.fitpay.android.api.models.security;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ssteveli on 10/5/17.
 */
public class AccessDenied {
    public final static int INVALID_TOKEN_RESPONSE_CODE = 401;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            Reason.EXPIRED_TOKEN,
            Reason.UNAUTHORIZED
    })
    public @interface Reason {
        int EXPIRED_TOKEN = 1;
        int UNAUTHORIZED = 2;
    }

    @Reason
    private final int reason;

    private final boolean tokenRefreshRequired;

    private AccessDenied(int reason) {
        this.reason = reason;
        this.tokenRefreshRequired = false;
    }

    private AccessDenied(int reason, boolean tokenRefreshRequired) {
        this.reason = reason;
        this.tokenRefreshRequired = tokenRefreshRequired;
    }

    @Reason
    public int getReason() {
        return reason;
    }

    public boolean isTokenRefreshRequired() {
        return tokenRefreshRequired;
    }

    public static AccessDenied.Builder builder() {
        return new AccessDenied.Builder();
    }

    public static class Builder {
        @Reason
        private int reason;
        private boolean tokenRefreshRequired;

        public AccessDenied.Builder reason(@Reason int reason) {
            this.reason = reason;
            return this;
        }

        public AccessDenied.Builder tokenRefreshRequired(boolean tokenRefreshRequired) {
            this.tokenRefreshRequired = tokenRefreshRequired;
            return this;
        }

        public AccessDenied build() {
            return new AccessDenied(reason, tokenRefreshRequired);
        }
    }
}