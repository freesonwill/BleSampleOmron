package jp.co.ohq.blesampleomron.model.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQBloodPressureMeasurementStatus;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.blesampleomron.controller.util.Converter;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.enumerate.ComType;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;
import jp.co.ohq.blesampleomron.model.enumerate.ResultType;

public class HistoryData implements Parcelable {

    public static final Creator<HistoryData> CREATOR = new Creator<HistoryData>() {
        @Override
        public HistoryData createFromParcel(Parcel source) {
            return new HistoryData(source);
        }

        @Override
        public HistoryData[] newArray(int size) {
            return new HistoryData[size];
        }
    };
    private String resultType;
    private Long receivedDate;
    private String userName;
    private String comType;
    private String protocol;
    private String address;
    private String localName;
    private String completeLocalName;
    private Integer consentCode;
    private String completionReason;
    private String deviceCategory;
    private String modelName;
    private String currentTime;
    private Integer batteryLevel;
    private Integer userIndex;
    private Long databaseChangeIncrement;
    private Integer sequenceNumberOfLatestRecord;
    private String userDataJson;
    private String measurementRecordsJson;
    public HistoryData() {
    }

    protected HistoryData(Parcel in) {
        this.resultType = in.readString();
        this.receivedDate = (Long) in.readValue(Long.class.getClassLoader());
        this.userName = in.readString();
        this.comType = in.readString();
        this.protocol = in.readString();
        this.address = in.readString();
        this.localName = in.readString();
        this.completeLocalName = in.readString();
        this.consentCode = (Integer) in.readValue(Integer.class.getClassLoader());
        this.completionReason = in.readString();
        this.deviceCategory = in.readString();
        this.modelName = in.readString();
        this.currentTime = in.readString();
        this.batteryLevel = (Integer) in.readValue(Integer.class.getClassLoader());
        this.userIndex = (Integer) in.readValue(Integer.class.getClassLoader());
        this.databaseChangeIncrement = (Long) in.readValue(Long.class.getClassLoader());
        this.sequenceNumberOfLatestRecord = (Integer) in.readValue(Integer.class.getClassLoader());
        this.userDataJson = in.readString();
        this.measurementRecordsJson = in.readString();
    }

    @NonNull
    private Object restore(@NonNull String value) {
        AppLog.d(value);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public ResultType getResultType() {
        if (null == resultType) {
            return null;
        }
        return ResultType.valueOf(resultType);
    }

    public void setResultType(ResultType resultType) {
        if (null == resultType) {
            return;
        }
        this.resultType = resultType.name();
    }

    @NonNull
    public Map<OHQUserDataKey, Object> getUserData() {
        final Map<OHQUserDataKey, Object> ret = new LinkedHashMap<>();
        if (null == userDataJson) {
            return ret;
        }
        for (Map.Entry<String, String> entry : Converter.toMap(userDataJson).entrySet()) {
            OHQUserDataKey key = OHQUserDataKey.valueOf(entry.getKey());
            if (OHQUserDataKey.GenderKey.equals(key)) {
                ret.put(key, OHQGender.valueOf(entry.getValue()));
            } else {
                ret.put(key, restore(entry.getValue()));
            }
        }
        return ret;
    }

    public void setUserData(Map<OHQUserDataKey, Object> userData) {
        if (null == userData) {
            return;
        }
        final Map<String, String> userDataAsString = new LinkedHashMap<>();
        for (Map.Entry<OHQUserDataKey, Object> entry : userData.entrySet()) {
            userDataAsString.put(entry.getKey().name(), entry.getValue().toString());
        }
        userDataJson = Converter.toJson(userDataAsString);
    }

    @NonNull
    public LinkedList<Map<OHQMeasurementRecordKey, Object>> getMeasurementRecords() {
        final LinkedList<Map<OHQMeasurementRecordKey, Object>> ret = new LinkedList<>();
        if (null == measurementRecordsJson) {
            return ret;
        }
        for (Map<String, String> recordAsString : Converter.toMapList(measurementRecordsJson)) {
            final Map<OHQMeasurementRecordKey, Object> record = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : recordAsString.entrySet()) {
                OHQMeasurementRecordKey key = OHQMeasurementRecordKey.valueOf(entry.getKey());
                if (OHQMeasurementRecordKey.BloodPressureMeasurementStatusKey.equals(key)) {
                    Type t = (new TypeToken<EnumSet<OHQBloodPressureMeasurementStatus>>() {
                    }).getType();
                    Gson gson = new GsonBuilder().registerTypeAdapter(t, new BloodPressureMeasurementStatusDeserializer()).create();
                    record.put(key, gson.fromJson(entry.getValue(), t));
                } else {
                    record.put(key, restore(entry.getValue()));
                }
            }
            ret.add(record);
        }
        return ret;
    }

    public void setMeasurementRecords(List<Map<OHQMeasurementRecordKey, Object>> measurementRecords) {
        if (null == measurementRecords) {
            return;
        }
        final LinkedList<Map<String, String>> measurementRecordsAsString = new LinkedList<>();
        for (Map<OHQMeasurementRecordKey, Object> record : measurementRecords) {
            final Map<String, String> recordAsStringKey = new HashMap<>();
            for (Map.Entry<OHQMeasurementRecordKey, Object> entry : record.entrySet()) {
                if (OHQMeasurementRecordKey.BloodPressureMeasurementStatusKey.equals(entry.getKey())) {
                    Type t = (new TypeToken<EnumSet<OHQBloodPressureMeasurementStatus>>() {
                    }).getType();
                    Gson gson = new GsonBuilder().registerTypeAdapter(t, new BloodPressureMeasurementStatusSerializer()).create();
                    recordAsStringKey.put(entry.getKey().name(), gson.toJson(entry.getValue()));
                } else {
                    recordAsStringKey.put(entry.getKey().name(), entry.getValue().toString());
                }
            }
            measurementRecordsAsString.add(recordAsStringKey);
        }
        measurementRecordsJson = Converter.toJson(measurementRecordsAsString);
    }

    public Long getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Long receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ComType getComType() {
        if (null == comType) {
            return null;
        }
        return ComType.valueOf(comType);
    }

    public void setComType(ComType comType) {
        if (null == comType) {
            return;
        }
        this.comType = comType.name();
    }

    public Protocol getProtocol() {
        if (null == protocol) {
            return null;
        }
        return Protocol.valueOf(protocol);
    }

    public void setProtocol(Protocol protocol) {
        if (null == protocol) {
            return;
        }
        this.protocol = protocol.name();
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

    public Integer getConsentCode() {
        return consentCode;
    }

    public void setConsentCode(Integer consentCode) {
        this.consentCode = consentCode;
    }

    public OHQCompletionReason getCompletionReason() {
        if (null == completionReason) {
            return null;
        }
        return OHQCompletionReason.valueOf(completionReason);
    }

    public void setCompletionReason(OHQCompletionReason completionReason) {
        if (null == completionReason) {
            return;
        }
        this.completionReason = completionReason.name();
    }

    public OHQDeviceCategory getDeviceCategory() {
        if (null == deviceCategory) {
            return null;
        }
        return OHQDeviceCategory.valueOf(deviceCategory);
    }

    public void setDeviceCategory(OHQDeviceCategory deviceCategory) {
        if (null == deviceCategory) {
            return;
        }
        this.deviceCategory = deviceCategory.name();
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Integer getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(Integer userIndex) {
        this.userIndex = userIndex;
    }

    public Long getDatabaseChangeIncrement() {
        return databaseChangeIncrement;
    }

    public void setDatabaseChangeIncrement(Long databaseChangeIncrement) {
        this.databaseChangeIncrement = databaseChangeIncrement;
    }

    public Integer getSequenceNumberOfLatestRecord() {
        return sequenceNumberOfLatestRecord;
    }

    public void setSequenceNumberOfLatestRecord(Integer sequenceNumberOfLatestRecord) {
        this.sequenceNumberOfLatestRecord = sequenceNumberOfLatestRecord;
    }

    @Override
    public String toString() {
        return "HistoryData{" +
                "resultType='" + resultType + '\'' +
                ", receivedDate=" + receivedDate +
                ", userName='" + userName + '\'' +
                ", comType='" + comType + '\'' +
                ", protocol='" + protocol + '\'' +
                ", address='" + address + '\'' +
                ", localName='" + localName + '\'' +
                ", completeLocalName='" + completeLocalName + '\'' +
                ", consentCode=" + consentCode +
                ", completionReason='" + completionReason + '\'' +
                ", deviceCategory='" + deviceCategory + '\'' +
                ", modelName='" + modelName + '\'' +
                ", currentTime='" + currentTime + '\'' +
                ", batteryLevel=" + batteryLevel +
                ", userIndex=" + userIndex +
                ", databaseChangeIncrement=" + databaseChangeIncrement +
                ", sequenceNumberOfLatestRecord=" + sequenceNumberOfLatestRecord +
                ", userDataJson='" + userDataJson + '\'' +
                ", measurementRecordsJson='" + measurementRecordsJson + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.resultType);
        dest.writeValue(this.receivedDate);
        dest.writeString(this.userName);
        dest.writeString(this.comType);
        dest.writeString(this.protocol);
        dest.writeString(this.address);
        dest.writeString(this.localName);
        dest.writeString(this.completeLocalName);
        dest.writeValue(this.consentCode);
        dest.writeString(this.completionReason);
        dest.writeString(this.deviceCategory);
        dest.writeString(this.modelName);
        dest.writeString(this.currentTime);
        dest.writeValue(this.batteryLevel);
        dest.writeValue(this.userIndex);
        dest.writeValue(this.databaseChangeIncrement);
        dest.writeValue(this.sequenceNumberOfLatestRecord);
        dest.writeString(this.userDataJson);
        dest.writeString(this.measurementRecordsJson);
    }

    private static class BloodPressureMeasurementStatusSerializer implements JsonSerializer<EnumSet<OHQBloodPressureMeasurementStatus>> {
        @Override
        public JsonElement serialize(EnumSet<OHQBloodPressureMeasurementStatus> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (OHQBloodPressureMeasurementStatus status : src) {
                array.add(status.name());
            }
            return array;
        }
    }

    private static class BloodPressureMeasurementStatusDeserializer implements JsonDeserializer<EnumSet<OHQBloodPressureMeasurementStatus>> {
        @Override
        public EnumSet<OHQBloodPressureMeasurementStatus> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EnumSet<OHQBloodPressureMeasurementStatus> ret = EnumSet.noneOf(OHQBloodPressureMeasurementStatus.class);
            for (JsonElement elem : json.getAsJsonArray()) {
                ret.add(OHQBloodPressureMeasurementStatus.valueOf(elem.getAsString()));
            }
            return ret;
        }
    }
}
