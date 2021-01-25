package jp.co.ohq.androidcorebluetooth;

import android.support.annotation.NonNull;

import java.util.UUID;

public class CBAttribute {

    @NonNull
    private final CBUUID mCBUUID;

    CBAttribute(@NonNull String uuid) {
        mCBUUID = new CBUUID(uuid);
    }

    CBAttribute(@NonNull UUID uuid) {
        mCBUUID = new CBUUID(uuid);
    }

    @NonNull
    public CBUUID uuid() {
        return mCBUUID;
    }
}
