package com.fitpay.android.api.sse;

import com.fitpay.android.api.models.UserStreamEvent;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.KeysManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.google.gson.Gson;
import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import okhttp3.Request;
import okhttp3.Response;

/**
 * This class is responsible for listening to a single user event stream, listening only for SYNC events
 * right now and initiating a sync with the SDK when received.
 *
 * Created by ssteveli on 3/15/18.
 */

public class UserEventStream {
    private final static String TAG = UserEventStream.class.getName();

    private final User user;

    private final ServerSentEvent sse;

    private long lastEventTs = -1;
    private boolean connected = false;

    public UserEventStream(User user) {
        FPLog.d(TAG, "connecting to user event stream for user: " + user.getId());

        this.user = user;

        String eventStreamUrl = user.getLinkUrl("eventStream");
        assert eventStreamUrl != null;

        Request request = new Request.Builder().url(eventStreamUrl).build();
        OkSse okSse = new OkSse();
        sse = okSse.newServerSentEvent(request, getListener());
    }

    public void close() {
        if (sse != null) {
            FPLog.d(TAG,"closing event stream for user " + user.getId());
            sse.close();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private ServerSentEvent.Listener getListener() {
        return new ServerSentEvent.Listener() {
            @Override
            public void onOpen(ServerSentEvent sse, Response response) {
                FPLog.d(TAG,"connected to event stream for user " + user.getId());
                connected = true;
            }

            @Override
            public void onMessage(ServerSentEvent sse, String id, String event, String message) {
                lastEventTs = System.currentTimeMillis();
                String payload = StringUtils.getDecryptedString(KeysManager.KEY_API, message);

                Gson gson = Constants.getGson();
                UserStreamEvent fitpayEvent = gson.fromJson(payload, UserStreamEvent.class);

                FPLog.d("event stream for user " + user.getId() + " received: " + fitpayEvent);
                RxBus.getInstance().post(fitpayEvent);
            }

            @Override
            public void onComment(ServerSentEvent sse, String comment) {
            }

            @Override
            public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
                return true;
            }

            @Override
            public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
                return false;
            }

            @Override
            public void onClosed(ServerSentEvent sse) {
                FPLog.d(TAG,"event stream for user " + user.getId() + " closed");
            }

            @Override
            public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
                return null;
            }
        };
    }
}
