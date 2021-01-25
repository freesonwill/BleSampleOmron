package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothGattCharacteristic;

public enum CBCharacteristicWriteType {
    WithResponse(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT),
    WithoutResponse(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    int value;

    CBCharacteristicWriteType(int value) {
        this.value = value;
    }

    int value() {
        return this.value;
    }
}
