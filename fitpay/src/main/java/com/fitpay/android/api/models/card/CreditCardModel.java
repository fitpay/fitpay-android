package com.fitpay.android.api.models.card;

import com.fitpay.android.api.enums.CardInitiators;
import com.fitpay.android.api.models.AssetReference;
import com.fitpay.android.api.models.BaseModel;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Credit card model
 */
abstract class CreditCardModel extends BaseModel {

    protected String creditCardId;
    protected String userId;
    @SerializedName("default")
    protected Boolean defaultX;
    protected Long createdTsEpoch;
    protected Long lastModifiedTsEpoch;
    protected String state;
    @CardInitiators.Initiator
    protected String causedBy;
    protected String cardType;
    protected CardMetaData cardMetaData;
    protected String targetDeviceId;
    protected String targetDeviceType;
    protected String externalTokenReference;
    protected List<VerificationMethod> verificationMethods;
    @SerializedName("encryptedData")
    protected CreditCardInfo creditCardInfo;
    protected String termsAssetId;
    protected Long eligibilityExpirationEpoch;
    protected List<AssetReference> termsAssetReferences;
    protected OfflineSeActions offlineSeActions;
    protected String tokenLastFour;

    protected CreditCardModel() {
        creditCardInfo = new CreditCardInfo();
    }

    public String getCreditCardId() {
        return creditCardId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isDefault() {
        if (null == defaultX) {
            return false;
        }
        return defaultX;
    }

    public long getCreatedTsEpoch() {
        return createdTsEpoch;
    }

    public long getLastModifiedTsEpoch() {
        return lastModifiedTsEpoch;
    }

    public String getState() {
        return state;
    }

    public String getCausedBy() {
        return causedBy;
    }

    public String getCardType() {
        return cardType;
    }

    public CardMetaData getCardMetaData() {
        return cardMetaData;
    }

    public String getTargetDeviceId() {
        return targetDeviceId;
    }

    public String getTargetDeviceType() {
        return targetDeviceType;
    }

    public String getExternalTokenReference() {
        return externalTokenReference;
    }

    public List<VerificationMethod> getVerificationMethods() {
        return verificationMethods;
    }

    /**
     * Terms & Conditions references
     *
     * @return T&C links
     */
    public List<AssetReference> getTermsAssetReferences() {
        return termsAssetReferences;
    }

    public String getName() {
        return creditCardInfo.name;
    }

    public String getCVV() {
        return creditCardInfo.cvv;
    }

    public String getPan() {
        return creditCardInfo.pan;
    }

    public Integer getExpMonth() {
        return creditCardInfo.expMonth;
    }

    public Integer getExpYear() {
        return creditCardInfo.expYear;
    }

    public Address getAddress() {
        return creditCardInfo.address;
    }

    public TopOfWallet getTOW() {
        return offlineSeActions != null ? offlineSeActions.getTopOfWallet() : null;
    }

    public String getTokenLastFour() {
        return tokenLastFour;
    }
}
