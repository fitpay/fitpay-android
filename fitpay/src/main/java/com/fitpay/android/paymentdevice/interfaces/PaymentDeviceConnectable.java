package com.fitpay.android.paymentdevice.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.card.TopOfWallet;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.CommitHandler;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncRequest;

import java.util.List;

/**
 * abstract interface of wearable payment device
 */
public interface PaymentDeviceConnectable extends CommitHandler {

    int COMMIT_WARNING_TIMEOUT = 5000;
    int COMMIT_ERROR_TIMEOUT = 30000;

    /**
     * @return payment device connector UUID
     */
    String id();

    /**
     * Connect to device
     */
    void connect();

    /**
     * Disconnect from device
     */
    void disconnect();

    /**
     * Read payment device info
     */
    void readDeviceInfo();

    /**
     * Post {@link SyncRequest} via {@link com.fitpay.android.utils.RxBus}
     *
     * @param user current user
     * @param device current device
     * @param syncInfo sync info
     */
    void createSyncRequest(@NonNull User user, @NonNull Device device, @Nullable SyncInfo syncInfo);

    /**
     * Do any pre-sync preparation.
     * Typically this will be used to make sure the device is in the proper state
     * and to register event listeners used in the sync process
     */
    void syncInit();

    /**
     * Do any post-sync operations
     * Typically used for device finalization or to unregister sync specific listeners
     */
    void syncComplete();

    /**
     * do what you need before executing apdu package
     */
    void onPreExecuteApduPackage();

    /**
     * do what you need after executiong apdu package
     */
    void onPostExecuteApduPackage();

    /**
     * process single apdu command
     *
     * @param apduPkgNumber package number
     * @param apduCommand   apdu command
     */
    void executeApduCommand(long apduPkgNumber, @NonNull final ApduCommand apduCommand);

    /**
     * send apdu execution result to the server
     *
     * @param apduExecutionResult apdu execution result
     */
    void sendApduExecutionResult(@NonNull final ApduExecutionResult apduExecutionResult);

    /**
     * @param towPackages top of wallet package
     * @deprecated At this time we're looking to move away from the SDK specifically managing TOW execution:
     * <p>
     * 1. This typically occurs on the wearable device and not within the mobile SDK
     * 2. When not occuring on the wearable, the TOW are really nothing more than APDU_PACKAGEs and can be treated as such in an integration
     */
    void executeTopOfWallet(List<TopOfWallet> towPackages);

    /**
     * call after your commit has been processed
     *
     * @param type  commit execution result type
     * @param error error
     */
    void commitProcessed(@CommitTypes.Type int type, @Nullable final Throwable error);

    @Connection.State
    int getState();

    void setState(@Connection.State int state);

    /**
     * Check if commit timers are enabled.
     * Default value should be true
     *
     * @return is enabled or not
     */
    boolean isCommitTimersEnabled();

    /**
     * Get commit warning timeout
     * Default value should be {@link #COMMIT_WARNING_TIMEOUT}
     *
     * @return warningTimeout milliseconds.
     */
    int getCommitWarningTimeout();

    /**
     * Get commit error timeout
     * Default value should be {@link #COMMIT_ERROR_TIMEOUT}
     *
     * @return errorTimeout milliseconds.
     */
    int getCommitErrorTimeout();
}
