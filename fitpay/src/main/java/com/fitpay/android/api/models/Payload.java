package com.fitpay.android.api.models;

import com.fitpay.android.api.enums.CommitTypes;
import com.fitpay.android.api.models.apdu.ApduPackage;
import com.fitpay.android.api.models.device.CreditCardCommit;

/**
 * Payload
 */
public final class Payload {
    private CreditCardCommit creditCard;
    private ApduPackage apduPackage;

    public Payload(CreditCardCommit creditCard) {
        this.creditCard = creditCard;
    }

    public Payload(ApduPackage apduPackage) {
        this.apduPackage = apduPackage;
    }

    public Object getData(@CommitTypes.Type String type) {
        Object dataToReturn;

        switch (type) {
            case CommitTypes.APDU_PACKAGE:
                dataToReturn = apduPackage;
                break;

            default:
                dataToReturn = creditCard;
                break;
        }

        return dataToReturn;
    }
}
