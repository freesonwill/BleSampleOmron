//
//  CBStatusCode.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothGatt;

public class CBStatusCode {
    // Reference
    //   /external/bluetooth/bluedroid/stack/include/gatt_api.h (Android 4.x/5.x)
    //   /system/bt/stack/include/gatt_api.h (Android 6.x)
    public static final int GATT_SUCCESS = BluetoothGatt.GATT_SUCCESS;
    public static final int GATT_INVALID_HANDLE = 0x01;
    public static final int GATT_READ_NOT_PERMIT = 0x02;
    public static final int GATT_WRITE_NOT_PERMIT = 0x03;
    public static final int GATT_INVALID_PDU = 0x04;
    public static final int GATT_INSUF_AUTHENTICATION = 0x05;
    public static final int GATT_REQ_NOT_SUPPORTED = 0x06;
    public static final int GATT_INVALID_OFFSET = 0x07;
    public static final int GATT_INSUF_AUTHORIZATION = 0x08;
    public static final int GATT_PREPARE_Q_FULL = 0x09;
    public static final int GATT_NOT_FOUND = 0x0a;
    public static final int GATT_NOT_LONG = 0x0b;
    public static final int GATT_INSUF_KEY_SIZE = 0x0c;
    public static final int GATT_INVALID_ATTR_LEN = 0x0d;
    public static final int GATT_ERR_UNLIKELY = 0x0e;
    public static final int GATT_INSUF_ENCRYPTION = 0x0f;
    public static final int GATT_UNSUPPORT_GRP_TYPE = 0x10;
    public static final int GATT_INSUF_RESOURCE = 0x11;
    public static final int GATT_NO_RESOURCES = 0x80;
    public static final int GATT_INTERNAL_ERROR = 0x81;
    public static final int GATT_WRONG_STATE = 0x82;
    public static final int GATT_DB_FULL = 0x83;
    public static final int GATT_BUSY = 0x84;
    public static final int GATT_ERROR = 0x85;
    public static final int GATT_CMD_STARTED = 0x86;
    public static final int GATT_ILLEGAL_PARAMETER = 0x87;
    public static final int GATT_PENDING = 0x88;
    public static final int GATT_AUTH_FAIL = 0x89;
    public static final int GATT_MORE = 0x8a;
    public static final int GATT_INVALID_CFG = 0x8b;
    public static final int GATT_SERVICE_STARTED = 0x8c;
    public static final int GATT_ENCRYPED_MITM = GATT_SUCCESS;
    public static final int GATT_ENCRYPED_NO_MITM = 0x8d;
    public static final int GATT_NOT_ENCRYPTED = 0x8e;
    public static final int GATT_CONGESTED = 0x8f;
    public static final int GATT_UNKNOWN = 0x101;
}
