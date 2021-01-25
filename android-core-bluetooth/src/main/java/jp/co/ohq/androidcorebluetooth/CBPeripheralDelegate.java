package jp.co.ohq.androidcorebluetooth;

import android.support.annotation.NonNull;

public interface CBPeripheralDelegate {
    void didDiscoverServices(@NonNull CBPeripheral peripheral/*, Error error*/);

    void didDiscoverIncludedServicesFor(@NonNull CBPeripheral peripheral, CBService service/*, Error error*/);

    void didDiscoverCharacteristicsFor(@NonNull CBPeripheral peripheral, CBService service/*, Error error*/);

    void didDiscoverDescriptorsFor(@NonNull CBPeripheral peripheral, CBCharacteristic characteristic/*, Error error*/);

    void didUpdateValueFor(@NonNull CBPeripheral peripheral, @NonNull CBCharacteristic characteristic, @NonNull byte[] value, int status/*, Error error*/);

    void didUpdateValueFor(@NonNull CBPeripheral peripheral, @NonNull CBDescriptor descriptor, int status/*, Error error*/);

    void didWriteValueFor(@NonNull CBPeripheral peripheral, @NonNull CBCharacteristic characteristic, int status/*, Error error*/);

    void didWriteValueFor(@NonNull CBPeripheral peripheral, @NonNull CBDescriptor descriptor, int status/*, Error error*/);

    void didUpdateNotificationStateFor(@NonNull CBPeripheral peripheral, @NonNull CBCharacteristic characteristic, int status/*, Error error*/);

    void didReadRSSI(@NonNull CBPeripheral peripheral, int rssi, int status/*, Error error*/);
}
