package com.fitpay.android.rtm.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated as of v1.1.0 - not being used
 */
@Deprecated
public final class ErrorCodes {

    public static final int UDEC_UNKNOWN = 0;
    public static final int UDEC_USER_ID_DOESNT_MATCH = 1;
    public static final int UDEC_DATA_ID_DOESNT_MATCH = 2;
    public static final int UDEC_USER_DATA_DOESNT_MATCH = 3;
    public static final int UDEC_USER_DATA_CANT_BE_APPLIED = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            UDEC_UNKNOWN,
            UDEC_USER_ID_DOESNT_MATCH,
            UDEC_DATA_ID_DOESNT_MATCH,
            UDEC_USER_DATA_DOESNT_MATCH,
            UDEC_USER_DATA_CANT_BE_APPLIED
    })
    public @interface UserData {
    }

    public static final int SEC_UNKNOWN = 0;
    public static final int SEC_NO_USER_DATA = 1;
    public static final int SEC_UNAUTHORIZED = 2;
    public static final int SEC_COMMITS_COULDNT_BE_APPLIED = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SEC_UNKNOWN,
            SEC_NO_USER_DATA,
            SEC_UNAUTHORIZED,
            SEC_COMMITS_COULDNT_BE_APPLIED
    })
    public @interface Sync {
    }
}



