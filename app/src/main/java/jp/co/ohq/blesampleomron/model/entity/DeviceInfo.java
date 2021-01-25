package jp.co.ohq.blesampleomron.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;

public class DeviceInfo extends RealmObject implements Parcelable {
    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel source) {
            return new DeviceInfo(source);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };
    @LinkingObjects("registeredDevices")
    private final RealmResults<UserInfo> users = null;
    private String address;
    private String localName;
    private String completeLocalName;
    private String modelName;
    private String deviceCategory;
    private String protocol;
    private Integer userIndex;
    private Integer consentCode;
    private Integer sequenceNumberOfLatestRecord;
    private Integer numberOfRecords;
    private Long databaseChangeIncrement;
    private boolean userDataUpdateFlag;

    public DeviceInfo() {
    }

    protected DeviceInfo(Parcel in) {
        this.address = in.readString();
        this.localName = in.readString();
        this.completeLocalName = in.readString();
        this.modelName = in.readString();
        this.deviceCategory = in.readString();
        this.protocol = in.readString();
        this.userIndex = (Integer) in.readValue(Integer.class.getClassLoader());
        this.consentCode = (Integer) in.readValue(Integer.class.getClassLoader());
        this.sequenceNumberOfLatestRecord = (Integer) in.readValue(Integer.class.getClassLoader());
        this.numberOfRecords = (Integer) in.readValue(Integer.class.getClassLoader());
        this.databaseChangeIncrement = (Long) in.readValue(Long.class.getClassLoader());
        this.userDataUpdateFlag = in.readByte() != 0;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getCompleteLocalName() {
        return completeLocalName;
    }

    public void setCompleteLocalName(String completeLocalName) {
        this.completeLocalName = completeLocalName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public OHQDeviceCategory getDeviceCategory() {
        if (null == deviceCategory) {
            return null;
        }
        return OHQDeviceCategory.valueOf(deviceCategory);
    }

    public void setDeviceCategory(OHQDeviceCategory deviceCategory) {
        if (null == deviceCategory) {
            this.deviceCategory = null;
        } else {
            this.deviceCategory = deviceCategory.name();
        }
    }

    public Protocol getProtocol() {
        if (null == protocol) {
            return null;
        }
        return Protocol.valueOf(protocol);
    }

    public void setProtocol(Protocol protocol) {
        if (null == protocol) {
            this.protocol = null;
        } else {
            this.protocol = protocol.name();
        }
    }

    public Integer getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(Integer userIndex) {
        this.userIndex = userIndex;
    }

    public Integer getConsentCode() {
        return consentCode;
    }

    public void setConsentCode(Integer consentCode) {
        this.consentCode = consentCode;
    }

    public Integer getSequenceNumberOfLatestRecord() {
        return sequenceNumberOfLatestRecord;
    }

    public void setSequenceNumberOfLatestRecord(Integer sequenceNumberOfLatestRecord) {
        this.sequenceNumberOfLatestRecord = sequenceNumberOfLatestRecord;
    }

    public Integer getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(Integer numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public Long getDatabaseChangeIncrement() {
        return databaseChangeIncrement;
    }

    public void setDatabaseChangeIncrement(Long databaseChangeIncrement) {
        AppLog.vMethodIn(address + " " + this.databaseChangeIncrement + " -> " + databaseChangeIncrement);
        this.databaseChangeIncrement = databaseChangeIncrement;
    }

    public boolean isUserDataUpdateFlag() {
        return userDataUpdateFlag;
    }

    public void setUserDataUpdateFlag(boolean userDataUpdateFlag) {
        AppLog.vMethodIn(address + " " + this.userDataUpdateFlag + " -> " + userDataUpdateFlag);
        this.userDataUpdateFlag = userDataUpdateFlag;
    }

    public RealmResults<UserInfo> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "address='" + address + '\'' +
                ", localName='" + localName + '\'' +
                ", completeLocalName='" + completeLocalName + '\'' +
                ", modelName='" + modelName + '\'' +
                ", deviceCategory='" + deviceCategory + '\'' +
                ", protocol='" + protocol + '\'' +
                ", userIndex=" + userIndex +
                ", consentCode=" + consentCode +
                ", sequenceNumberOfLatestRecord=" + sequenceNumberOfLatestRecord +
                ", numberOfRecords=" + numberOfRecords +
                ", databaseChangeIncrement=" + databaseChangeIncrement +
                ", userDataUpdateFlag=" + userDataUpdateFlag +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeString(this.localName);
        dest.writeString(this.completeLocalName);
        dest.writeString(this.modelName);
        dest.writeString(this.deviceCategory);
        dest.writeString(this.protocol);
        dest.writeValue(this.userIndex);
        dest.writeValue(this.consentCode);
        dest.writeValue(this.sequenceNumberOfLatestRecord);
        dest.writeValue(this.numberOfRecords);
        dest.writeValue(this.databaseChangeIncrement);
        dest.writeByte(this.userDataUpdateFlag ? (byte) 1 : (byte) 0);
    }
}
