package com.fitpay.android.api.sse;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.services.FitPayClient;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Response;


/**
 * This class manages the subscribing and unsubscribing from the user event stream of the FitPay platform.  The subscription
 * is encapsulated in a {@link UserEventStream}.
 */
public class UserEventStreamManager {
    private static ConcurrentHashMap<String, UserEventStream> streams = new ConcurrentHashMap<>();

    public static boolean isSubscribed(String userId) {
        UserEventStream stream = streams.get(userId);

        if (stream != null) {
            return stream.isConnected();
        } else {
            return false;
        }
    }

    /**
     * Subscribe to a user event stream, posting SYNC events to the included connector and device when
     * received from the FitPay platform.
     *
     * @param userId
     *
     * @return
     * @throws IOException
     */
    public static UserEventStream subscribe(final String userId) throws IOException {

        UserEventStream stream = streams.get(userId);

        if (stream == null) {
            FitPayClient client = ApiManager.getInstance().getClient();
            Response<User> user = client.getUser(userId).execute();

            if (user.isSuccessful()) {
                stream = new UserEventStream(user.body());
                UserEventStream existing = streams.putIfAbsent(userId, stream);

                if (existing != null) {
                    // whoops, another thread beat us to subscribing to this event stream... no need for this new one
                    stream.close();
                    stream = existing;
                }
            }

        }

        return stream;
    }

    /**
     * If subscribed, close the event stream subscription for the specified userId and device.
     *
     * @param userId
     */
    public static void unsubscribe(final String userId) {
        UserEventStream stream = streams.remove(userId);

        if (stream != null) {
            stream.close();
        }
    }
}
