package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.AsyncTask;

import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.ApduExecutionError;
import com.fitpay.android.paymentdevice.impl.ble.message.ApduResultMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.ApplicationControlMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationControlBeginMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationControlEndMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationControlMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationControlMessageFactory;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationPacketMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.NotificationMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.SecurityStateMessage;
import com.fitpay.android.paymentdevice.interfaces.IPaymentDeviceConnector;
import com.fitpay.android.paymentdevice.interfaces.ISecureMessage;
import com.fitpay.android.paymentdevice.utils.Crc32;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Hex;
import com.fitpay.android.utils.RxBus;

import java.io.IOException;
import java.util.UUID;

import rx.Observable;

/**
 * Manager that works with Bluetooth GATT Profile.
 **/
final class GattManager {

    private static final String TAG = GattManager.class.getSimpleName();

    private IPaymentDeviceConnector paymentDeviceConnector;

    private Context mContext;
    private BluetoothGatt mGatt;
    private BluetoothDevice mDevice;

    private OperationQueue mQueue;
    private GattOperation mCurrentOperation = null;

    private ContinuationPayload mContinuationPayload = null;
    private int mLastApduSequenceId;

    private AsyncTask<Void, Void, Void> mCurrentOperationTimeout;

    public GattManager(IPaymentDeviceConnector paymentDeviceConnector, Context context, BluetoothDevice device) {
        this.paymentDeviceConnector = paymentDeviceConnector;
        mContext = context;
        mDevice = device;
        mQueue = new OperationQueue();
    }

    public void reconnect() {
        queue(new GattSubscribeOperation());
    }

    public synchronized void disconnect() {
        if (mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }

        setCurrentOperation(null);

        mQueue.clear();

        paymentDeviceConnector.setState(States.DISCONNECTING);

        if (mGatt != null) {
            mGatt.disconnect();
        }
    }

    public synchronized void close() {
        mQueue.clear();

        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
    }

    public synchronized void cancelCurrentOperationBundle() {
        FPLog.w(TAG, "Cancelling current operation. Queue size before: " + mQueue.size());
        processError(ApduExecutionError.ON_TIMEOUT);
    }

    public synchronized void queue(GattOperation gattOperation) {
        mQueue.add(gattOperation);
        FPLog.i(TAG, "Queueing Gatt operation, size will now become: " + mQueue.size());
        drive();
    }

    private synchronized void drive() {
        if (mCurrentOperation != null) {
            FPLog.e(TAG, "tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }

        if (mQueue.size() == 0) {
            FPLog.i(TAG, "Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            if (mCurrentOperationTimeout != null) {
                mCurrentOperationTimeout.cancel(true);
            }
            return;
        }

        final GattOperation operation = mQueue.getFirst();
        setCurrentOperation(operation);

        resetTimer(operation.getTimeoutMs());

        if (operation instanceof GattApduBaseOperation) {
            mLastApduSequenceId = ((GattApduBaseOperation) operation).getSequenceId();
        }

        if (mGatt != null) {
            execute(mGatt, operation);
        } else {
            paymentDeviceConnector.setState(States.CONNECTING);

            mDevice.connectGatt(mContext, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);

                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            paymentDeviceConnector.setState(States.CONNECTED);

                            FPLog.i(TAG, "Gatt connected to device " + mDevice.getAddress());

                            mGatt = gatt;
                            mGatt.discoverServices();
                            break;

                        case BluetoothProfile.STATE_DISCONNECTED:
                            paymentDeviceConnector.setState(States.DISCONNECTED);

                            FPLog.i(TAG, "Disconnected from gatt server " + mDevice.getAddress() + ", newState: " + newState);

                            setCurrentOperation(null);

                            //Fix: Android Issue 97501:	BLE reconnect issue
                            if (mGatt != null) {
                                close();
                            } else {
                                mQueue.clear();
                                gatt.close();
                            }

                            break;

                        case BluetoothProfile.STATE_CONNECTING:
                            paymentDeviceConnector.setState(States.CONNECTING);
                            break;

                        case BluetoothProfile.STATE_DISCONNECTING:
                            paymentDeviceConnector.setState(States.DISCONNECTING);
                            break;
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);

                    FPLog.d(TAG, "services discovered, status: " + status);
                    execute(gatt, operation);
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);

                    if (mCurrentOperation instanceof DataReader) {
                        ((DataReader) mCurrentOperation).onRead(characteristic.getValue());
                    }

                    driveNext();
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);

                    FPLog.d(TAG, "Characteristic " + characteristic.getUuid() + "written to on device " + mDevice.getAddress());

                    driveNext();
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);

                    UUID uuid = characteristic.getUuid();
                    byte[] value = characteristic.getValue();

                    FPLog.d(TAG, "Characteristic changed: " + uuid);

                    if (PaymentServiceConstants.CHARACTERISTIC_SECURITY_STATE.equals(uuid)) {
                        ISecureMessage securityStateMessage = new SecurityStateMessage().withData(value);
                        RxBus.getInstance().post(paymentDeviceConnector.id(), securityStateMessage);
                    } else if (PaymentServiceConstants.CHARACTERISTIC_NOTIFICATION.equals(uuid)) {
                        NotificationMessage notificationMessage = new NotificationMessage().withData(value);
                        RxBus.getInstance().post(paymentDeviceConnector.id(), notificationMessage);
                    } else if (PaymentServiceConstants.CHARACTERISTIC_APDU_RESULT.equals(uuid)) {
                        ApduResultMessage apduResultMessage = new ApduResultMessage().withMessage(value);

                        if (mLastApduSequenceId == apduResultMessage.getSequenceId()) {

                            postMessage(apduResultMessage);

//                            RxBus.getInstance().post(apduResultMessage);
//                            driveNext();
                        } else {
                            FPLog.e(TAG, "Wrong sequenceID. lastSequenceID:" + mLastApduSequenceId + " currentID:" + apduResultMessage.getSequenceId());
                            processError(ApduExecutionError.WRONG_SEQUENCE);
                        }
                    } else if (PaymentServiceConstants.CHARACTERISTIC_CONTINUATION_CONTROL.equals(uuid)) {
                        FPLog.d(TAG, "continuation control write received [" + Hex.bytesToHexString(value) + "], length [" + value.length + "]");
                        ContinuationControlMessage continuationControlMessage = ContinuationControlMessageFactory.withMessage(value);
                        FPLog.d(TAG, "continuation control message: " + continuationControlMessage);

                        // start continuation packet
                        if (continuationControlMessage instanceof ContinuationControlBeginMessage) {
                            if (mContinuationPayload != null) {
                                FPLog.d(TAG, "continuation was previously started, resetting to blank");
                            }

                            mContinuationPayload = new ContinuationPayload(((ContinuationControlBeginMessage) continuationControlMessage).getUuid());

                            FPLog.d(TAG, "continuation start control received, ready to receive continuation data");
                        } else if (continuationControlMessage instanceof ContinuationControlEndMessage) {
                            FPLog.d(TAG, "continuation control end received.  process update to characteristic: " + mContinuationPayload.getTargetUuid());

                            UUID targetUuid = mContinuationPayload.getTargetUuid();
                            byte[] payloadValue = null;
                            try {
                                payloadValue = mContinuationPayload.getValue();
                                mContinuationPayload = null;
                                FPLog.d(TAG, "complete continuation data [" + Hex.bytesToHexString(payloadValue) + "]");
                            } catch (IOException e) {
                                FPLog.e(TAG, "error parsing continuation data" + e.getMessage());
                                processError(ApduExecutionError.CONTINUATION_ERROR);
                                return;
                            }

                            long checkSumValue = Crc32.getCRC32Checksum(payloadValue);
                            long expectedChecksumValue = ((ContinuationControlEndMessage) continuationControlMessage).getChecksum();

                            if (checkSumValue != expectedChecksumValue) {
                                FPLog.e(TAG, "Checksums not equal.  input data checksum: " + checkSumValue
                                        + ", expected value as provided on continuation end: " + expectedChecksumValue);

                                processError(ApduExecutionError.WRONG_CHECKSUM);
                                return;
                            }

                            if (PaymentServiceConstants.CHARACTERISTIC_APDU_RESULT.equals(targetUuid)) {
                                FPLog.d(TAG, "continuation is for APDU Result");

                                ApduResultMessage apduResultMessage = new ApduResultMessage().withMessage(payloadValue);
//                                RxBus.getInstance().post(apduResultMessage);
//
//                                driveNext();

                                postMessage(apduResultMessage);

                            } else {
                                FPLog.w(TAG, "Code does not handle continuation for characteristic: " + targetUuid);
                                processError(ApduExecutionError.CONTINUATION_ERROR);
                            }
                        }
                    } else if (PaymentServiceConstants.CHARACTERISTIC_CONTINUATION_PACKET.equals(uuid)) {

                        FPLog.d(TAG, "continuation data packet received [" + Hex.bytesToHexString(value) + "]");
                        ContinuationPacketMessage continuationPacketMessage = new ContinuationPacketMessage().withMessage(value);
                        FPLog.d(TAG, "parsed continuation packet message: " + continuationPacketMessage);

                        if (mContinuationPayload == null) {
                            FPLog.e(TAG, "invalid continuation, no start received on control characteristic");
                            processError(ApduExecutionError.CONTINUATION_ERROR);
                            return;
                        }

                        try {
                            mContinuationPayload.processPacket(continuationPacketMessage);
                        } catch (Exception e) {
                            FPLog.e(TAG, "exception handling continuation packet:" + e.getMessage());
                            processError(ApduExecutionError.CONTINUATION_ERROR);
                        }

                    } else if (PaymentServiceConstants.CHARACTERISTIC_APPLICATION_CONTROL.equals(uuid)) {
                        ApplicationControlMessage applicationControlMessage = new ApplicationControlMessage()
                                .withData(value);
                        RxBus.getInstance().post(paymentDeviceConnector.id(), applicationControlMessage);
                    }
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);

                    if (mCurrentOperation instanceof DataReader) {
                        ((GattDescriptorReadOperation) mCurrentOperation).onRead(descriptor.getValue());
                    }

                    driveNext();
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    driveNext();
                }
            });
        }
    }

    private void driveNext() {
        setCurrentOperation(null);
        drive();
    }

    private void execute(BluetoothGatt gatt, GattOperation operation) {
        if (operation != mCurrentOperation) {
            return;
        }

        operation.execute(gatt);

        if (operation.canRunNextOperation()) {
            driveNext();
        }
    }

    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }

    private void processError(@ApduExecutionError.Reason int reason) {

        GattOperation parent = null;

        if (mCurrentOperation != null) {
            parent = GattOperation.getRoot(mCurrentOperation);
            mQueue.remove(parent);
        }

        if (parent != null && parent instanceof GattApduOperation) {
            RxBus.getInstance().post(paymentDeviceConnector.id(), new ApduExecutionError(reason));
        } else {
            driveNext();
        }
    }

    private void resetTimer(final long timeout) {
        if (mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }
        mCurrentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    FPLog.i(TAG, "Starting to do a background timeout");
                    wait(timeout);
                } catch (InterruptedException e) {
                    FPLog.i(TAG, "was interrupted out of the timeout");
                }
                if (isCancelled()) {
                    FPLog.i(TAG, "The timeout was cancelled, so we do nothing.");
                    return null;
                }
                FPLog.i(TAG, "Timeout ran to completion, time to cancel the entire operation bundle. Abort, abort!");
                cancelCurrentOperationBundle();
                return null;
            }

            @Override
            protected synchronized void onCancelled() {
                super.onCancelled();
                notify();
            }
        }.execute();
    }

    private void postMessage(final ApduResultMessage message) {
        RxBus.getInstance().post(paymentDeviceConnector.id(), message);

        Observable.create(
                subscriber -> {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                })
                .compose(RxBus.applySchedulersMainThread())
                .subscribe(o -> {
                }, throwable -> {
                }, this::driveNext);
    }
}
