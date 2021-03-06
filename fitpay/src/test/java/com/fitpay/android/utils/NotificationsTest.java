package com.fitpay.android.utils;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.paymentdevice.callbacks.ConnectionListener;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Connection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reactivestreams.Subscription;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

import static com.fitpay.android.api.enums.ResultCode.TIMEOUT;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationsTest extends BaseTestActions {

    private static Listener listener;
    private static NotificationManager manager;

    private static List<Listener> listeners;
    private static ConcurrentHashMap<Class, Subscription> subscriptions;
    private static ConcurrentHashMap<Class, List<Command>> commands;

    private static
    @Connection.State
    Integer testState;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void init() {
        BaseTestActions.init();
        listener = new ConnectionListener() {
            @Override
            public void onDeviceStateChanged(@Connection.State int state) {
                log("checkNotification receive:" + state);
                testState = state;
            }
        };

        manager = NotificationManager.getInstance();

        listeners = (List<Listener>) getPrivateField(manager, "mListeners");
        subscriptions = (ConcurrentHashMap<Class, Subscription>) getPrivateField(manager, "mSubscriptions");
        commands = (ConcurrentHashMap<Class, List<Command>>) getPrivateField(manager, "mCommands");
    }

    @Test
    public void test01_checkManager() throws InterruptedException {
        Assert.assertNotNull(listeners);
        Assert.assertNotNull(subscriptions);
        Assert.assertNotNull(commands);
    }

    @Test
    public void test02_addListener() throws InterruptedException {
        manager.addListener(listener, Schedulers.trampoline());

        Assert.assertEquals(1, listeners.size());
        Assert.assertEquals(1, subscriptions.size());
        Assert.assertEquals(1, commands.size());
    }

    @Test
    public void test03_checkNotification() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean changed = new AtomicBoolean(false);
        log("checkNotification start");

        Completable.create(emitter -> {
            log("checkNotification send state");
            RxBus.getInstance().post(new Connection(States.CONNECTED));
            emitter.onComplete();
        }).observeOn(Schedulers.trampoline()).subscribeOn(Schedulers.trampoline())
                .subscribe(
                        () -> {
                            log("checkNotification complete");
                            changed.set(testState != null && testState == States.CONNECTED);
                            latch.countDown();
                        },
                        e -> {
                            log("checkNotification error:" + e.getMessage());
                            latch.countDown();
                        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);
        log("checkNotification finish");
        Assert.assertTrue("state was not changed", changed.get());
    }

    @Test
    public void test04_removeListener() throws InterruptedException {
        manager.removeListener(listener);

        Assert.assertEquals(0, listeners.size());
        Assert.assertEquals(0, subscriptions.size());
        Assert.assertEquals(0, commands.size());
    }

    private static Object getPrivateField(Object from, String fieldName) {
        try {
            Field field = from.getClass().getDeclaredField(fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(from);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        manager.removeListener(listener);

        listener = null;
        manager = null;
        listeners = null;
        subscriptions = null;
        commands = null;
    }

    private static void log(String str) {
        System.out.println(str + " " + Thread.currentThread());
    }
}
