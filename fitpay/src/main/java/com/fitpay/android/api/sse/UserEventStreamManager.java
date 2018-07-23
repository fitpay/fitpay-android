package com.fitpay.android.api.sse;

import android.support.annotation.NonNull;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.services.FitPayClient;
import com.fitpay.android.utils.FPLog;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * This class manages the subscribing and unsubscribing from the user event stream of the FitPay platform.  The subscription
 * is encapsulated in a {@link UserEventStream}.
 */
public class UserEventStreamManager {
    private final static String TAG = UserEventStreamManager.class.getName();

    private static ConcurrentHashMap<String, UserEventStream> streams = new ConcurrentHashMap<>();
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static boolean isSubscribed(String userId) {
        UserEventStream stream = streams.get(userId);

        return stream != null && stream.isConnected();
    }

    /**
     * Subscribe to a user event stream, posting SYNC events to the included connector and device when
     * received from the FitPay platform.  The subscription occurs in the background, therefore this
     * method returns a Future for that subscription task.
     *
     * @param userId users Id
     * @return null is possible if not supported
     */
    public static Future<UserEventStream> subscribe(final String userId) {
        // if at the platform level SSE subscriptions are turned off, then this method will simply
        // return null
        if (!ApiManager.getInstance().getPlatformConfig().isUserEventStreamsEnabled()) {
            FPLog.i(TAG, "userEventStreamsEnabled has been disabled at the platform level, skipping user event stream subscription");
            return null;
        }

        UserEventStream stream = streams.get(userId);

        // why this background execution, well android.. we don't want these network calls to
        // occur on the UI thread, therefore they're backgrounded.
        if (stream == null) {
            return executor.submit(() -> {

                UserEventStream[] result = new UserEventStream[1];

                Observable.defer(() -> {
                    try {
                        FitPayClient client = ApiManager.getInstance().getClient();
                        Response<User> user = client.getUser(userId).execute();

                        if (user.isSuccessful()) {
                            UserEventStream eventStream = new UserEventStream(user.body());
                            UserEventStream existing = streams.putIfAbsent(userId, eventStream);

                            if (existing != null) {
                                // whoops, another thread beat us to subscribing to this event stream... no need for this new one
                                eventStream.close();
                                eventStream = existing;
                            }

                            result[0] = eventStream;
                        }
                    } catch (IOException e) {
                        FPLog.e(e);
                    }

                    return Observable.empty();
                }).subscribeOn(Schedulers.io()).toBlocking().subscribe();

                return result[0];
            });

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
                return stream;
            }

            @Override
            public UserEventStream get(long timeout, @NonNull TimeUnit unit) {
                return stream;
            }
        };
    }

    /**
     * If subscribed, close the event stream subscription for the specified userId and device.
     *
     * @param userId users Id
     */
    public static void unsubscribe(final String userId) {
        UserEventStream stream = streams.remove(userId);

        if (stream != null) {
            executor.execute(stream::close);
        }
    }
}
