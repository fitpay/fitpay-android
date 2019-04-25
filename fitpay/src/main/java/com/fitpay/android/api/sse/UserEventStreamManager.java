package com.fitpay.android.api.sse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.utils.FPLog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class manages the subscribing and unsubscribing from the user event stream of the FitPay platform.  The subscription
 * is encapsulated in a {@link UserEventStream}.
 */
public class UserEventStreamManager {
    private final static String TAG = UserEventStreamManager.class.getName();

    private static ConcurrentHashMap<String, UserEventStream> streams = new ConcurrentHashMap<>();

    public static boolean isSubscribed(String userId) {
        UserEventStream stream = streams.get(userId);

        return stream != null && stream.isConnected();
    }

    /**
     * @deprecated
     * Use {@link #subscribeUser(String)}
     *
     * Subscribe to a user event stream, posting SYNC events to the included connector and device when
     * received from the FitPay platform.  The subscription occurs in the background, therefore this
     * method returns a Future for that subscription task.
     *
     * @param userId users Id
     * @return null is possible if not supported
     */
    @Deprecated
    public static Future<UserEventStream> subscribe(final String userId) {
        // if at the platform level SSE subscriptions are turned off, then this method will simply
        // return null
        if (!ApiManager.getInstance().getPlatformConfig().isUserEventStreamsEnabled()) {
            FPLog.i(TAG, "userEventStreamsEnabled has been disabled at the platform level, skipping user event stream subscription");
            return null;
        }

        final AtomicReference<UserEventStream> ref = new AtomicReference<>(streams.get(userId));

        // why this background execution, well android.. we don't want these network calls to
        // occur on the UI thread, therefore they're backgrounded.
        if (ref.get() == null) {
            UserEventStream stream = new UserEventStream(userId);
            stream.subscribe();
            ref.set(stream);
            streams.put(userId, stream);
        }

        return new Future<UserEventStream>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public UserEventStream get() {
                return ref.get();
            }

            @Override
            public UserEventStream get(long timeout, @NonNull TimeUnit unit) {
                return ref.get();
            }
        };
    }

    /**
     * @deprecated
     * Use {@link #unsubscribeUser(String)}
     *
     * If subscribed, close the event stream subscription for the specified userId and device.
     *
     * @param userId users Id
     */
    @Deprecated
    public static void unsubscribe(final String userId) {
        unsubscribeUser(userId);
    }

    /**
     * Subscribe to a user event stream, posting SYNC events to the included connector and device when
     * received from the FitPay platform. The subscription occurs in the background.
     * Don't forget to call {@link #unsubscribeUser(String)}
     *
     * @param userId users Id
     */
    public static void subscribeUser(final String userId) {
        if (!ApiManager.getInstance().getPlatformConfig().isUserEventStreamsEnabled()) {
            FPLog.i(TAG, "userEventStreamsEnabled has been disabled at the platform level, skipping user event stream subscription");
            return;
        }

        UserEventStream stream = streams.get(userId);
        if (stream == null) {
            stream = new UserEventStream(userId);
            stream.subscribe();
            streams.put(userId, stream);
        }
    }

    /**
     * If subscribed, close the event stream subscription for the specified userId and device.
     *
     * @param userId users Id
     */
    public static void unsubscribeUser(final String userId) {
        UserEventStream stream = streams.remove(userId);

        if (stream != null) {
            stream.unsubscribe();
        }
    }

    /**
     * Get user event stream
     *
     * @param userId userId
     * @return user event stream
     */
    @Nullable
    public static UserEventStream getUserEventStream(final String userId){
        return streams.get(userId);
    }
}
