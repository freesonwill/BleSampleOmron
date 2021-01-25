package jp.co.ohq.androidcorebluetooth;

public enum CBPeripheralDetailedState {
    Unconnected,
    ConnectStarting,
    PairRemoving,
    Pairing,
    GattConnecting,
    ServiceDiscovering,
    ConnectCanceling,
    CleanupConnection,
    ConnectionRetryReady,
    ConnectCanceled,
    ConnectionFailed,
    Connected,
    Disconnecting,
    Disconnected,
}
