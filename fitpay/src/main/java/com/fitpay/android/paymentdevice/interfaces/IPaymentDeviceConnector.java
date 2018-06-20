package com.fitpay.android.paymentdevice.interfaces;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.card.TopOfWallet;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.CommitHandler;
import com.fitpay.android.paymentdevice.enums.Connection;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncRequest;

import java.util.List;
import java.util.Properties;

/**
 * abstract interface of wearable payment device
 */
public interface IPaymentDeviceConnector extends CommitHandler {

    /**
     * @return payment device connector UUID
     */
    String id();

    /**
     * Provide an Android context to the PaymentDeviceConnector so tha it can access
     * application and environment reseources as needed.
     *
     * @param context Android context.   In most case this will be the DeviceService context.
     */
    void setContext(@NonNull Context context);

    /**
     * Configuration properties for the PaymentDeviceConnector.
     * Property content is specific to the PaymentDeviceConnector implementation.
     *
     * @param props configuration properties
     */
    void init(@NonNull Properties props);

    void reset();

    void connect();

    void disconnect();

    void reconnect();

    void close();

    /**
     * Read payment device info
     */
    void readDeviceInfo();

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
    void onPreExecuteApdu();

    /**
     * do what you need after executiong apdu package
     */
    void onPostExecuteApdu();

    /**
     * process apdu package
     *
     * @param apduPackage apdu package commit
     */
    void executeApduPackage(@NonNull final ApduPackage apduPackage);

    /**
     * process single apdu command
     *
     * @param apduPkgNumber package number
     * @param apduCommand   apdu command
     */
    void executeApduCommand(long apduPkgNumber,@NonNull final ApduCommand apduCommand);

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

    /**
     * Add commit type handler
     *
     * @param commitType type of commit
     * @param handler    handler to process that commit
     */
    void addCommitHandler(String commitType, @NonNull CommitHandler handler);

    /**
     * Remove commit type handler
     *
     * @param commitType type of commit
     */
    void removeCommitHandler(String commitType);

    @Connection.State
    int getState();

    void setState(@Connection.State int state);

    /**
     * Add user
     *
     * @param user current user
     */
    void setUser(@NonNull User user);

    /**
     * Add device
     *
     * @param device current device
     */
    void setDevice(@NonNull Device device);


    /**
     * Post {@link SyncRequest} via {@link com.fitpay.android.utils.RxBus}
     */
    void createSyncRequest(@Nullable SyncInfo syncInfo);

    /**
     * Get User
     *
     * @return current user
     */
    User getUser();

    /**
     * Get Device
     *
     * @return current payment device
     */
    Device getDevice();
}
