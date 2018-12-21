package com.fitpay.android.api.sse;

import android.support.annotation.Nullable;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallbackExt;
import com.fitpay.android.api.callbacks.CallbackWrapper;
import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.api.models.Link;
import com.fitpay.android.api.models.UserStreamEvent;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.services.BaseClient;
import com.fitpay.android.api.services.FitPayClient;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.KeysManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.google.gson.Gson;
import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This class is responsible for listening to a single user event stream, listening only for SYNC events
 * right now and initiating a sync with the SDK when received.
 * <p>
 * Created by ssteveli on 3/15/18.
 */

public class UserEventStream {

    private final static String TAG = UserEventStream.class.getName();

    private long lastEventTs = -1;
    private boolean connected = false;

    private String userId;
    private ServerSentEvent sse;
    private Disposable disposable;

    private PublishSubject<UserStreamEvent> subject;

    public UserEventStream(String userId) {
        this.userId = userId;
    }

    public boolean isConnected() {
        return connected;
    }

    public long getLastEventTs() {
        return lastEventTs;
    }

    /**
     * Subscribe to events.
     * Don't forget to unsubscribe
     */
    public void subscribe() {
        if (subject == null) {
            subject = PublishSubject.create();
        }

        if (disposable == null || disposable.isDisposed()) {
            disposable = getUserStreamEvents().subscribe(
                    userStreamEvent -> {
                        if (subject != null) {
                            subject.onNext(userStreamEvent);
                        }
                        RxBus.getInstance().post(userStreamEvent);
                    },
                    throwable -> FPLog.e(TAG, throwable.getMessage()));
        }
    }

    /**
     * Unsubscribe from events
     */
    public void unsubscribe() {
        subject.onComplete();
        subject = null;

        disposable.dispose();
        disposable = null;
    }

    /**
     * Subscribe to events. It will be auto disposed on {@link #unsubscribe()}
     *
     * @param consumer event consumer
     * @return disposable
     */
    @Nullable
    public Disposable subscribeToEvents(Consumer<UserStreamEvent> consumer) {
        if (subject != null) {
            return subject.subscribe(consumer);
        }
        return null;
    }

    /**
     * Get new ServerSentEvent instance
     *
     * @return observable
     */
    private Observable<UserStreamEvent> getSse() {
        return Observable.create(emitter -> {
            FitPayClient client = ApiManager.getInstance().getClient();
            client.getUser(userId).enqueue(new CallbackWrapper<>(new ApiCallbackExt<User>() {
                @Override
                public void onFailure(ErrorResponse apiErrorResponse) {
                    emitter.onError(new Exception(apiErrorResponse.getDescription()));
                }

                @Override
                public void onSuccess(User user) {
                    Link eventStreamUrl = user.getEventStreamLink();

                    Request request = new Request.Builder().url(eventStreamUrl.getHref()).build();
                    OkSse okSse = new OkSse(BaseClient
                            .getOkHttpClient(false) // don't enable logging, that interceptor doesn't work with SSE streams
                            .readTimeout(0, TimeUnit.MILLISECONDS)
                            .retryOnConnectionFailure(true)
                            .build());

                    sse = okSse.newServerSentEvent(request, new ServerSentEvent.Listener() {
                        @Override
                        public void onOpen(ServerSentEvent sse, Response response) {
                            FPLog.d(TAG, "connected to event stream for user " + user.getId());
                            connected = true;
                        }

                        @Override
                        public void onMessage(ServerSentEvent sse, String id, String event, String message) {
                            lastEventTs = System.currentTimeMillis();

                            String payload = StringUtils.getDecryptedString(KeysManager.KEY_API, message);

                            Gson gson = Constants.getGson();
                            UserStreamEvent fitpayEvent = gson.fromJson(payload, UserStreamEvent.class);
                            emitter.onNext(fitpayEvent);
                        }

                        @Override
                        public void onComment(ServerSentEvent sse, String comment) {
                            FPLog.d(TAG, "sse onComment: " + comment);
                        }

                        @Override
                        public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
                            FPLog.d(TAG, "sse onRetryTime: " + milliseconds);
                            return false;
                        }

                        @Override
                        public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
                            FPLog.w(TAG, "sse onRetryError: " + throwable.getMessage());

                            emitter.onError(new ClosedChannelException());
                            return false;
                        }

                        @Override
                        public void onClosed(ServerSentEvent sse) {
                            connected = false;
                            FPLog.d(TAG, "event stream for user " + user.getId() + " closed");
                        }

                        @Override
                        public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
                            return null;
                        }
                    });
                }
            }));
        });
    }

    /**
     * Get UserStreamEvent observable.
     * Don't forget to unsubscribe;
     *
     * @return observable
     */
    private Observable<UserStreamEvent> getUserStreamEvents() {
        return getSse()
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                        return throwableObservable.flatMap(throwable -> {
                            if (throwable instanceof ClosedChannelException) {
                                return Observable.timer(1, TimeUnit.SECONDS);
                            } else {
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .doOnDispose(() -> {
                    if (sse != null) {
                        sse.close();
                    }
                });
    }
}
