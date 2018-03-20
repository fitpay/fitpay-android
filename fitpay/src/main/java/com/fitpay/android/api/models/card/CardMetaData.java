package com.fitpay.android.api.models.card;

import android.os.Parcel;
import android.os.Parcelable;

import com.fitpay.android.api.models.ImageAssetReference;
import com.fitpay.android.api.models.ImageAssetWithOptionsReference;

import java.util.ArrayList;
import java.util.List;

public final class CardMetaData implements Parcelable {

    private String labelColor;
    private String backgroundColor;
    private String foregroundColor;
    private String issuerName;
    private String shortDescription;
    private String longDescription;
    private String contactUrl;
    private String contactPhone;
    private String contactEmail;
    private String termsAndConditionsUrl;
    private String privacyPolicyUrl;
    private List<ImageAssetReference> brandLogo;
    private List<ImageAssetReference> cardBackground;
    private List<ImageAssetWithOptionsReference> cardBackgroundCombined;
    private List<ImageAssetWithOptionsReference> cardBackgroundCombinedEmbossed;

    //TODO: check values
    private List<ImageAssetReference> icon;
    private List<ImageAssetReference> issuerLogo;

    public String getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getTermsAndConditionsUrl() {
        return termsAndConditionsUrl;
    }

    public void setTermsAndConditionsUrl(String termsAndConditionsUrl) {
        this.termsAndConditionsUrl = termsAndConditionsUrl;
    }

    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }

    public List<ImageAssetReference> getBrandLogo() {
        return brandLogo;
    }

    public void setBrandLogo(List<ImageAssetReference> brandLogo) {
        this.brandLogo = brandLogo;
    }

    public List<ImageAssetReference> getCardBackground() {
        return cardBackground;
    }

    public void setCardBackground(List<ImageAssetReference> cardBackground) {
        this.cardBackground = cardBackground;
    }

    public List<ImageAssetWithOptionsReference> getCardBackgroundCombined() {
        return cardBackgroundCombined;
    }

    public void setCardBackgroundCombined(List<ImageAssetWithOptionsReference> cardBackgroundCombined) {
        this.cardBackgroundCombined = cardBackgroundCombined;
    }

    public List<ImageAssetWithOptionsReference> getCardBackgroundCombinedEmbossed() {
        return cardBackgroundCombinedEmbossed;
    }

    public void setCardBackgroundCombinedEmbossed(List<ImageAssetWithOptionsReference> cardBackgroundCombinedEmbossed) {
        this.cardBackgroundCombinedEmbossed = cardBackgroundCombinedEmbossed;
    }

    public List<ImageAssetReference> getIcon() {
        return icon;
    }

    public void setIcon(List<ImageAssetReference> icon) {
        this.icon = icon;
    }

    public List<ImageAssetReference> getIssuerLogo() {
        return issuerLogo;
    }

    public void setIssuerLogo(List<ImageAssetReference> issuerLogo) {
        this.issuerLogo = issuerLogo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.labelColor);
        dest.writeString(this.backgroundColor);
        dest.writeString(this.foregroundColor);
        dest.writeString(this.issuerName);
        dest.writeString(this.shortDescription);
        dest.writeString(this.longDescription);
        dest.writeString(this.contactUrl);
        dest.writeString(this.contactPhone);
        dest.writeString(this.contactEmail);
        dest.writeString(this.termsAndConditionsUrl);
        dest.writeString(this.privacyPolicyUrl);
        dest.writeList(this.brandLogo);
        dest.writeList(this.cardBackground);
        dest.writeList(this.cardBackgroundCombined);
        dest.writeList(this.cardBackgroundCombinedEmbossed);
        dest.writeList(this.icon);
        dest.writeList(this.issuerLogo);
    }

    public CardMetaData() {
    }

    protected CardMetaData(Parcel in) {
        this.labelColor = in.readString();
        this.backgroundColor = in.readString();
        this.foregroundColor = in.readString();
        this.issuerName = in.readString();
        this.shortDescription = in.readString();
        this.longDescription = in.readString();
        this.contactUrl = in.readString();
        this.contactPhone = in.readString();
        this.contactEmail = in.readString();
        this.termsAndConditionsUrl = in.readString();
        this.privacyPolicyUrl = in.readString();
        this.brandLogo = new ArrayList<>();
        in.readList(this.brandLogo, ImageAssetReference.class.getClassLoader());
        this.cardBackground = new ArrayList<>();
        in.readList(this.cardBackground, ImageAssetReference.class.getClassLoader());
        this.cardBackgroundCombined = new ArrayList<>();
        in.readList(this.cardBackgroundCombined, ImageAssetWithOptionsReference.class.getClassLoader());
        this.cardBackgroundCombinedEmbossed = new ArrayList<>();
        in.readList(this.cardBackgroundCombinedEmbossed, ImageAssetWithOptionsReference.class.getClassLoader());
        this.icon = new ArrayList<>();
        in.readList(this.icon, ImageAssetReference.class.getClassLoader());
        this.issuerLogo = new ArrayList<>();
        in.readList(this.issuerLogo, ImageAssetReference.class.getClassLoader());
    }

    public static final Parcelable.Creator<CardMetaData> CREATOR = new Parcelable.Creator<CardMetaData>() {
        @Override
        public CardMetaData createFromParcel(Parcel source) {
            return new CardMetaData(source);
        }

        @Override
        public CardMetaData[] newArray(int size) {
            return new CardMetaData[size];
        }
    };
}