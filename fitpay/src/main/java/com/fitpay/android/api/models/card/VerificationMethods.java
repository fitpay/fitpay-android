package com.fitpay.android.api.models.card;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.models.BaseModel;

import java.util.List;

/**
 * Credit card verification methods.
 * Response for {@link com.fitpay.android.api.ApiManager#getVerificationMethods(String, String, ApiCallback)}
 */
public class VerificationMethods extends BaseModel {

    private String creditCardId;
    private List<VerificationMethod> verificationMethods;

    public String getCreditCardId() {
        return creditCardId;
    }

    public List<VerificationMethod> getVerificationMethods() {
        return verificationMethods;
    }
}
