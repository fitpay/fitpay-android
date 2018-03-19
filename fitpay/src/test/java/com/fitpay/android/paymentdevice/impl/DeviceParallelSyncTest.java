package com.fitpay.android.paymentdevice.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestUtils;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.LoginIdentity;
import com.fitpay.android.api.models.user.UserCreateRequest;
import com.fitpay.android.paymentdevice.DeviceSyncManager;
import com.fitpay.android.paymentdevice.callbacks.DeviceSyncManagerCallback;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by ssteveli on 7/6/17.
 */


public class DeviceParallelSyncTest extends TestActions {

    private Context mContext;
    private DeviceSyncManager syncManager;

    private IPaymentDeviceConnector firstMockPaymentDevice;
    private Device firstDevice;

    private IPaymentDeviceConnector secondMockPaymentDevice;
    private Device secondDevice;

    private SyncCompleteListener firstSyncListener;
    private SyncCompleteListener secondSyncListener;

    private AtomicReference<CountDownLatch> firstLatch;
    private AtomicReference<CountDownLatch> secondLatch;

    private AtomicReference<CountDownLatch> firstFinishLatch = new AtomicReference<>(new CountDownLatch(1));
    private AtomicReference<CountDownLatch> secondFinishLatch = new AtomicReference<>(new CountDownLatch(1));

    private DeviceSyncManagerCallback syncManagerCallback;

    private Map<String, String> commitId = new HashMap<String, String>();

    @Before
    @Override
    public void testActionsSetup() throws Exception {
        mContext = Mockito.mock(Context.class);

        /*-----user-----*/
        userName = TestUtils.getRandomLengthString(5, 10) + "@"
                + TestUtils.getRandomLengthString(5, 10) + "." + TestUtils.getRandomLengthString(4, 10);
        pin = TestUtils.getRandomLengthNumber(4, 4);

        UserCreateRequest userCreateRequest = getNewTestUser(userName, pin);
        createUser(userCreateRequest);

        assertTrue(doLogin(new LoginIdentity.Builder()
                .setPassword(pin)
                .setUsername(userName)
                .build()));
        this.user = getUser();
        /*-----user_end-----*/

        /*-----first_device-----*/
        firstDevice = createDevice(this.user, getTestDevice());
        assertNotNull(firstDevice);

        initPrefs(firstDevice.getDeviceIdentifier());

        firstMockPaymentDevice = new MockPaymentDeviceConnector();
        initPaymentDeviceConnector(firstMockPaymentDevice);

        firstSyncListener = new SyncCompleteListener(firstMockPaymentDevice.id());
        NotificationManager.getInstance().addListenerToCurrentThread(firstSyncListener);
        /*-----first_device_end-----*/

        /*-----second_device-----*/
        secondDevice = createDevice(this.user, getTestDevice());
        assertNotNull(secondDevice);

        initPrefs(secondDevice.getDeviceIdentifier());

        secondMockPaymentDevice = new MockPaymentDeviceConnector();
        initPaymentDeviceConnector(secondMockPaymentDevice);

        secondSyncListener = new SyncCompleteListener(secondMockPaymentDevice.id());
        NotificationManager.getInstance().addListenerToCurrentThread(secondSyncListener);
        /*-----second_device_end-----*/

        syncManager = new DeviceSyncManager(mContext);
        syncManager.onCreate();

        syncManagerCallback = new DeviceSyncManagerCallback() {
            @Override
            public void syncRequestAdded(SyncRequest request) {
            }

            @Override
            public void syncRequestFailed(SyncRequest request) {
            }

            @Override
            public void syncTaskStarting(SyncRequest request) {
            }

            @Override
            public void syncTaskStarted(SyncRequest request) {
            }

            @Override
            public void syncTaskCompleted(SyncRequest request) {
                if (request.getConnector().id().equals(firstMockPaymentDevice.id())) {
                    if (firstLatch != null) {
                        firstLatch.get().countDown();
                    }
                } else if (request.getConnector().id().equals(secondMockPaymentDevice.id())) {
                    if (secondLatch != null) {
                        secondLatch.get().countDown();
                    }
                }
            }
        };
        syncManager.registerDeviceSyncManagerCallback(syncManagerCallback);

        firstLatch = new AtomicReference<>(new CountDownLatch(1));
        secondLatch = new AtomicReference<>(new CountDownLatch(1));
    }

    @After
    public void cleanup() {
        if (syncManager != null) {
            syncManager.onDestroy();
            syncManager.removeDeviceSyncManagerCallback(syncManagerCallback);
        }

        NotificationManager.getInstance().removeListener(this.firstSyncListener);
        NotificationManager.getInstance().removeListener(this.secondSyncListener);

        mContext = null;
    }

    private void initPrefs(String deviceId) {
        final SharedPreferences mockPrefs = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor mockEditor = Mockito.mock(SharedPreferences.Editor.class);

        when(mContext.getSharedPreferences(ArgumentMatchers.eq("paymentDevice_" + deviceId), ArgumentMatchers.eq(Context.MODE_PRIVATE))).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockPrefs.getAll()).thenReturn(Collections.emptyMap());
        when(mockPrefs.getString(ArgumentMatchers.eq("lastCommitId"), ArgumentMatchers.isNull())).then(invocation -> commitId.get(deviceId));
        when(mockEditor.commit()).thenReturn(true);
        when(mockEditor.putString(ArgumentMatchers.eq("lastCommitId"), ArgumentMatchers.anyString())).thenAnswer(invocation -> {
            commitId.put(deviceId, (String) invocation.getArguments()[1]);
            return mockEditor;
        });
    }

    @Test
    public void syncTest() throws Exception {

        new Thread(() -> {
            try {
                runSync(firstMockPaymentDevice, firstDevice, firstSyncListener, firstLatch, firstFinishLatch);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                runSync(secondMockPaymentDevice, secondDevice, secondSyncListener, secondLatch, secondFinishLatch);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        firstFinishLatch.get().await();
        secondFinishLatch.get().await();

        firstMockPaymentDevice.disconnect();
        secondMockPaymentDevice.disconnect();

        assertEquals(3,
                firstSyncListener.getCommits().stream()
                        .filter(commit -> commit.getCommitType().equals("APDU_PACKAGE"))
                        .count());

        assertEquals(3,
                secondSyncListener.getCommits().stream()
                        .filter(commit -> commit.getCommitType().equals("APDU_PACKAGE"))
                        .count());
    }

    private void runSync(IPaymentDeviceConnector deviceConnector, Device device, SyncCompleteListener listener, AtomicReference<CountDownLatch> executionLatch, AtomicReference<CountDownLatch> finishLatch) throws InterruptedException {
        int syncCount = 10;

        for (int i = 0; i < syncCount; i++) {
            System.out.println("");
            System.out.println("###############################################################################################################");
            System.out.println("################ sync #" + (i + 1) + " of " + syncCount + " started for connector:" + deviceConnector.id());
            System.out.println("###############################################################################################################");
            System.out.println("");

            syncManager.add(SyncRequest.builder()
                    .setConnector(deviceConnector)
                    .setUser(user)
                    .setDevice(device)
                    .build());

            executionLatch.get().await();
            executionLatch.set(new CountDownLatch(1));

            System.out.println("");
            System.out.println("###############################################################################################################");
            System.out.println("################ sync #" + (i + 1) + " of " + syncCount + " completed for connector:" + deviceConnector.id());
            System.out.println("###############################################################################################################");
            System.out.println("");

            /*
                This test will emit three APDU packages for the newly boarded SE, therefore there should be 3 commits that show up... before
                we run the next sync(), let's wait for new commits to show up
             */
            if (listener.getCommits().size() < 3) {
                final CountDownLatch waitForCommitsLatch = new CountDownLatch(1);
                do {
                    String lastCommitId = commitId.get(device.getDeviceIdentifier());

                    System.out.println(">>> deviceId:" + device.getDeviceIdentifier() + " lastCommitId:" + lastCommitId);

                    device.getAllCommits(lastCommitId)
                            .subscribe(commits -> {
                                        System.out.println("commits found from " + lastCommitId + ": " + commits.getTotalResults());

                                        if (commits.getTotalResults() > 0) {
                                            waitForCommitsLatch.countDown();
                                        }
                                    },
                                    throwable -> {
                                        throwable.printStackTrace();
                                        fail(throwable.getMessage());
                                    });

                    Thread.sleep(500);
                } while (waitForCommitsLatch.getCount() > 0);
            }
        }

        finishLatch.get().countDown();
    }

    private void initPaymentDeviceConnector(IPaymentDeviceConnector connector) throws InterruptedException {
        Properties props = new Properties();
        props.put(MockPaymentDeviceConnector.CONFIG_CONNECTED_RESPONSE_TIME, "0");

        connector.init(props);

        assertEquals("payment service is not initialized", States.INITIALIZED, connector.getState());

        connector.connect();

        int count = 0;
        while (connector.getState() != States.CONNECTED || ++count < 5) {
            Thread.sleep(500);
        }
        assertEquals("payment service should be connected", States.CONNECTED, connector.getState());
    }

    private class SyncCompleteListener extends Listener {
        private final List<CommitSuccess> commits = new ArrayList<>();

        private SyncCompleteListener(String filter) {
            super(filter);
            mCommands.put(CommitSuccess.class, data -> onCommitSuccess((CommitSuccess) data));
        }

        public void onCommitSuccess(CommitSuccess commit) {
            commits.add(commit);
        }

        public List<CommitSuccess> getCommits() {
            return commits;
        }
    }

}
