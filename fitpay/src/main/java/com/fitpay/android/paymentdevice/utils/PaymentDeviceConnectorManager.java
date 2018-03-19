package com.fitpay.android.paymentdevice.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;

import com.fitpay.android.paymentdevice.impl.PaymentDeviceConnector;

import java.util.Iterator;
import java.util.Map;

/**
 * Singleton that manages 'n' {@link PaymentDeviceConnector}s.
 */
public class PaymentDeviceConnectorManager {

    private static String TAG = PaymentDeviceConnector.class.getName();

    private static PaymentDeviceConnectorManager sInstance = null;

    private Map<String, PaymentDeviceConnector> connectors;

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
    @Nullable
    public PaymentDeviceConnector getConnector(String id) {
        return connectors.get(id);
    }

    /**
     * Add new connector
     *
     * @param id        string value to determine your connector
     * @param connector payment device connector
     */
    public void addConnector(String id, PaymentDeviceConnector connector) {
        for (PaymentDeviceConnector value : connectors.values()) {
            if (value == connector) {
                Log.e(TAG, "connector has been added already");
                return;
            }
        }

        if (connectors.containsKey(id)) {
            Log.e(TAG, "id:" + id + " already exist");

            if (connectors.get(id) != connector) {
                Log.e(TAG, "id:" + id + " uses another connector");
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
    public void removeConnector(PaymentDeviceConnector connector) {
        for (Iterator<Map.Entry<String, PaymentDeviceConnector>> it = connectors.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, PaymentDeviceConnector> entry = it.next();
            if (entry.getValue() == connector) {
                it.remove();
                return;
            }
        }
    }
}
