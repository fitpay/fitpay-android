package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothGatt;

import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.api.models.apdu.ApduCommandResult;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.ApduExecutionError;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.interfaces.IApduMessage;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Hex;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.TimestampUtils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Apdu operation. Can process commands with any size.
 */
class GattApduOperation extends GattOperation {

    private ApduExecutionResult mResult;

    private ApduNotificationListener mNotificationListener;
    private Map<Integer, String> mSequencesMap;

    private long validUntil;
    private long mStartTime;

    public GattApduOperation(String connectorId, ApduPackage apduPackage) {

        mResult = new ApduExecutionResult(apduPackage.getPackageId());

        try {
            validUntil = TimestampUtils.getDateForISO8601String(apduPackage.getValidUntil()).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        mSequencesMap = new HashMap<>();

        addNestedOperation(new GattOperation() {
            @Override
            public void execute(BluetoothGatt bluetoothGatt) {
                mStartTime = System.currentTimeMillis();

                mNotificationListener = new ApduNotificationListener(connectorId);
                NotificationManager.getInstance().addListener(mNotificationListener);
            }

            @Override
            public boolean canRunNextOperation() {
                return true;
            }
        });

        for (ApduCommand command : apduPackage.getApduCommands()) {

            mSequencesMap.put(command.getSequence(), command.getCommandId());

            if (command.getCommand().length <= 17) {
                addNestedOperation(new GattApduBasicOperation(command));
            } else {
                addNestedOperation(new GattApduComplexOperation(command));
            }
        }
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        NotificationManager.getInstance().removeListener(mNotificationListener);

        long endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - mStartTime) / 1000);

        @ResponseState.ApduState String state = null;

        if (mSequencesMap.size() != 0) {
            mSequencesMap.clear();
            state = ResponseState.ERROR;
        } else if (validUntil < endTime) {
            state = ResponseState.EXPIRED;
        }

        mResult.setExecutedDuration(duration);
        mResult.setExecutedTsEpoch(endTime);
        if (null != state) {
            mResult.setState(state);
        } else {
            mResult.deriveState();
        }

        RxBus.getInstance().post(mResult);

        mSequencesMap.clear();
        clear();
    }

    @Override
    public boolean canRunNextOperation() {
        return true;
    }

    private class ApduNotificationListener extends Listener {
        public ApduNotificationListener(String connectorId) {
            super(connectorId);

            mCommands.put(IApduMessage.class, data -> {

                IApduMessage apduMessage = (IApduMessage) data;

                int sId = apduMessage.getSequenceId();

                if (mSequencesMap.containsKey(sId)) {

                    RxBus.getInstance().post(Sync.builder()
                            .state(States.INC_PROGRESS)
                            .build());

                    byte[] apduData = apduMessage.getData();
                    String responseCode = Hex.bytesToHexString(Arrays.copyOfRange(apduData, apduData.length - 2, apduData.length));
                    String responseData = Hex.bytesToHexString(apduData);

                    ApduCommandResult result = new ApduCommandResult.Builder()
                            .setCommandId(mSequencesMap.get(sId))
                            .setResponseCode(responseCode)
                            .setResponseData(responseData)
                            .build();

                    mResult.addResponse(result);

                    mSequencesMap.remove(sId);

                    FPLog.i(mSequencesMap.values().toString());
                }
            });

            mCommands.put(ApduExecutionError.class, data -> {
                execute(null);
            });
        }
    }
}
