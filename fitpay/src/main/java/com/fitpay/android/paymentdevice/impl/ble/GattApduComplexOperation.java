package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothGatt;

import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationControlBeginMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationControlEndMessage;
import com.fitpay.android.paymentdevice.impl.ble.message.ContinuationPacketMessage;
import com.fitpay.android.utils.Hex;

/**
 * Apdu operation for commands with size > 17 bytes
 */
class GattApduComplexOperation extends GattApduBaseOperation {

    public GattApduComplexOperation(ApduCommand command) {
        super(command.getSequence());

        /*begin*/
        ContinuationControlBeginMessage beingMsg = new ContinuationControlBeginMessage()
                .withUuid(PaymentServiceConstants.CHARACTERISTIC_APDU_CONTROL);

        GattOperation continuationStartWrite = new GattCharacteristicWriteOperation(
                PaymentServiceConstants.SERVICE_UUID,
                PaymentServiceConstants.CHARACTERISTIC_CONTINUATION_CONTROL,
                beingMsg.getMessage());

        addNestedOperation(continuationStartWrite);

        /*packets*/
        int currentPos = 0;
        int sortOrder = 0;
        byte[] dataToSend;

        byte[] byteSequenceId = Hex.sequenceToBytes(command.getSequence());
        byte[] apduCommand = command.getCommand();
        byte[] msg = new byte[3 + apduCommand.length];

        System.arraycopy(byteSequenceId, 0, msg, 1, byteSequenceId.length);
        System.arraycopy(apduCommand, 0, msg, 3, apduCommand.length);

        while (currentPos < msg.length) {
            int len = Math.min(msg.length - currentPos, ContinuationPacketMessage.getMaxDataLength());
            dataToSend = new byte[len];
            System.arraycopy(msg, currentPos, dataToSend, 0, len);

            ContinuationPacketMessage packetMessage = new ContinuationPacketMessage()
                    .withSortOrder(sortOrder)
                    .withData(dataToSend);

            GattOperation packetWrite = new GattCharacteristicWriteOperation(
                    PaymentServiceConstants.SERVICE_UUID,
                    PaymentServiceConstants.CHARACTERISTIC_CONTINUATION_PACKET,
                    packetMessage.getMessage());

            addNestedOperation(packetWrite);

            currentPos += len;
            sortOrder++;
        }

        /*end*/
        ContinuationControlEndMessage endMsg = new ContinuationControlEndMessage()
                .withPayload(msg);

        GattOperation continuationEndWrite = new GattCharacteristicWriteOperation(
                PaymentServiceConstants.SERVICE_UUID,
                PaymentServiceConstants.CHARACTERISTIC_CONTINUATION_CONTROL,
                endMsg.getMessage());

        addNestedOperation(continuationEndWrite);
    }

    @Override
    public void execute(BluetoothGatt bluetoothGatt) {
        //wait for ApduResultMessage
    }
}
