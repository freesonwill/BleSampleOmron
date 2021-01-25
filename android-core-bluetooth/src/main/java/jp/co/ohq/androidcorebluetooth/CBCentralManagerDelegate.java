package jp.co.ohq.androidcorebluetooth;

import android.support.annotation.NonNull;

public interface CBCentralManagerDelegate {
    void didConnect(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral);

    void didDisconnectPeripheral(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral/*, Error error*/);

    void didFailToConnect(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral/*, Error error*/);

    void didDiscover(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull byte[] advertisementData, int rssi);

    void centralManagerDidUpdateState(@NonNull CBCentralManager central, @NonNull CBManagerState newState);

    // for debug
    void onConnectionStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheralState newState);

    // for debug
    void onDetailedStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheralDetailedState newState);

    // for debug
    void onPairingRequest(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral);

    // for debug
    void onBondStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheral.BondState bondState);

    // for debug
    void onAclConnectionStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheral.AclConnectionState aclConnectionState);

    // for debug
    void onGattConnectionStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheral.GattConnectionState gattConnectionState, int status);
}
