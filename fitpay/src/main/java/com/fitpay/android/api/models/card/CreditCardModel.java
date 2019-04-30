package com.fitpay.android.api.models.card;

import androidx.annotation.Nullable;

import com.fitpay.android.api.enums.CardInitiators;
import com.fitpay.android.api.enums.OfflineSeActionTypes;
import com.fitpay.android.api.models.AssetReference;
import com.fitpay.android.api.models.BaseModel;
import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.utils.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Credit card model
 */
abstract class CreditCardModel extends BaseModel {

    protected String creditCardId;
    protected String userId;

    /**
     * @deprecated as of v1.1.0
     */
    @Deprecated
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

    /**
     * @deprecated as of v1.1.0 - will stop being returned from the server
     */
    @Deprecated
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

    /**
     * Credit card inf
     *
     * @return creditCardInfo
     */
    public CreditCardInfo getCreditCardInfo() {
        return creditCardInfo;
    }

    /**
     * @deprecated as of v1.3.2
     *
     * use getCreditCardInfo
     * */
    @Deprecated
    public String getName() {
        return creditCardInfo.name;
    }

    /**
     * @deprecated as of v1.3.2
     *
     * use getCreditCardInfo
     * */
    @Deprecated
    public String getCVV() {
        return creditCardInfo.cvv;
    }

    /**
     * @deprecated as of v1.3.2
     *
     * use getCreditCardInfo
     * */
    @Deprecated
    public String getPan() {
        return creditCardInfo.pan;
    }

    /**
     * @deprecated as of v1.3.2
     *
     * use getCreditCardInfo
     * */
    @Deprecated
    public Integer getExpMonth() {
        return creditCardInfo.expMonth;
    }

    /**
     * @deprecated as of v1.3.2
     *
     * use getCreditCardInfo
     * */
    @Deprecated
    public Integer getExpYear() {
        return creditCardInfo.expYear;
    }

    /**
     * @deprecated as of v1.3.2
     *
     * use getCreditCardInfo
     * */
    @Deprecated
    public Address getAddress() {
        return creditCardInfo.address;
    }

    /**
     * @deprecated  As of release 1.7.0, replaced by {@link #getOfflineSeAction(String)} ()}
     */
    @Deprecated
    @Nullable
    public TopOfWallet getTOW() {
        SeAction action = offlineSeActions != null ?
                offlineSeActions.getAction(OfflineSeActionTypes.TOP_OF_WALLET) : null;
        //fallback to old class
        return Constants.getGson().fromJson(Constants.getGson().toJson(action), TopOfWallet.class);
    }

    /**
     * Get offline SE action commands
     * @param type action
     * @return list of apdu commands or null
     */
    @Nullable
    public List<ApduCommand> getOfflineSeAction(@OfflineSeActionTypes.Type String type){
        return offlineSeActions != null ? offlineSeActions.getCommands(type) : null;
    }

    public String getTokenLastFour() {
        return tokenLastFour;
    }

}
