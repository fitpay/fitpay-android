package com.fitpay.android.paymentdevice.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import com.fitpay.android.paymentdevice.impl.PaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.PaymentDeviceConnectable;
import com.fitpay.android.utils.FPLog;

import java.util.Iterator;
import java.util.Map;

/**
 * Singleton that manages 'n' {@link PaymentDeviceConnector}s.
 */
public class PaymentDeviceConnectorManager {

    private static String TAG = PaymentDeviceConnector.class.getName();

    private static PaymentDeviceConnectorManager sInstance = null;

    private Map<String, PaymentDeviceConnectable> connectors;

    private PaymentDeviceConnectorManager() {
        connectors = new ArrayMap<>(5);
    }

    @NonNull
    public static PaymentDeviceConnectorManager getInstance() {
        if (null == sInstance) {
            sInstance = new PaymentDeviceConnectorManager();
        }

        return sInstance;
    }

    /**
     * Get the connector
     *
     * @param id string value to determine your connector
     * @return connector, null if not found
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends PaymentDeviceConnectable> T getConnector(String id) {
        return (T)connectors.get(id);
    }

    /**
     * Add new connector
     *
     * @param id        string value to determine your connector
     * @param connector payment device connector
     */
    public void addConnector(String id, PaymentDeviceConnectable connector) {
        for (PaymentDeviceConnectable value : connectors.values()) {
            if (value == connector) {
                FPLog.e(TAG, "connector has been added already");
                return;
            }
        }

        if (connectors.containsKey(id)) {
            FPLog.e(TAG, "id:" + id + " already exist");

            if (connectors.get(id) != connector) {
                FPLog.e(TAG, "id:" + id + " uses another connector");
            }

            return;
        }

        connectors.put(id, connector);
    }

    /**
     * Remove connector by id from the list
     *
     * @param id
     */
    public void removeConnector(String id) {
        connectors.remove(id);
    }

    /**
     * Remove connector by value from the list
     *
     * @param connector
     */
    public void removeConnector(PaymentDeviceConnectable connector) {
        for (Iterator<Map.Entry<String, PaymentDeviceConnectable>> it = connectors.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, PaymentDeviceConnectable> entry = it.next();
            if (entry.getValue() == connector) {
                it.remove();
                return;
            }
        }
    }
}
