package com.fitpay.android.paymentdevice.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestUtils;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.LoginIdentity;
import com.fitpay.android.api.models.user.UserCreateRequest;
import com.fitpay.android.paymentdevice.DeviceSyncManager;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by ssteveli on 7/6/17.
 */


public class DeviceSyncManagerTest extends TestActions {

    private Context mContext;
    private DeviceSyncManager syncManager;
    protected IPaymentDeviceConnector mockPaymentDevice;

    private Device device;

    private SyncCompleteListener listener;
    private CountDownLatch executionLatch;
    private DeviceSyncManager.DeviceSyncManagerCallback syncManagerCallback;

    private String lastCommitId = null;

    @Before
    @Override
    public void testActionsSetup() throws Exception {
        SharedPreferences sp = Mockito.mock(SharedPreferences.class);
        Mockito.when(sp.getAll()).thenReturn(Collections.emptyMap());
        Mockito.when(sp.getString(Matchers.eq("lastCommitId"), (String)Matchers.isNull())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return lastCommitId;
            }
        });

        SharedPreferences.Editor spEditor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(sp.edit()).thenReturn(spEditor);
        Mockito.when(spEditor.putString(Matchers.eq("lastCommitId"), Matchers.anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                lastCommitId = (String)invocation.getArguments()[1];

                return spEditor;
            }
        });

        Mockito.when(spEditor.commit()).thenReturn(true);

        mContext = Mockito.mock(Context.class);
        Mockito.when(mContext.getSharedPreferences(Matchers.anyString(), Matchers.eq(Context.MODE_PRIVATE))).thenReturn(sp);

        syncManager = new DeviceSyncManager(mContext);
        syncManager.onCreate();

        syncManagerCallback = new DeviceSyncManager.DeviceSyncManagerCallback() {
            @Override
            public void syncRequestAdded(SyncRequest request) {
            }

            @Override
            public void syncTaskStarting(SyncRequest request) {
            }

            @Override
            public void syncTaskStarted(SyncRequest request) {
            }

            @Override
            public void syncTaskCompleted(SyncRequest request) {
                if (executionLatch != null) {
                    executionLatch.countDown();
                }
            }
        };
        syncManager.registerDeviceSyncManagerCallback(syncManagerCallback);

        mockPaymentDevice = new MockPaymentDeviceConnector();

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

        this.device = createDevice(this.user, getTestDevice());
        assertNotNull(this.device);

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);
        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", createdCard);

        Properties props = new Properties();
        props.put(MockPaymentDeviceConnector.CONFIG_CONNECTED_RESPONSE_TIME, "0");
        mockPaymentDevice.init(props);

        assertEquals("payment service is not initialized", States.INITIALIZED, mockPaymentDevice.getState());

        mockPaymentDevice.connect();

        int count = 0;
        while (mockPaymentDevice.getState() != States.CONNECTED || ++count < 5) {
            Thread.sleep(500);
        }

        assertEquals("payment service should be connected", States.CONNECTED, mockPaymentDevice.getState());

        this.executionLatch = new CountDownLatch(1);
        this.listener = new SyncCompleteListener();
        NotificationManager.getInstance().addListenerToCurrentThread(this.listener);
    }

    @After
    public void cleanup() {
        if (syncManager != null) {
            syncManager.onDestroy();
            syncManager.removeDeviceSyncManagerCallback(syncManagerCallback);
        }

        NotificationManager.getInstance().removeListener(this.listener);

        mContext = null;
    }

    @Test
    public void missingUserSyncRequestIsSkipped() throws Exception {
        syncManager.add(SyncRequest.builder()
                .setConnector(mockPaymentDevice)
                .setDevice(device)
                .build());

        executionLatch.await();

        assertEquals(1, listener.getSyncEvents().stream()
                .filter(syncEvent -> syncEvent.getState() == States.SKIPPED)
                .count());
        assertEquals(0, listener.getCommits().size());
    }

    @Test
    public void missingDeviceSyncRequestIsSkipped() throws Exception {
        syncManager.add(SyncRequest.builder()
                .setConnector(mockPaymentDevice)
                .setUser(user)
                .build());

        executionLatch.await();

        assertEquals(1, listener.getSyncEvents().stream()
                .filter(syncEvent -> syncEvent.getState() == States.SKIPPED)
                .count());
        assertEquals(0, listener.getCommits().size());
    }

    @Test
    public void missingConnectorSyncRequestIsSkipped() throws Exception {
        syncManager.add(SyncRequest.builder()
                .setUser(user)
                .setDevice(device)
                .build());

        executionLatch.await();

        assertEquals(1, listener.getSyncEvents().stream()
                .filter(syncEvent -> syncEvent.getState() == States.SKIPPED)
                .count());
        assertEquals(0, listener.getCommits().size());
    }

    @Test
    public void notConnectedDeviceSyncRequestIsSkipped() throws Exception {
        mockPaymentDevice.disconnect();

        while (mockPaymentDevice.getState() != States.DISCONNECTED) {
            Thread.sleep(500);
        }

        syncManager.add(SyncRequest.builder()
                .setConnector(mockPaymentDevice)
                .setUser(user)
                .setDevice(device)
                .build());

        executionLatch.await();

        assertEquals(1, listener.getSyncEvents().stream()
                .filter(syncEvent -> syncEvent.getState() == States.SKIPPED)
                .count());
        assertEquals(0, listener.getCommits().size());
    }

    @Test
    public void happyPathSyncTest() throws Exception {
        int syncCount = 10;

        for (int i=0; i<syncCount; i++) {
            System.out.println("");
            System.out.println("###############################################################################################################");
            System.out.println("################ sync #" + (i+1) + " of " + syncCount + " started");
            System.out.println("###############################################################################################################");
            System.out.println("");

            syncManager.add(SyncRequest.builder()
                    .setConnector(mockPaymentDevice)
                    .setUser(user)
                    .setDevice(device)
                    .build());

            executionLatch.await();
            executionLatch = new CountDownLatch(1);

            System.out.println("");
            System.out.println("###############################################################################################################");
            System.out.println("################ sync #" + (i+1) + " of " + syncCount + " completed");
            System.out.println("###############################################################################################################");
            System.out.println("");

            /*
                This test will emit three APDU packages for the newly boarded SE, therefore there should be 3 commits that show up... before
                we run the next sync(), let's wait for new commits to show up
             */
            if (listener.getCommits().size() < 3) {
                final CountDownLatch waitForCommitsLatch = new CountDownLatch(1);
                do {
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

        mockPaymentDevice.disconnect();

        assertEquals(syncCount,
                listener.getSyncEvents().stream()
                    .filter(syncEvent -> syncEvent.getState() == States.COMPLETED_NO_UPDATES || syncEvent.getState() == States.COMPLETED)
                    .count());

        assertEquals(3,
                listener.getCommits().stream()
                    .filter(commit -> commit.getCommitType().equals("APDU_PACKAGE"))
                    .count());
    }

    private class SyncCompleteListener extends Listener {
        private final List<Sync> syncEvents = new ArrayList<>();
        private final List<CommitSuccess> commits = new ArrayList<>();

        private SyncCompleteListener() {
            mCommands.put(Sync.class, data -> onSyncStateChanged((Sync) data));
            mCommands.put(CommitSuccess.class, data -> onCommitSuccess((CommitSuccess) data));
        }

        public void onSyncStateChanged(Sync syncEvent) {
            syncEvents.add(syncEvent);
        }

        public void onCommitSuccess(CommitSuccess commit) {
            commits.add(commit);
        }

        public List<Sync> getSyncEvents() {
            return syncEvents;
        }

        public List<CommitSuccess> getCommits() {
            return commits;
        }
    }
}
