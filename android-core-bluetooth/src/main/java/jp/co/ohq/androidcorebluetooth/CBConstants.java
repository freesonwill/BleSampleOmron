//
//  CBConstants.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

class CBConstants {

    static final String ACTION_PAIRING_REQUEST;
    static final String EXTRA_PAIRING_VARIANT;
    static final int PAIRING_VARIANT_UNKNOWN = -1;
    // Reference
    //   /frameworks/base/core/java/android/bluetooth/BluetoothDevice.java
    private static final int PAIRING_VARIANT_PIN = 0;
    private static final int PAIRING_VARIANT_PASSKEY = 1;
    private static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    private static final int PAIRING_VARIANT_CONSENT = 3;
    private static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    private static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    private static final int PAIRING_VARIANT_OOB_CONSENT = 6;
    private static final int PAIRING_VARIANT_PIN_16_DIGITS = 7;

    static {
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        } else {
            ACTION_PAIRING_REQUEST = actionPairingRequestStringFromKitkat();
        }
    }

    static {
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
        } else {
            EXTRA_PAIRING_VARIANT = extraPairingVariantStringFromKitkat();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String actionPairingRequestStringFromKitkat() {
        return BluetoothDevice.ACTION_PAIRING_REQUEST;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String extraPairingVariantStringFromKitkat() {
        return BluetoothDevice.EXTRA_PAIRING_VARIANT;
    }

    enum PairingVariant {
        Pin(PAIRING_VARIANT_PIN),
        Passkey(PAIRING_VARIANT_PASSKEY),
        PasskeyConfirmation(PAIRING_VARIANT_PASSKEY_CONFIRMATION),
        Consent(PAIRING_VARIANT_CONSENT),
        DisplayPasskey(PAIRING_VARIANT_DISPLAY_PASSKEY),
        DisplayPin(PAIRING_VARIANT_DISPLAY_PIN),
        OobConsent(PAIRING_VARIANT_OOB_CONSENT),
        Pin16Digits(PAIRING_VARIANT_PIN_16_DIGITS),
        Unknown(PAIRING_VARIANT_UNKNOWN);
        private int value;

        PairingVariant(int value) {
            this.value = value;
        }

        static PairingVariant valueOf(int value) throws IllegalArgumentException {
            for (PairingVariant type : values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return CBConstants.PairingVariant.Unknown;
        }

        int value() {
            return value;
        }
    }
}
