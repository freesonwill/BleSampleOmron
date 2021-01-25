package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CBService extends CBAttribute {

    @NonNull
    private final CBPeripheral mPeripheral;

    @NonNull
    private final BluetoothGattService mBluetoothGattService;

    CBService(@NonNull CBPeripheral peripheral, @NonNull BluetoothGattService bluetoothGattService) {
        super(bluetoothGattService.getUuid());
        mPeripheral = peripheral;
        mBluetoothGattService = bluetoothGattService;
    }

    @NonNull
    public CBPeripheral peripheral() {
        return mPeripheral;
    }

    public boolean isPrimary() {
        return (mBluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY);
    }

    @NonNull
    public List<CBCharacteristic> characteristics() {
        List<CBCharacteristic> characteristics = new ArrayList<>();
        List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = mBluetoothGattService.getCharacteristics();
        if (null == bluetoothGattCharacteristics) {
            return characteristics;
        }
        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
            characteristics.add(new CBCharacteristic(this, bluetoothGattCharacteristic));
        }
        return characteristics;
    }

    @NonNull
    public List<CBService> includedServices() {
        List<CBService> includedServices = new ArrayList<>();
        List<BluetoothGattService> bluetoothGattIncludedServices = mBluetoothGattService.getIncludedServices();
        if (null == bluetoothGattIncludedServices) {
            return includedServices;
        }
        for (BluetoothGattService bluetoothGattIncludedService : bluetoothGattIncludedServices) {
            includedServices.add(new CBService(mPeripheral, bluetoothGattIncludedService));
        }
        return includedServices;
    }

    @Nullable
    CBCharacteristic characteristic(@NonNull CBUUID uuid) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(uuid.androidUUID());
        if (null == bluetoothGattCharacteristic) {
            return null;
        }
        return new CBCharacteristic(this, bluetoothGattCharacteristic);
    }

    @NonNull
    BluetoothGattService getBluetoothGattService() {
        return mBluetoothGattService;
    }

    @Override
    public String toString() {
        return "CBService{" +
                uuid().uuidString() +
                ", isPrimary=" + isPrimary() +
                ", characteristics=" + characteristics().toString() +
                ", includedServices=" + includedServices().toString() +
                '}';
    }
}
