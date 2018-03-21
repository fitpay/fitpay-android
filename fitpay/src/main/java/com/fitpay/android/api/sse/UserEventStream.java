package com.fitpay.android.api.sse;

import com.fitpay.android.api.models.UserStreamEvent;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.services.BaseClient;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.KeysManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.google.gson.Gson;
import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import java.util.concurrent.TimeUnit;

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
        OkSse okSse = new OkSse(BaseClient
                .getOkHttpClient(false) // don't enable logging, that interceptor doesn't work with SSE streams
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build());
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
            private int counter = 0;

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

                FPLog.d(TAG,"sse onMessage " + user.getId() + " received: " + fitpayEvent);
                RxBus.getInstance().post(fitpayEvent);
            }

            @Override
            public void onComment(ServerSentEvent sse, String comment) {
                FPLog.d(TAG, "sse onComment: " + comment);
            }

            @Override
            public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
                FPLog.d(TAG, "sse onRetryTime: " + milliseconds);

                return true;
            }

            @Override
            public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
                FPLog.e(TAG, "sse onRetryTime: " + response);
                FPLog.e(TAG, throwable);

                if (++counter <= 5) {
                    FPLog.d(TAG, "still within retry parameters: " + counter + ", retrying sse connection");
                    return true;
                } else {
                    FPLog.d(TAG, "outside the retry parameters: " + counter + ", retrying sse connection");
                    return false;
                }
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

    public long getLastEventTs() {
        return lastEventTs;
    }
}
