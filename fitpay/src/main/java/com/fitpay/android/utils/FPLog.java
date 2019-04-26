package com.fitpay.android.utils;

import androidx.annotation.IntDef;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vlad on 14.11.2016.
 */

public class FPLog {
    @Deprecated
    public static final int NONE = -1;
    public static final int VERBOSE = 4;
    public static final int DEBUG = 3;
    public static final int INFO = 2;
    public static final int WARNING = 1;
    public static final int ERROR = 0;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, VERBOSE, DEBUG, INFO, WARNING, ERROR})
    public @interface LogLevel {
    }

    private static boolean showHTTPLogs = false;
    private static final List<ILog> logs = new ArrayList<>();

    public static void addLogImpl(ILog iLog) {
        logs.add(iLog);
    }

    public static void clean() {
        logs.clear();
    }

    public static void v(String text) {
        v(Constants.FIT_PAY_TAG, text);
    }

    public static void v(String tag, String text) {
        for (ILog l : logs) {
            if (l.logLevel() >= VERBOSE) {
                l.v(tag, text);
            }
        }
    }

    public static void d(String text) {
        d(Constants.FIT_PAY_TAG, text);
    }

    public static void d(String tag, String text) {
        for (ILog l : logs) {
            if (l.logLevel() >= DEBUG) {
                l.d(tag, text);
            }
        }
    }

    public static void i(String text) {
        i(Constants.FIT_PAY_TAG, text);
    }

    public static void i(String tag, String text) {
        for (ILog l : logs) {
            if (l.logLevel() >= INFO) {
                l.i(tag, text);
            }
        }
    }

    public static void w(String text) {
        w(Constants.FIT_PAY_TAG, text);
    }

    public static void w(String tag, String text) {
        for (ILog l : logs) {
            if (l.logLevel() >= WARNING) {
                l.w(tag, text);
            }
        }
    }

    public static void e(String text) {
        e(Constants.FIT_PAY_TAG, new Throwable(text));
    }

    public static void e(Throwable throwable) {
        e(Constants.FIT_PAY_TAG, throwable);
    }

    public static void e(String tag, String text) {
        e(tag, new Throwable(text));
    }

    public static void e(String tag, Throwable throwable) {
        for (ILog l : logs) {
            if (l.logLevel() >= ERROR) {
                l.e(tag, throwable);
            }
        }
    }

    public static boolean showHttpLogs() {
        return showHTTPLogs;
    }

    public static void setShowHTTPLogs(boolean value) {
        showHTTPLogs = value;
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    }

    public interface ILog {
        void v(String tag, String text);

        void d(String tag, String text);

        void i(String tag, String text);

        void w(String tag, String text);

        void e(String tag, Throwable throwable);

        @LogLevel
        int logLevel();
    }
}
