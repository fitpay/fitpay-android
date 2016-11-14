package com.fitpay.android.utils;

import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

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

    private final Subject<Object, Object> mBus = new SerializedSubject<>(PublishSubject.create());

    public <T> Subscription register(final Class<T> eventClass, Action1<T> onNext) {
        return register(eventClass, AndroidSchedulers.mainThread(), onNext);
    }

    public <T> Subscription register(final Class<T> eventClass, final Scheduler scheduler, final Action1<T> onNext) {
        return mBus
                .asObservable()
                .onBackpressureBuffer()
                .filter(event -> eventClass.isAssignableFrom(event.getClass()))
                .map(obj -> (T) obj)
                .subscribeOn(Schedulers.from(Constants.getExecutor()))
                .observeOn(scheduler)
                .subscribe(onNext, throwable -> Logger.e(throwable.toString() + ", " + getStackTrace(throwable)));
    }

    public void post(Object object) {
        Log.d("RxBus", "post event: " + object);
        mBus.onNext(object);
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    }

    public static <T> Observable.Transformer<T, T> applySchedulersMainThread() {
        return observable -> observable.subscribeOn(Schedulers.from(Constants.getExecutor()))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable.Transformer<T, T> applySchedulersExecutorThread() {
        return observable -> observable.subscribeOn(Schedulers.from(Constants.getExecutor()))
                .observeOn(Schedulers.from(Constants.getExecutor()));
    }
}
