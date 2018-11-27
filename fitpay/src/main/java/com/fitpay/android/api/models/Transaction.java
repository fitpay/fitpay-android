package com.fitpay.android.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Transaction
 */
public final class Transaction extends BaseModel {

    private String transactionId;
    private String transactionType;
    private double amount;
    private String currencyCode;
    private String authorizationStatus;
    private long transactionTimeEpoch;
    private String merchantName;
    private String merchantCode;
    private String merchantType;

    @SerializedName("encryptedData")
    protected EncryptedTransaction encryptedTransaction;

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionType() {
        return encryptedTransaction != null ? encryptedTransaction.transactionType : transactionType;
    }

    public double getAmount() {
        return encryptedTransaction != null ? encryptedTransaction.amount : amount;
    }

    public String getCurrencyCode() {
        return encryptedTransaction != null ? encryptedTransaction.currencyCode : currencyCode;
    }

    public String getAuthorizationStatus() {
        return encryptedTransaction != null ? encryptedTransaction.authorizationStatus : authorizationStatus;
    }

    public long getTransactionTimeEpoch() {
        return transactionTimeEpoch;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public String getMerchantType() {
        return merchantType;
    }

    @Deprecated
    public String getErchantName() {
        return merchantName;
    }

    @Deprecated
    public String getErchantCode() {
        return merchantCode;
    }

    @Deprecated
    public String getErchantType() {
        return merchantType;
    }

    public final class EncryptedTransaction {
        private String transactionType;
        private double amount;
        private String currencyCode;
        private String authorizationStatus;
        private String merchantName;
        private String merchantCode;
        private String merchantType;
    }

}
