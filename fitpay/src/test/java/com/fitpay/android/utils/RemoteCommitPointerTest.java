package com.fitpay.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.paymentdevice.interfaces.IRemoteCommitPtrHandler;
import com.fitpay.android.paymentdevice.utils.DevicePreferenceData;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Vlad on 10/5/17.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RemoteCommitPointerTest  extends BaseTestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(RemoteCommitPointerTest.class);

    @Test
    public void storeCommitPointerRemote() throws Exception {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences mockPrefs = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor mockEditor = Mockito.mock(SharedPreferences.Editor.class);

        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);

        final String deviceIdentifier = UUID.randomUUID().toString();
        final String commitId = UUID.randomUUID().toString();

        DevicePreferenceData.setRemoteCommitPtrHandler(new PtrHandler());

        DevicePreferenceData data = new DevicePreferenceData.Builder()
                .deviceIdentifier(deviceIdentifier)
                .lastCommitId(commitId)
                .build();

        final String newCommitId = UUID.randomUUID().toString();
        data.setLastCommitId(newCommitId);

        DevicePreferenceData.store(context, data);

        DevicePreferenceData loadedData = DevicePreferenceData.load(context, deviceIdentifier);
        Assert.assertEquals(deviceIdentifier, loadedData.getDeviceIdentifier());
        Assert.assertEquals(newCommitId, loadedData.getLastCommitId());
    }

    private class PtrHandler implements IRemoteCommitPtrHandler {

        private String commitId;

        @Override
        public String getLastCommitId(String deviceId) {
            return commitId;
        }

        @Override
        public void setLastCommitId(String deviceId, String lastCommitId) {
            commitId = lastCommitId;
        }
    }
}
