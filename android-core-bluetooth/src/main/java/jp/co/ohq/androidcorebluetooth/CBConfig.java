//
//  CBConfig.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class CBConfig {

    static final int RETRY_UNLIMITED = 0;
    private static final CreateBondOption DEF_CREATE_BOND_OPTION = CreateBondOption.UsedBeforeGattConnection;
    private static final RemoveBondOption DEF_REMOVE_BOND_OPTION = RemoveBondOption.NotUse;
    private static final boolean DEF_ASSIST_PAIRING_DIALOG_ENABLED = false;
    private static final boolean DEF_AUTO_PAIRING_ENABLED = false;
    private static final boolean DEF_AUTO_ENTER_THE_PIN_CODE_ENABLED = false;
    private static final String DEF_PIN_CODE = "000000";
    private static final boolean DEF_STABLE_CONNECTION_ENABLED = true;
    private static final long DEF_STABLE_CONNECTION_WAIT_TIME = 1500;
    private static final boolean DEF_CONNECTION_RETRY_ENABLED = true;
    private static final long DEF_CONNECTION_RETRY_DELAY_TIME = 1000;
    private static final int DEF_CONNECTION_RETRY_COUNT = RETRY_UNLIMITED;
    private static final boolean DEF_USE_REFRESH_WHEN_DISCONNECT = true;
    private CreateBondOption mCreateBondOption = DEF_CREATE_BOND_OPTION;
    private RemoveBondOption mRemoveBondOption = DEF_REMOVE_BOND_OPTION;
    private boolean mAssistPairingDialogEnabled = DEF_ASSIST_PAIRING_DIALOG_ENABLED;
    private boolean mAutoPairingEnabled = DEF_AUTO_PAIRING_ENABLED;
    private boolean mAutoEnterThePinCodeEnabled = DEF_AUTO_ENTER_THE_PIN_CODE_ENABLED;
    @NonNull
    private String mPinCode = DEF_PIN_CODE;
    private boolean mStableConnectionEnabled = DEF_STABLE_CONNECTION_ENABLED;
    private long mStableConnectionWaitTime = DEF_STABLE_CONNECTION_WAIT_TIME;
    private boolean mConnectionRetryEnabled = DEF_CONNECTION_RETRY_ENABLED;
    private long mConnectionRetryDelayTime = DEF_CONNECTION_RETRY_DELAY_TIME;
    private int mConnectionRetryCount = DEF_CONNECTION_RETRY_COUNT;
    private boolean mUseRefreshWhenDisconnect = DEF_USE_REFRESH_WHEN_DISCONNECT;

    @NonNull
    public Bundle getDefault(@Nullable List<Key> keys) {
        if (null == keys) {
            keys = Arrays.asList(Key.values());
        }
        final Bundle bundle = new Bundle();
        for (Key key : keys) {
            if (bundle.containsKey(Key.CreateBondOption.name())) {
                bundle.putSerializable(Key.CreateBondOption.name(), DEF_CREATE_BOND_OPTION);
            }
            if (bundle.containsKey(Key.RemoveBondOption.name())) {
                bundle.putSerializable(Key.CreateBondOption.name(), DEF_REMOVE_BOND_OPTION);
            }
            if (Key.AssistPairingDialogEnabled.equals(key)) {
                bundle.putBoolean(Key.AssistPairingDialogEnabled.name(), DEF_ASSIST_PAIRING_DIALOG_ENABLED);
            }
            if (Key.AutoPairingEnabled.equals(key)) {
                bundle.putBoolean(Key.AutoPairingEnabled.name(), DEF_AUTO_PAIRING_ENABLED);
            }
            if (Key.AutoEnterThePinCodeEnabled.equals(key)) {
                bundle.putBoolean(Key.AutoEnterThePinCodeEnabled.name(), DEF_AUTO_ENTER_THE_PIN_CODE_ENABLED);
            }
            if (Key.PinCode.equals(key)) {
                bundle.putString(Key.PinCode.name(), DEF_PIN_CODE);
            }
            if (Key.StableConnectionEnabled.equals(key)) {
                bundle.putBoolean(Key.StableConnectionEnabled.name(), DEF_STABLE_CONNECTION_ENABLED);
            }
            if (Key.StableConnectionWaitTime.equals(key)) {
                bundle.putLong(Key.StableConnectionWaitTime.name(), DEF_STABLE_CONNECTION_WAIT_TIME);
            }
            if (Key.ConnectionRetryEnabled.equals(key)) {
                bundle.putBoolean(Key.ConnectionRetryEnabled.name(), DEF_CONNECTION_RETRY_ENABLED);
            }
            if (Key.ConnectionRetryDelayTime.equals(key)) {
                bundle.putLong(Key.ConnectionRetryDelayTime.name(), DEF_CONNECTION_RETRY_DELAY_TIME);
            }
            if (Key.ConnectionRetryCount.equals(key)) {
                bundle.putInt(Key.ConnectionRetryCount.name(), DEF_CONNECTION_RETRY_COUNT);
            }
            if (Key.UseRefreshWhenDisconnect.equals(key)) {
                bundle.putBoolean(Key.UseRefreshWhenDisconnect.name(), DEF_USE_REFRESH_WHEN_DISCONNECT);
            }
        }
        return bundle;
    }

    @NonNull
    public Bundle get(@Nullable List<Key> keys) {
        if (null == keys) {
            keys = Arrays.asList(Key.values());
        }
        final Bundle bundle = new Bundle();
        for (Key key : keys) {
            if (bundle.containsKey(Key.CreateBondOption.name())) {
                bundle.putSerializable(Key.CreateBondOption.name(), mCreateBondOption);
            }
            if (bundle.containsKey(Key.RemoveBondOption.name())) {
                bundle.putSerializable(Key.CreateBondOption.name(), mRemoveBondOption);
            }
            if (Key.AssistPairingDialogEnabled.equals(key)) {
                bundle.putBoolean(Key.AssistPairingDialogEnabled.name(), mAssistPairingDialogEnabled);
            }
            if (Key.AutoPairingEnabled.equals(key)) {
                bundle.putBoolean(Key.AutoPairingEnabled.name(), mAutoPairingEnabled);
            }
            if (Key.AutoEnterThePinCodeEnabled.equals(key)) {
                bundle.putBoolean(Key.AutoEnterThePinCodeEnabled.name(), mAutoEnterThePinCodeEnabled);
            }
            if (Key.PinCode.equals(key)) {
                bundle.putString(Key.PinCode.name(), mPinCode);
            }
            if (Key.StableConnectionEnabled.equals(key)) {
                bundle.putBoolean(Key.StableConnectionEnabled.name(), mStableConnectionEnabled);
            }
            if (Key.StableConnectionWaitTime.equals(key)) {
                bundle.putLong(Key.StableConnectionWaitTime.name(), mStableConnectionWaitTime);
            }
            if (Key.ConnectionRetryEnabled.equals(key)) {
                bundle.putBoolean(Key.ConnectionRetryEnabled.name(), mConnectionRetryEnabled);
            }
            if (Key.ConnectionRetryDelayTime.equals(key)) {
                bundle.putLong(Key.ConnectionRetryDelayTime.name(), mConnectionRetryDelayTime);
            }
            if (Key.ConnectionRetryCount.equals(key)) {
                bundle.putInt(Key.ConnectionRetryCount.name(), mConnectionRetryCount);
            }
            if (Key.UseRefreshWhenDisconnect.equals(key)) {
                bundle.putBoolean(Key.UseRefreshWhenDisconnect.name(), mUseRefreshWhenDisconnect);
            }
        }
        return bundle;
    }

    void set(@NonNull Bundle bundle) {
        if (bundle.containsKey(Key.CreateBondOption.name())) {
            mCreateBondOption = (CreateBondOption) bundle.getSerializable(Key.CreateBondOption.name());
        }
        if (bundle.containsKey(Key.RemoveBondOption.name())) {
            mRemoveBondOption = (RemoveBondOption) bundle.getSerializable(Key.RemoveBondOption.name());
        }
        if (bundle.containsKey(Key.AssistPairingDialogEnabled.name())) {
            mAssistPairingDialogEnabled = bundle.getBoolean(Key.AssistPairingDialogEnabled.name());
        }
        if (bundle.containsKey(Key.AutoPairingEnabled.name())) {
            mAutoPairingEnabled = bundle.getBoolean(Key.AutoPairingEnabled.name());
        }
        if (bundle.containsKey(Key.AutoEnterThePinCodeEnabled.name())) {
            mAutoEnterThePinCodeEnabled = bundle.getBoolean(Key.AutoEnterThePinCodeEnabled.name());
        }
        if (bundle.containsKey(Key.PinCode.name())) {
            mPinCode = bundle.getString(Key.PinCode.name(), DEF_PIN_CODE);
        }
        if (bundle.containsKey(Key.StableConnectionEnabled.name())) {
            mStableConnectionEnabled = bundle.getBoolean(Key.StableConnectionEnabled.name());
        }
        if (bundle.containsKey(Key.StableConnectionWaitTime.name())) {
            mStableConnectionWaitTime = bundle.getLong(Key.StableConnectionWaitTime.name());
        }
        if (bundle.containsKey(Key.ConnectionRetryEnabled.name())) {
            mConnectionRetryEnabled = bundle.getBoolean(Key.ConnectionRetryEnabled.name());
        }
        if (bundle.containsKey(Key.ConnectionRetryDelayTime.name())) {
            mConnectionRetryDelayTime = bundle.getLong(Key.ConnectionRetryDelayTime.name());
        }
        if (bundle.containsKey(Key.ConnectionRetryCount.name())) {
            mConnectionRetryCount = bundle.getInt(Key.ConnectionRetryCount.name());
        }
        if (bundle.containsKey(Key.UseRefreshWhenDisconnect.name())) {
            mUseRefreshWhenDisconnect = bundle.getBoolean(Key.UseRefreshWhenDisconnect.name());
        }
    }

    CreateBondOption getCreateBondOption() {
        return mCreateBondOption;
    }

    RemoveBondOption getRemoveBondOption() {
        return mRemoveBondOption;
    }

    boolean isAssistPairingDialogEnabled() {
        return mAssistPairingDialogEnabled;
    }

    boolean isAutoPairingEnabled() {
        return mAutoPairingEnabled;
    }

    boolean isAutoEnterThePinCodeEnabled() {
        return mAutoEnterThePinCodeEnabled;
    }

    @NonNull
    String getPinCode() {
        return mPinCode;
    }

    boolean isStableConnectionEnabled() {
        return mStableConnectionEnabled;
    }

    long getStableConnectionWaitTime() {
        return mStableConnectionWaitTime;
    }

    boolean isConnectionRetryEnabled() {
        return mConnectionRetryEnabled;
    }

    long getConnectionRetryDelayTime() {
        return mConnectionRetryDelayTime;
    }

    int getConnectionRetryCount() {
        return mConnectionRetryCount;
    }

    boolean isUseRefreshWhenDisconnect() {
        return mUseRefreshWhenDisconnect;
    }

    public enum CreateBondOption {
        NotUse,
        UsedBeforeGattConnection,
        UsedAfterServicesDiscovered
    }

    public enum RemoveBondOption {
        NotUse,
        UsedBeforeConnectionProcessEveryTime
    }

    public enum Key {
        CreateBondOption,
        RemoveBondOption,
        AssistPairingDialogEnabled,
        AutoPairingEnabled,
        AutoEnterThePinCodeEnabled,
        PinCode,
        StableConnectionEnabled,
        StableConnectionWaitTime,
        ConnectionRetryEnabled,
        ConnectionRetryDelayTime,
        ConnectionRetryCount,
        UseRefreshWhenDisconnect,
    }
}
