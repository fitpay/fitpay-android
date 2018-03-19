package com.fitpay.android.paymentdevice.events;

/**
 * Created by tgs on 5/25/16.
 */
public class PaymentDeviceOperationFailed {

    private String reason;
    private int reasonCode;

    private PaymentDeviceOperationFailed() {
        super();
    }

    public static class Builder {

        private String reason;
        private int reasonCode;

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder reasonCode(int reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        public PaymentDeviceOperationFailed build() {
            PaymentDeviceOperationFailed op = new PaymentDeviceOperationFailed();
            op.reason = this.reason;
            op.reasonCode = this.reasonCode;
            return op;
        }
    }
}
