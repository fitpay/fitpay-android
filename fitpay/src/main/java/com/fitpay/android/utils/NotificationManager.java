package com.fitpay.android.utils;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Notification manager. Support any {@link Listener} object
 */
public final class NotificationManager {

    private final static String TAG = NotificationManager.class.getSimpleName();

    private static NotificationManager sInstance;

    private List<Listener> mListeners;

    private Map<Class, Subscription> mSubscriptions;
    private Map<Class, List<Command>> mCommands;

    public static NotificationManager getInstance() {
        if (sInstance == null) {
            synchronized (NotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new NotificationManager();
                }
            }
        }

        return sInstance;
    }

    private NotificationManager() {
        mListeners = new CopyOnWriteArrayList<>();
        mCommands = new ConcurrentHashMap<>();
        mSubscriptions = new ConcurrentHashMap<>();
    }

    /**
     * Start listen to some events
     *
     * @param clazz     type of event
     * @param scheduler thread for result
     */
    private void subscribeTo(final Class clazz, final Scheduler scheduler) {
        FPLog.d(TAG, "subscribeTo class: " + clazz + " from thread: " + Thread.currentThread());

        if (!mSubscriptions.containsKey(clazz)) {
            synchronized (this) {
                FPLog.d(TAG, "subscribeTo doing put of class:  " + clazz + " from thread: " + Thread.currentThread());

                mSubscriptions.put(clazz, RxBus.getInstance().register(clazz, scheduler, object -> {
                    synchronized (this) {
                        for (Command command : mCommands.get(clazz)) {
                            if (object instanceof Wrapper) {
                                if (command instanceof FilterCommand) {
                                    String filter = ((FilterCommand) command).filter();
                                    if (filter != null && filter.equals(((Wrapper) object).getFilter())) {
                                        command.execute(((Wrapper) object).getObject());
                                    }
                                } else {
                                    command.execute(((Wrapper) object).getObject());
                                }
                            } else if (!(command instanceof FilterCommand)) {
                                command.execute(object);
                            }
                        }
                    }
                }));
            }
        }
    }

    /**
     * stop listen to events
     *
     * @param clazz
     */
    private void unsubscribeFrom(Class clazz) {
        FPLog.d(TAG, "unsubscribeFrom class: " + clazz + " called from thread: " + Thread.currentThread());
        if (mSubscriptions.containsKey(clazz)) {
            FPLog.d(TAG, "unsubscribeFrom removing class: " + clazz + " from thread: " + Thread.currentThread());
            mSubscriptions.get(clazz).unsubscribe();
            mSubscriptions.remove(clazz);
        }
    }

    /**
     * Add current listener. !!! Don't forget to remove it
     * The listener will execute on the Android UI thread
     *
     * @param listener listener
     */
    public void addListener(Listener listener) {
        addListener(listener, AndroidSchedulers.mainThread());
    }

    /**
     * Add current listener. !!! Don't forget to remove it
     *
     * @param listener listener
     */
    public void addListenerToCurrentThread(Listener listener) {
        addListener(listener, Schedulers.from(Constants.getExecutor()));
    }

    /**
     * Add current listener. !!! Don't forget to remove it
     *
     * @param listener listener
     */
    public void addListener(Listener listener, Scheduler observerScheduler) {
        synchronized (this) {
            FPLog.d(TAG, "addListener " + listener + " on scheduler: " + observerScheduler + ", current thread: " + Thread.currentThread());

            if (!mListeners.contains(listener)) {
                FPLog.d(TAG, "addListener: " + listener);
                mListeners.add(listener);

                Map<Class, Command> commands = listener.getCommands();

                for (Map.Entry<Class, Command> map : commands.entrySet()) {
                    Class clazz = map.getKey();

                    subscribeTo(clazz, observerScheduler);

                    if (!mCommands.containsKey(clazz)) {
                        mCommands.put(clazz, new CopyOnWriteArrayList<>());
                    }

                    mCommands.get(clazz).add(map.getValue());
                }
            } else {
                FPLog.w(TAG, "addListener skipped.  Listener already exists: " + listener);
            }
        }
    }

    /**
     * Remove current listener
     *
     * @param listener listener
     */
    public void removeListener(Listener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            FPLog.d(TAG, "removeListener " + listener + " called from thread: " + Thread.currentThread());

            if (mListeners.contains(listener)) {
                Map<Class, Command> commands = listener.getCommands();
                for (Map.Entry<Class, Command> map : commands.entrySet()) {
                    Class clazz = map.getKey();
                    FPLog.d(TAG, "removeListener removing value " + map.getValue() + " from thread: " + Thread.currentThread());

                    mCommands.get(clazz).remove(map.getValue());
                    if (mCommands.get(clazz).size() == 0) {
                        FPLog.d(TAG, "removeListener removing class: " + clazz + " from thread: " + Thread.currentThread());

                        mCommands.remove(clazz);
                        unsubscribeFrom(clazz);
                    }
                }

                mListeners.remove(listener);
            }
        }
    }
}
