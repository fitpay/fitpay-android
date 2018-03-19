package com.fitpay.android.paymentdevice.callbacks;

import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.events.PaymentDeviceOperationFailed;
import com.fitpay.android.paymentdevice.interfaces.IControlMessage;
import com.fitpay.android.paymentdevice.interfaces.INotificationMessage;
import com.fitpay.android.paymentdevice.interfaces.ISecureMessage;

/**
 * Payment device callbacks
 */
public abstract class PaymentDeviceListener extends ConnectionListener implements IListeners.PaymentDeviceListener {
    public PaymentDeviceListener(String filter) {
        super(filter);
        mCommands.put(Device.class, data -> onDeviceInfoReceived((Device) data));
        mCommands.put(PaymentDeviceOperationFailed.class, data -> onDeviceOperationFailed((PaymentDeviceOperationFailed) data));
        mCommands.put(IControlMessage.class, data -> {
            IControlMessage message = (IControlMessage) data;
            onApplicationControlReceived(message.getData());
        });
        mCommands.put(INotificationMessage.class, data -> {
            INotificationMessage message = (INotificationMessage) data;
            onNotificationReceived(message.getData());
        });
        mCommands.put(ISecureMessage.class, data -> {
            ISecureMessage message = (ISecureMessage) data;
            onNFCStateReceived(message.isNfcEnabled(), message.getNfcErrorCode());
        });
    }
}
