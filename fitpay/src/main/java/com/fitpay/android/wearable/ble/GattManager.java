package com.fitpay.android.wearable.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.AsyncTask;

import com.fitpay.android.utils.RxBus;
import com.fitpay.android.wearable.ble.callbacks.CharacteristicChangeListener;
import com.fitpay.android.wearable.ble.constants.PaymentServiceConstants;
import com.fitpay.android.wearable.ble.interfaces.CharacteristicReader;
import com.fitpay.android.wearable.ble.message.ApduResultMessage;
import com.fitpay.android.wearable.ble.message.NotificationMessage;
import com.fitpay.android.wearable.ble.message.SecurityStateMessage;
import com.fitpay.android.wearable.ble.operations.GattDescriptorReadOperation;
import com.fitpay.android.wearable.ble.operations.GattOperation;
import com.fitpay.android.wearable.ble.operations.GattOperationBundle;
import com.fitpay.android.wearable.ble.utils.OperationConcurrentQueue;
import com.fitpay.android.wearable.interfaces.IApduMessage;
import com.fitpay.android.wearable.interfaces.ISecureMessage;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.UUID;

public class GattManager {

    private Context mContext;
    private BluetoothGatt mGatt;
    private BluetoothDevice mDevice;

    private OperationConcurrentQueue mQueue;
    private GattOperation mCurrentOperation;

    private int lastApduSequenceId;

    private AsyncTask<Void, Void, Void> mCurrentOperationTimeout;

    public GattManager(Context context, BluetoothDevice device) {
        mContext = context;
        mDevice = device;
        mQueue = new OperationConcurrentQueue();
        mCurrentOperation = null;
    }

    public synchronized void disconnect(){
        mQueue.clear();

        if(mGatt != null){
            mGatt.disconnect();
        }
    }

    public void close(){
        if(mGatt != null){
            mGatt.close();
            mGatt = null;
        }
    }

    public synchronized void cancelCurrentOperationBundle() {
        Logger.v("Cancelling current operation. Queue size before: " + mQueue.size());
        if(mCurrentOperation != null) {
            mQueue.remove(mCurrentOperation);
        }
        Logger.v("Queue size after: " + mQueue.size());
        mCurrentOperation = null;
        drive();
    }

    public synchronized void queue(GattOperation gattOperation) {
        mQueue.add(gattOperation);
        Logger.v("Queueing Gatt operation, size will now become: " + mQueue.size());
        drive();
    }

    private synchronized void drive() {
        if(mCurrentOperation != null) {
            Logger.e("tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }

        if( mQueue.size() == 0) {
            Logger.v("Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            if(mCurrentOperationTimeout != null) {
                mCurrentOperationTimeout.cancel(true);
            }
            return;
        }

        final GattOperation operation = mQueue.getFirst();
        Logger.v("Driving Gatt queue, size will now become: " + mQueue.size());
        setCurrentOperation(operation);

        resetTimer(operation.getTimeoutMs());

        if(mGatt != null) {

            if(operation instanceof IApduMessage){
                lastApduSequenceId = ((IApduMessage) operation).getSequenceId();
            }

            execute(mGatt, operation);
        } else {
            mDevice.connectGatt(mContext, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Logger.i("Gatt connected to device " + mDevice.getAddress());
                        mGatt = gatt;
                        mGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Logger.i("Disconnected from gatt server " + mDevice.getAddress() + ", newState: " + newState);
                        mCurrentOperation = null;
                        drive();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);

                    Logger.d("services discovered, status: " + status);
                    execute(gatt, operation);
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);

                    if(mCurrentOperation instanceof CharacteristicReader){
                        ((CharacteristicReader)mCurrentOperation).onRead(characteristic);
                    }

                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);

                    Logger.d("Characteristic " + characteristic.getUuid() + "written to on device " + mDevice.getAddress());

                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);

                    UUID uuid = characteristic.getUuid();

                    Logger.d("Characteristic changed: " + uuid);

                    if(PaymentServiceConstants.CHARACTERISTIC_SECURITY_STATE.equals(uuid)){
                        ISecureMessage securityStateMessage = new SecurityStateMessage().withData(characteristic.getValue());
                        RxBus.getInstance().post(securityStateMessage);
                    } else if(PaymentServiceConstants.CHARACTERISTIC_NOTIFICATION.equals(uuid)){
                        NotificationMessage notificationMessage = new NotificationMessage().withData(characteristic.getValue());
                        RxBus.getInstance().post(notificationMessage);
                    } else if(PaymentServiceConstants.CHARACTERISTIC_APDU_RESULT.equals(uuid)){
                        ApduResultMessage apduResultMessage = new ApduResultMessage().withMessage(characteristic.getValue());
                        if(lastApduSequenceId == apduResultMessage.getSequenceId()) {
                            RxBus.getInstance().post(apduResultMessage);
                        } else {
                            //TODO: send error
                        }
                    } else if(PaymentServiceConstants.CHARACTERISTIC_CONTINUATION_CONTROL.equals(uuid)){

                    } else if(PaymentServiceConstants.CHARACTERISTIC_CONTINUATION_PACKET.equals(uuid)){

                    } else if(PaymentServiceConstants.CHARACTERISTIC_APPLICATION_CONTROL.equals(uuid)){

                    }
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);

                    ((GattDescriptorReadOperation) mCurrentOperation).onRead(descriptor);
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);

                    setCurrentOperation(null);
                    drive();
                }
            });
        }
    }

    private void execute(BluetoothGatt gatt, GattOperation operation) {
        if(operation != mCurrentOperation) {
            return;
        }

        operation.execute(gatt);
    }

    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }

    public void queue(GattOperationBundle bundle) {
        for(GattOperation operation : bundle.getOperations()) {
            queue(operation);
        }
    }

    private void resetTimer(final long timeout){
        if(mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }
        mCurrentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    Logger.v("Starting to do a background timeout");
                    wait(timeout);
                } catch (InterruptedException e) {
                    Logger.v("was interrupted out of the timeout");
                }
                if(isCancelled()) {
                    Logger.v("The timeout was cancelled, so we do nothing.");
                    return null;
                }
                Logger.v("Timeout ran to completion, time to cancel the entire operation bundle. Abort, abort!");
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
}
