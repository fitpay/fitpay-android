package com.fitpay.android.wearable.callbacks;

import com.fitpay.android.wearable.utils.ApduPair;

/**
 * Created by Vlad on 11.04.2016.
 */
interface IApduListener {
    void onApduPackageResultReceived(ApduPair pair);
}
