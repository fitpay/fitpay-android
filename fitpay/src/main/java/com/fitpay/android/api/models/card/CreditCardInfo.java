package com.fitpay.android.api.models.card;

import android.os.Parcel;
import android.os.Parcelable;

import com.fitpay.android.webview.models.IdVerification;

/***
 * Credit card info
 */
public final class CreditCardInfo implements Parcelable {

    /**
     * description : Card holder name
     */
    String name;

    /**
     * description : The credit card cvv2 code
     */
    String cvv;

    /**
     * description : The credit card number, also known as a Primary Account Number (PAN)
     */
    String pan;

    /**
     * description : The credit card expiration month
     */
    Integer expMonth;

    /**
     * description : The credit card expiration year
     */
    Integer expYear;

    /**
     * description : Card holder address
     */
    Address address;

    /**
     * description : Card holder risk data
     */
    IdVerification riskData;

    /**
     * description : Card holder language
     */
    String language;

    CreditCardInfo() {
    }

    @Override
    public String toString() {
        return "CreditCardInfo";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.cvv);
        dest.writeString(this.pan);
        dest.writeValue(this.expMonth);
        dest.writeValue(this.expYear);
        dest.writeParcelable(this.address, flags);
        dest.writeParcelable(this.riskData, flags);
        dest.writeString(this.language);
    }

    protected CreditCardInfo(Parcel in) {
        this.name = in.readString();
        this.cvv = in.readString();
        this.pan = in.readString();
        this.expMonth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.expYear = (Integer) in.readValue(Integer.class.getClassLoader());
        this.address = in.readParcelable(Address.class.getClassLoader());
        this.riskData = in.readParcelable(IdVerification.class.getClassLoader());
        this.language = in.readString();
    }

    public static final Parcelable.Creator<CreditCardInfo> CREATOR = new Parcelable.Creator<CreditCardInfo>() {
        @Override
        public CreditCardInfo createFromParcel(Parcel source) {
            return new CreditCardInfo(source);
        }

        @Override
        public CreditCardInfo[] newArray(int size) {
            return new CreditCardInfo[size];
        }
    };
}
