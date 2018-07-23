package com.fitpay.android.api.models.apdu;

import com.fitpay.android.api.enums.APDUPackageCategory;
import com.fitpay.android.api.enums.APDUPackageOperation;
import com.fitpay.android.api.models.BaseModel;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ApduPackageModel extends BaseModel {

    private String seIdType;
    private String targetDeviceType;
    private String targetDeviceId;
    private String packageId;
    private String seId;
    private String targetAid;
    private String validUntil;
    private String apduPackageUrl;
    @APDUPackageCategory.Category
    private String category;
    @APDUPackageOperation.Operation
    private String operation;
    private HashMap<String, Object> metadata;
    @SerializedName("commandApdus")
    private List<ApduCommand> apduCommands;

    public String getSeIdType() {
        return seIdType;
    }

    public String getTargetDeviceType() {
        return targetDeviceType;
    }

    public String getTargetDeviceId() {
        return targetDeviceId;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getSeId() {
        return seId;
    }

    public String getTargetAid() {
        return targetAid;
    }

    public List<ApduCommand> getApduCommands() {
        return apduCommands;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public String getApduPackageUrl() {
        return apduPackageUrl;
    }

    @APDUPackageCategory.Category
    public String getCategory() { return category; }

    @APDUPackageOperation.Operation
    public String getOperation() { return operation; }

    public Map<String, Object> getMetadata() { return metadata; }

    @Override
    public String toString() {
        return "ApduPackage{" +
                "seIdType='" + seIdType + '\'' +
                ", targetDeviceType='" + targetDeviceType + '\'' +
                ", targetDeviceId='" + targetDeviceId + '\'' +
                ", packageId='" + packageId + '\'' +
                ", seId='" + seId + '\'' +
                ", targetAid='" + targetAid + '\'' +
                ", validUntil='" + validUntil + '\'' +
                ", apduPackageUrl='" + apduPackageUrl + '\'' +
                ", apduCommands=" + apduCommands + '\'' +
                ", category='" + category + '\'' +
                ", operation='" + operation + '\'' +
                ", metadata='" + metadata +
                '}';
    }
}
