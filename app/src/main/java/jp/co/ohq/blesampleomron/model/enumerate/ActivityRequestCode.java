package jp.co.ohq.blesampleomron.model.enumerate;

import android.support.annotation.NonNull;

import jp.co.ohq.blesampleomron.view.activity.DiscoveredDeviceSelectionActivity;
import jp.co.ohq.blesampleomron.view.activity.SessionActivity;
import jp.co.ohq.blesampleomron.view.activity.UserSelectionActivity;

public enum ActivityRequestCode {
    DiscoveredDeviceSelection(DiscoveredDeviceSelectionActivity.class.hashCode()),
    Session(SessionActivity.class.hashCode()),
    UserSelection(UserSelectionActivity.class.hashCode()),;
    private int mHashCode;

    ActivityRequestCode(int hashCode) {
        mHashCode = hashCode;
    }

    @NonNull
    public static ActivityRequestCode valueOf(int hashCode16) {
        for (ActivityRequestCode type : values()) {
            if (type.hashCode16() == hashCode16) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown hashCode16. " + hashCode16);
    }

    public int hashCode16() {
        return mHashCode & 0x0000ffff;
    }

    public int hashCode32() {
        return mHashCode;
    }
}
