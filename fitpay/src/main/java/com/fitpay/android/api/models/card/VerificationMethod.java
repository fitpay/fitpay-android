package com.fitpay.android.api.models.card;


import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.models.Links;
import com.google.gson.JsonObject;

/**
 * Card verification method
 */
public final class VerificationMethod extends VerificationMethodModel implements Parcelable{
    private static final String SELECT = "select";
    private static final String VERIFY = "verify";

    /**
     * When an issuer requires additional authentication to verify the identity of the cardholder,
     * this indicates the user has selected the specified verification method.
     *
     * @param callback result callback
     */
    public void select(@NonNull ApiCallback<VerificationMethod> callback) {
        makePostCall(SELECT, null, VerificationMethod.class, callback);
    }

    /**
     * If a verification method is selected that requires an entry of a pin code, this transition will be available.
     * Not all verification methods will include a secondary verification step through the FitPay API.
     *
     * @param callback result callback
     */
    public void verify(@NonNull String verificationCode, @NonNull ApiCallback<VerificationMethod> callback) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("verificationCode", verificationCode);
        makePostCall(VERIFY, jsonObject, VerificationMethod.class, callback);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.verificationId);
        dest.writeString(this.state);
        dest.writeString(this.methodType);
        dest.writeString(this.value);
        dest.writeString(this.verificationResult);
        dest.writeLong(this.createdTsEpoch);
        dest.writeLong(this.lastModifiedTsEpoch);
        dest.writeLong(this.verifiedTsEpoch);
        dest.writeParcelable(this.links, flags);
    }

    public VerificationMethod() {
    }

    protected VerificationMethod(Parcel in) {
        this.verificationId = in.readString();
        this.state = in.readString();
        this.methodType = in.readString();
        this.value = in.readString();
        this.verificationResult = in.readString();
        this.createdTsEpoch = in.readLong();
        this.lastModifiedTsEpoch = in.readLong();
        this.verifiedTsEpoch = in.readLong();
        this.links = in.readParcelable(Links.class.getClassLoader());
    }

    public static final Parcelable.Creator<VerificationMethod> CREATOR = new Parcelable.Creator<VerificationMethod>() {
        @Override
        public VerificationMethod createFromParcel(Parcel source) {
            return new VerificationMethod(source);
        }

        @Override
        public VerificationMethod[] newArray(int size) {
            return new VerificationMethod[size];
        }
    };
}