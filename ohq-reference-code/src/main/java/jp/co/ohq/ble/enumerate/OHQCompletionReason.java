package jp.co.ohq.ble.enumerate;

public enum OHQCompletionReason {
    Disconnected,
    Canceled,
    PoweredOff,
    Busy,
    InvalidDeviceIdentifier,
    FailedToConnect,
    FailedToTransfer,
    FailedToRegisterUser,
    FailedToAuthenticateUser,
    FailedToDeleteUser,
    FailedToSetUserData,
    OperationNotSupported,
    ConnectionTimedOut,
}
