package jp.co.ohq.blesampleomron.model.entity;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.co.ohq.ble.enumerate.OHQGender;

public class UserInfo extends RealmObject {
    @NonNull
    @PrimaryKey
    private String name = "Dummy User";
    @NonNull
    private String dateOfBirth = "1980-01-06";
    @NonNull
    private Float height = 0.0f;
    @NonNull
    private String gender = OHQGender.Male.name();
    @NonNull
    private RealmList<DeviceInfo> registeredDevices = new RealmList<>();

    public UserInfo() {
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@NonNull String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @NonNull
    public BigDecimal getHeight() {
        return BigDecimal.valueOf(height).setScale(1, RoundingMode.HALF_UP);
    }

    public void setHeight(@NonNull BigDecimal height) {
        this.height = height.floatValue();
    }

    @NonNull
    public OHQGender getGender() {
        return OHQGender.valueOf(gender);
    }

    public void setGender(@NonNull OHQGender gender) {
        this.gender = gender.name();
    }

    @NonNull
    public RealmList<DeviceInfo> getRegisteredDevices() {
        return registeredDevices;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", height=" + height +
                ", gender='" + gender + '\'' +
                ", registeredDevices=" + registeredDevices +
                '}';
    }
}
