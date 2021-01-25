package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;

public class CBDescriptor extends CBAttribute {

    public static final String CBUUIDCharacteristicExtendedPropertiesString = "2900";
    public static final String CBUUIDCharacteristicUserDescriptionString = "2901";
    public static final String CBUUIDClientCharacteristicConfigurationString = "2902";
    public static final String CBUUIDServerCharacteristicConfigurationString = "2903";
    public static final String CBUUIDCharacteristicFormatString = "2904";
    public static final String CBUUIDCharacteristicAggregateFormatString = "2905";

    @NonNull
    private final CBCharacteristic mCharacteristic;
    @NonNull
    private final BluetoothGattDescriptor mBluetoothGattDescriptor;

    CBDescriptor(@NonNull CBCharacteristic characteristic, @NonNull BluetoothGattDescriptor bluetoothGattDescriptor) {
        super(bluetoothGattDescriptor.getUuid());
        mCharacteristic = characteristic;
        mBluetoothGattDescriptor = bluetoothGattDescriptor;
    }

    @NonNull
    public CBCharacteristic characteristic() {
        return mCharacteristic;
    }

    @NonNull
    public byte[] value() {
        return mBluetoothGattDescriptor.getValue();
    }

    @NonNull
    BluetoothGattDescriptor getBluetoothGattDescriptor() {
        return mBluetoothGattDescriptor;
    }

    @Override
    public String toString() {
        return "CBDescriptor{" +
                uuid().toString() +
                '}';
    }
}
