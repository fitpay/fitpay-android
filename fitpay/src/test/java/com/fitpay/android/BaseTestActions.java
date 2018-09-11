package com.fitpay.android;

import android.content.Context;

import com.fitpay.android.paymentdevice.DeviceSyncManager;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.NotificationManager;

import org.conscrypt.Conscrypt;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.security.Provider;
import java.util.concurrent.Executor;

import mockit.Mock;
import mockit.MockUp;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

public class BaseTestActions {

    public static Context mContext;

    @BeforeClass
    public static void init() {
        new MockUp<Conscrypt>() {
            @mockit.Mock
            Provider newProvider() {
                return null;
            }
        };

//        SecurityProvider.getInstance().setProvider(Conscrypt.newProvider());
        TestConstants.configureFitpay(mContext = Mockito.mock(Context.class));

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        RxJavaHooks.setOnIOScheduler(scheduler -> Schedulers.immediate());
        RxJavaHooks.setOnComputationScheduler(scheduler -> Schedulers.immediate());
        RxJavaHooks.setOnNewThreadScheduler(scheduler -> Schedulers.immediate());

        new MockUp<Schedulers>() {
            @Mock
            Scheduler from(Executor executor) {
                return Schedulers.immediate();
            }
        };
    }

    @AfterClass
    public static void clean() {
        mContext = null;
        DeviceSyncManager.clean();
        NotificationManager.clean();
        FPLog.clean();
    }

    @After
    public void after() {
        NotificationManager.clean();
    }
}