package com.fitpay.android.utils;


import java.io.PrintWriter;
import java.io.StringWriter;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vlad on 28.03.2016.
 */
public class RxBus {

    private static final RxBus sInstance;

    static {
        sInstance = new RxBus();
    }

    public static RxBus getInstance() {
        return sInstance;
    }

    private final PublishProcessor<Object> mBus = PublishProcessor.create();

    public <T> Disposable register(final Class<T> eventClass, Consumer<T> onNext) {
        return register(eventClass, AndroidSchedulers.mainThread(), onNext);
    }

    public <T> Disposable register(final Class<T> eventClass, final Scheduler scheduler, final Consumer<T> onNext) {
        return mBus
                .toObservable()
                .filter(event -> {
                    if (event == null || eventClass == null) {
                        FPLog.e(event + " " + eventClass);
                        return false;
                    }
                    if (event instanceof Wrapper) {
                        return eventClass.isAssignableFrom(((Wrapper) event).getClazz());
                    } else {
                        return eventClass.isAssignableFrom(event.getClass());
                    }
                })
                .map(obj -> (T) obj)
                .subscribeOn(Schedulers.from(Constants.getExecutor()))
                .observeOn(scheduler)
                .subscribe(onNext, throwable -> FPLog.e(throwable.toString() + ", " + getStackTrace(throwable)));
    }

    public void post(Object object) {
        FPLog.d("RxBus", "post event: " + object);
        mBus.onNext(object);
    }

    public <T> void post(String filter, T object) {
        if (filter != null) {
            post(new Wrapper<T>(filter, object));
        } else {
            post(object);
        }
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    }

    /*
    public static <T> T applySchedulersMainThread(Class<? extends T> type) {
        return applySchedulers(type, Schedulers.from(Constants.getExecutor()), AndroidSchedulers.mainThread());
    }

    public static <T> T applySchedulersExecutorThread(Class<? extends T> type){
        return applySchedulers(type, Schedulers.from(Constants.getExecutor()), Schedulers.from(Constants.getExecutor()));
    }

    private static <T> T applySchedulers(Class<? extends T> type, Scheduler subscribe, Scheduler observe){
        if (type.isAssignableFrom(Completable.class)) {
            return (T)(CompletableTransformer) upstream -> upstream
                    .subscribeOn(subscribe)
                    .observeOn(observe);
        } else if (type.isAssignableFrom(Maybe.class)) {
            return  (T)(MaybeTransformer<T, T>) upstream -> upstream
                    .subscribeOn(subscribe)
                    .observeOn(observe);
        } else if (type.isAssignableFrom(Single.class)) {
            return  (T)(SingleTransformer<T, T>) upstream -> upstream
                    .subscribeOn(subscribe)
                    .observeOn(observe);
        } else if (type.isAssignableFrom(Observable.class)) {
            return  (T)(ObservableTransformer<T, T>) upstream -> upstream
                    .subscribeOn(subscribe)
                    .observeOn(observe);
        } else {
            throw new IllegalArgumentException("Class not found");
        }

        return result;
    }
    */
}
