//
//  CBCharacteristicProperties.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;

import java.util.EnumSet;

public enum CBCharacteristicProperties {
    Broadcast(BluetoothGattCharacteristic.PROPERTY_BROADCAST),
    Read(BluetoothGattCharacteristic.PERMISSION_READ),
    WriteWithoutResponse(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE),
    Write(BluetoothGattCharacteristic.PROPERTY_WRITE),
    Notify(BluetoothGattCharacteristic.PROPERTY_NOTIFY),
    Indicate(BluetoothGattCharacteristic.PROPERTY_INDICATE),
    AuthenticatedSignedWrite(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE),
    ExtendedProperties(BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS),
    NotifyEncryptionRequired(0x0100),
    IndicateEncryptionRequired(0x0200),;
    private int mValue;

    CBCharacteristicProperties(int value) {
        mValue = value;
    }

    @NonNull
    static EnumSet<CBCharacteristicProperties> valueOf(int bits) {
        EnumSet<CBCharacteristicProperties> ret = EnumSet.noneOf(CBCharacteristicProperties.class);
        for (CBCharacteristicProperties type : values()) {
            if (type.contains(bits)) {
                ret.add(type);
            }
        }
        return ret;
    }

    boolean contains(int bits) {
        return mValue == (bits & mValue);
    }
}
