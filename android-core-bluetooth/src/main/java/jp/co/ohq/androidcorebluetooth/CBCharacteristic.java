package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CBCharacteristic extends CBAttribute {

    @NonNull
    private final CBService mService;
    @NonNull
    private final BluetoothGattCharacteristic mBluetoothGattCharacteristic;

    CBCharacteristic(@NonNull CBService service, @NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        super(bluetoothGattCharacteristic.getUuid());
        mService = service;
        mBluetoothGattCharacteristic = bluetoothGattCharacteristic;
    }

    @NonNull
    public CBService service() {
        return mService;
    }

    @NonNull
    public byte[] value() {
        return mBluetoothGattCharacteristic.getValue();
    }

    @NonNull
    public String stringValue() {
        return mBluetoothGattCharacteristic.getStringValue(0);
    }

    @NonNull
    public List<CBDescriptor> descriptors() {
        List<CBDescriptor> descriptors = new ArrayList<>();
        List<BluetoothGattDescriptor> bluetoothGattDescriptors = mBluetoothGattCharacteristic.getDescriptors();
        if (null == bluetoothGattDescriptors) {
            return descriptors;
        }
        for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattDescriptors) {
            descriptors.add(new CBDescriptor(this, bluetoothGattDescriptor));
        }
        return descriptors;
    }

    @NonNull
    public EnumSet<CBCharacteristicProperties> properties() {
        return CBCharacteristicProperties.valueOf(mBluetoothGattCharacteristic.getProperties());
    }

    public boolean isNotifying() {
        int bluetoothGattCharacteristicProperties = mBluetoothGattCharacteristic.getProperties();
        return (CBCharacteristicProperties.Notify.contains(bluetoothGattCharacteristicProperties) ||
                CBCharacteristicProperties.Indicate.contains(bluetoothGattCharacteristicProperties));
    }

    @Nullable
    CBDescriptor descriptor(@NonNull CBUUID uuid) {
        BluetoothGattDescriptor bluetoothGattDescriptor = mBluetoothGattCharacteristic.getDescriptor(uuid.androidUUID());
        if (null == bluetoothGattDescriptor) {
            return null;
        }
        return new CBDescriptor(this, bluetoothGattDescriptor);
    }

    @NonNull
    BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return mBluetoothGattCharacteristic;
    }

    @Override
    public String toString() {
        return "CBCharacteristic{" +
                uuid().toString() +
                ", properties=" + properties().toString() +
                ", isNotifying=" + isNotifying() +
                ", descriptors=" + descriptors().toString() +
                '}';
    }
}
