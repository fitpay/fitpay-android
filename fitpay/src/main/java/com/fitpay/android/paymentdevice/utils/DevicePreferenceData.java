package com.fitpay.android.paymentdevice.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.fitpay.android.paymentdevice.interfaces.IRemoteCommitPtrHandler;
import com.fitpay.android.utils.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.alexrs.prefs.lib.Prefs;

/**
 * Created by tgs on 5/14/16.
 */
public class DevicePreferenceData {

    private static final String[] KEY_VALUES = {"lastCommitId", "paymentDeviceServiceType", "paymentDeviceConfig"};
    private static IRemoteCommitPtrHandler remoteCommitPtrHandler;

    private String deviceIdentifier;
    private String lastCommitId;
    private String paymentDeviceServiceType;
    private String paymentDeviceConfig;
    private Map<String, String> additionalValues;

    private DevicePreferenceData() {
        additionalValues = new HashMap<>();
    }

    public static void setRemoteCommitPtrHandler(IRemoteCommitPtrHandler commitPointerHandler){
        remoteCommitPtrHandler = commitPointerHandler;
    }

    public static DevicePreferenceData load(Context context, String deviceIdentifier) {
        SharedPreferences prefs = getPreferences(context, deviceIdentifier);
        Map<String, String> values = new HashMap<>();
        for (String key : prefs.getAll().keySet()) {
            if (!Arrays.asList(KEY_VALUES).contains(key)) {
                values.put(key, prefs.getString(key, null));
            }
        }

        String lastCommitId = remoteCommitPtrHandler != null ?
                remoteCommitPtrHandler.getLastCommitId(deviceIdentifier) : prefs.getString("lastCommitId", null);

        DevicePreferenceData data = new Builder()
                .deviceIdentifier(deviceIdentifier)
                .lastCommitId(lastCommitId)
                .paymentDeviceServiceType(prefs.getString("paymentDeviceServiceType", null))
                .paymentDeviceConfig(prefs.getString("paymentDeviceConfig", null))
                .additionalValues(values)
                .build();
        return data;
    }

    /**
     * Remove current device prefs.
     * Call this method when watch app was deleted and you need to resync the data.
     *
     * @param context  Context
     * @param deviceId device ID
     */
    public static void clearCurrentData(Context context, String deviceId) {
        Prefs prefs = Prefs.with(context);
        String devId = prefs.getString(deviceId, null);
        if (!StringUtils.isEmpty(devId)) {
            getPreferences(context, devId).edit().clear().apply();
            prefs.save(deviceId, "");
        }
    }

    public static void store(Context context, DevicePreferenceData data) {
        if (null == data.deviceIdentifier) {
            return;
        }

        if(remoteCommitPtrHandler != null){
            remoteCommitPtrHandler.setLastCommitId(data.deviceIdentifier, data.lastCommitId);
        }

        SharedPreferences prefs = getPreferences(context, data.deviceIdentifier);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastCommitId", data.lastCommitId);
        editor.putString("paymentDeviceServiceType", data.paymentDeviceServiceType);
        editor.putString("paymentDeviceConfig", data.paymentDeviceConfig);
        if (null != data.additionalValues) {
            for (String key : data.additionalValues.keySet()) {
                editor.putString(key, data.additionalValues.get(key));
            }
        }
        boolean success = editor.commit();
    }

    protected static SharedPreferences getPreferences(Context context, String deviceIdentifier) {
        return context.getSharedPreferences("paymentDevice_" + deviceIdentifier, Context.MODE_PRIVATE);
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public String getPaymentDeviceServiceType() {
        return paymentDeviceServiceType;
    }

    public String getPaymentDeviceConfig() {
        return paymentDeviceConfig;
    }

    public void setPaymentDeviceServiceType(String paymentDeviceServiceType) {
        this.paymentDeviceServiceType = paymentDeviceServiceType;
    }

    public void putAdditionalValue(String key, String value) {
        additionalValues.put(key, value);
    }

    public String getAdditionalValue(String key) {
        return additionalValues.get(key);
    }


    public void removeAdditionalValue(String key) {
        additionalValues.remove(key);
    }

    public static class Builder {

        private String deviceIdentifier;
        private String lastCommitId;
        private String paymentDeviceServiceType;
        private String paymentDeviceConfig;
        private Map<String, String> additionalValues;

        public Builder() {
        }

        public Builder deviceIdentifier(String deviceIdentifier) {
            this.deviceIdentifier = deviceIdentifier;
            return this;
        }

        public Builder lastCommitId(String lastCommitIdValue) {
            this.lastCommitId = lastCommitIdValue;
            return this;
        }

        public Builder paymentDeviceServiceType(String paymentDeviceServiceTypeValue) {
            this.paymentDeviceServiceType = paymentDeviceServiceTypeValue;
            return this;
        }

        public Builder paymentDeviceConfig(String paymentDeviceConfig) {
            this.paymentDeviceConfig = paymentDeviceConfig;
            return this;
        }

        public Builder additionalValues(Map<String, String> additionalValues) {
            this.additionalValues = additionalValues;
            return this;
        }

        public DevicePreferenceData build() {
            DevicePreferenceData data = new DevicePreferenceData();
            data.deviceIdentifier = this.deviceIdentifier;
            data.lastCommitId = this.lastCommitId;
            data.paymentDeviceServiceType = this.paymentDeviceServiceType;
            data.paymentDeviceConfig = this.paymentDeviceConfig;
            data.additionalValues = this.additionalValues;
            return data;
        }
    }
}
