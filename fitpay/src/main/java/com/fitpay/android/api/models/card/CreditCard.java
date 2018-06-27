package com.fitpay.android.api.models.card;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.CardInitiators;
import com.fitpay.android.api.models.AssetReference;
import com.fitpay.android.api.models.Links;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.DeviceRef;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.webview.models.IdVerification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Credit card info
 */
public final class CreditCard extends CreditCardModel implements Parcelable {

    private static final String ACCEPT_TERMS = "acceptTerms";
    private static final String DECLINE_TERMS = "declineTerms";
    private static final String REACTIVATE = "reactivate";
    private static final String DEACTIVATE = "deactivate";
    private static final String TRANSACTIONS = "transactions";
    private static final String MAKE_DEFAULT = "makeDefault";
    private static final String SELECTED_VERIFICATION = "selectedVerification";
    private static final String VERIFICATION_METHODS = "verificationMethods";

    private List<DeviceRef> deviceRelationships;

    public List<DeviceRef> getDeviceRelationships() {
        return deviceRelationships;
    }

    /**
     * Indicate a user has accepted the terms and conditions presented
     * when the credit card was first added to the user's profile.
     * This link will only be available when the credit card is awaiting the user
     * to accept or decline the presented terms and conditions.
     *
     * <b>Important note:</b>
     * <p>
     * @see User#createCreditCard
     * <p>
     *
     * @param callback result callback
     */
    public void acceptTerms(@NonNull ApiCallback<CreditCard> callback) {
        makePostCall(ACCEPT_TERMS, null, CreditCard.class, callback);
    }

    public boolean canAcceptTerms() {
        return hasLink(ACCEPT_TERMS);
    }

    public boolean hasLink(String linkName) {
        return null != links.getLink(linkName);
    }

    /**
     * Indicate a user has declined the terms and conditions.
     * Once declined the credit card will be in a final state, no other actions may be taken.
     * This link will only be available when the credit card is awaiting the user to accept
     * or decline the presented terms and conditions.
     *
     * @param callback result callback
     */
    public void declineTerms(@NonNull ApiCallback<CreditCard> callback) {
        makePostCall(DECLINE_TERMS, null, CreditCard.class, callback);
    }

    public boolean canDeclineTerms() {
        return hasLink(DECLINE_TERMS);
    }

    /**
     * Transition the credit card into an active state where it can be utilized for payment.
     * This link will only be available for qualified credit cards that are currently in a deactivated state.
     *
     * @param reason   reason data:(causedBy, reason)
     * @param callback result callback
     */
    public void reactivate(@NonNull Reason reason, @NonNull ApiCallback<CreditCard> callback) {
        makePostCall(REACTIVATE, reason, CreditCard.class, callback);
    }

    public boolean canReactivate() {
        return hasLink(REACTIVATE);
    }

    /**
     * Transition the credit card into a deactivated state so that it may not be utilized for payment.
     * This link will only be available for qualified credit cards that are currently in an active state.
     *
     * @param reason   reason data:(causedBy, reason)
     * @param callback result callback
     */
    public void deactivate(@NonNull Reason reason, @NonNull ApiCallback<CreditCard> callback) {
        makePostCall(DEACTIVATE, reason, CreditCard.class, callback);
    }

    public boolean canDeactivate() {
        return hasLink(DEACTIVATE);
    }


    /**
     * Mark the credit card as the default payment instrument.
     * If another card is currently marked as the default,
     * the default will automatically transition to the indicated credit card.
     *
     * @param callback result callback
     */
    public void makeDefault(@NonNull ApiCallback<Void> callback) {
        makePostCall(MAKE_DEFAULT, null, Void.class, callback);
    }

    public boolean canMakeDefault() {
        return hasLink(MAKE_DEFAULT);
    }


    /**
     * Delete a single credit card from a user's profile.
     * If you delete a card that is currently the default source,
     * then the most recently added source will become the new default.
     * If you delete a card that is the last remaining source on the customer
     * then the default_source attribute will become null.
     *
     * @param callback result callback
     */
    public void deleteCard(@NonNull ApiCallback<Void> callback) {
        makeDeleteCall(callback);
    }

    public boolean canDelete() {
        return state != "DELETED" && hasLink(SELF);
    }

    /**
     * Update the details of an existing credit card.
     *
     * @param name name
     * @param address address {@link Address}
     * @param callback   result callback
     */
    public void updateCard(String name, Address address, @NonNull ApiCallback<CreditCard> callback) {
        CreditCardUpdateModel updateModel = new CreditCardUpdateModel();
        updateModel.name = name;
        updateModel.address = address;
        makePatchCall(updateModel, true, CreditCard.class, callback);
    }

    public boolean canUpdateCard() {
        return state != "DELETED" && hasLink(SELF);
    }


    /**
     * Get all transactions.
     *
     * @param limit    Max number of transactions per page, default: 10
     * @param offset   Start index position for list of entities returned
     * @param callback result callback
     */
    public void getTransactions(int limit, int offset, ApiCallback<Collections.TransactionCollection> callback) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("limit", limit);
        queryMap.put("offset", offset);
        makeGetCall(TRANSACTIONS, queryMap, Collections.TransactionCollection.class, callback);
    }

    public boolean canGetTransactions() {
        return hasLink(TRANSACTIONS);
    }

    /**
     * Get acceptTerms url
     *
     * <p>
     * @see User#createCreditCard
     * <p>
     *
     * @return acceptTerms url
     */
    @Nullable
    public String getAcceptTermsUrl() {
        return getLinkUrl(ACCEPT_TERMS);
    }

    /**
     * Update acceptTerms url
     *
     * <p>
     * @see User#createCreditCard
     * </p>
     *
     * @param acceptTermsUrl url
     */
    public void setAcceptTermsUrl(@NonNull String acceptTermsUrl) throws IllegalAccessException{
        if (hasLink(ACCEPT_TERMS)) {
            links.setLink(ACCEPT_TERMS, acceptTermsUrl);
        } else {
            throw new IllegalAccessException("The card is not in a state to accept terms anymore");
        }
    }

    /**
     * Get selected verification method to verify the identity of the cardholder
     *
     * @param callback result callback
     */
    public void getSelectedVerificationMethod(@NonNull ApiCallback<VerificationMethod> callback) {
        makeGetCall(SELECTED_VERIFICATION, null, VerificationMethod.class, callback);
    }

    /**
     * Provides a fresh list of available verification methods for current credit card
     *
     * @param callback result callback
     */
    public void getVerificationMethods(@NonNull ApiCallback<VerificationMethods> callback) {
        makeGetCall(VERIFICATION_METHODS, null, VerificationMethods.class, callback);
    }

    public CreditCard() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.creditCardId);
        dest.writeString(this.userId);
        dest.writeValue(this.defaultX);
        dest.writeValue(this.createdTsEpoch);
        dest.writeValue(this.lastModifiedTsEpoch);
        dest.writeString(this.state);
        dest.writeString(this.causedBy);
        dest.writeString(this.cardType);
        dest.writeParcelable(this.cardMetaData, flags);
        dest.writeString(this.targetDeviceId);
        dest.writeString(this.targetDeviceType);
        dest.writeString(this.externalTokenReference);
        dest.writeList(this.verificationMethods);
        dest.writeParcelable(this.creditCardInfo, flags);
        dest.writeString(this.termsAssetId);
        dest.writeValue(this.eligibilityExpirationEpoch);
        dest.writeList(this.termsAssetReferences);
        dest.writeList(this.deviceRelationships);
        dest.writeParcelable(this.links, flags);
    }

    protected CreditCard(Parcel in) {
        this.creditCardId = in.readString();
        this.userId = in.readString();
        this.defaultX = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.createdTsEpoch = (Long) in.readValue(Long.class.getClassLoader());
        this.lastModifiedTsEpoch = (Long) in.readValue(Long.class.getClassLoader());
        this.state = in.readString();
        @CardInitiators.Initiator String cb = in.readString();
        this.causedBy = cb;
        this.cardType = in.readString();
        this.cardMetaData = in.readParcelable(CardMetaData.class.getClassLoader());
        this.targetDeviceId = in.readString();
        this.targetDeviceType = in.readString();
        this.externalTokenReference = in.readString();
        this.verificationMethods = new ArrayList<>();
        in.readList(this.verificationMethods, VerificationMethod.class.getClassLoader());
        this.creditCardInfo = in.readParcelable(CreditCardInfo.class.getClassLoader());
        this.termsAssetId = in.readString();
        this.eligibilityExpirationEpoch = (Long) in.readValue(Long.class.getClassLoader());
        this.termsAssetReferences = new ArrayList<>();
        in.readList(this.termsAssetReferences, AssetReference.class.getClassLoader());
        this.deviceRelationships = new ArrayList<>();
        in.readList(this.deviceRelationships, DeviceRef.class.getClassLoader());
        this.links = in.readParcelable(Links.class.getClassLoader());
    }

    public static final Parcelable.Creator<CreditCard> CREATOR = new Parcelable.Creator<CreditCard>() {
        @Override
        public CreditCard createFromParcel(Parcel source) {
            return new CreditCard(source);
        }

        @Override
        public CreditCard[] newArray(int size) {
            return new CreditCard[size];
        }
    };

    private static class CreditCardUpdateModel {
        String name;
        Address address;
    }
}