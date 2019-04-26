package com.fitpay.android.cardscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface for default card scanner
 */
public interface IFitPayCardScanner {
    /**
     * start scan
     *
     * @param callbackId     js callback id
     * @param resultCallback result callback
     */
    void startScan(String callbackId, ResultCallback resultCallback);

    /**
     * Callback for scanning result
     */
    interface ResultCallback {
        void onScanned(@NonNull String callbackId,@Nullable ScannedCardInfo cardInfo);
    }
}
