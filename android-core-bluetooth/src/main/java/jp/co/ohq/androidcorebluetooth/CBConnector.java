//
//  CBPeripheral.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.List;

import jp.co.ohq.utility.SynchronizeCallback;
import jp.co.ohq.utility.sm.State;
import jp.co.ohq.utility.sm.StateMachine;

class CBConnector extends StateMachine {

    private static final EnumMap<CBPeripheralDetailedState, CBPeripheralState> sStateMap = new EnumMap<>(CBPeripheralDetailedState.class);
    private static final int EVT_BASE = 0x10000000;
    static final int EVT_CONNECT = EVT_BASE + 0x0002;
    static final int EVT_CANCEL_PERIPHERAL_CONNECTION = EVT_BASE + 0x0003;
    static final int EVT_PAIRING_REQUEST = EVT_BASE + 0x1001;
    static final int EVT_BONDED = EVT_BASE + 0x1002;
    static final int EVT_BONDING = EVT_BASE + 0x1003;
    static final int EVT_BOND_NONE = EVT_BASE + 0x1004;
    static final int EVT_ACL_CONNECTED = EVT_BASE + 0x1005;
    static final int EVT_ACL_DISCONNECTED = EVT_BASE + 0x1006;
    static final int EVT_GATT_CONNECTED = EVT_BASE + 0x1007;
    static final int EVT_GATT_DISCONNECTED = EVT_BASE + 0x1008;
    static final int EVT_DISCOVER_SERVICE_SUCCESS = EVT_BASE + 0x1009;
    static final int EVT_DISCOVER_SERVICE_FAILURE = EVT_BASE + 0x100a;
    private static final int LOCAL_EVT_BASE = 0xf0000000;

    static {
        sStateMap.put(CBPeripheralDetailedState.Unconnected, CBPeripheralState.Disconnected);
        sStateMap.put(CBPeripheralDetailedState.ConnectStarting, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.PairRemoving, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.Pairing, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.GattConnecting, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.ServiceDiscovering, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.ConnectCanceling, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.CleanupConnection, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.ConnectionRetryReady, CBPeripheralState.Connecting);
        sStateMap.put(CBPeripheralDetailedState.ConnectCanceled, CBPeripheralState.Disconnected);
        sStateMap.put(CBPeripheralDetailedState.ConnectionFailed, CBPeripheralState.Disconnected);
        sStateMap.put(CBPeripheralDetailedState.Connected, CBPeripheralState.Connected);
        sStateMap.put(CBPeripheralDetailedState.Disconnecting, CBPeripheralState.Disconnecting);
        sStateMap.put(CBPeripheralDetailedState.Disconnected, CBPeripheralState.Disconnected);
    }

    private final State mDefaultState = new DefaultState();
    private final State mUnconnectedState = new UnconnectedState();
    private final State mConnectingState = new ConnectingState();
    private final State mConnectedState = new ConnectedState();
    private final State mDisconnectingState = new DisconnectingState();
    private final State mConnectStartingState = new ConnectStartingState();
    private final State mPairRemovingState = new PairRemovingState();
    private final State mPairingState = new PairingState();
    private final State mGattConnectingState = new GattConnectingState();
    private final State mServiceDiscoveringState = new ServiceDiscoveringState();
    private final State mConnectCancelingState = new ConnectCancelingState();
    private final State mCleanupConnectionState = new CleanupConnectionState();
    private final State mConnectionRetryReadyState = new ConnectionRetryReadyState();
    private final State mConnectCanceledState = new ConnectCanceledState();
    private final State mDisconnectedState = new DisconnectedState();
    private final State mConnectFailedState = new ConnectionFailedState();
    @NonNull
    private final WeakReference<CBPeripheral> mPeripheralRef;
    @NonNull
    private final Callback mCallback;
    @NonNull
    private final CBConfig mConfig;
    private int mConnectionRetryCount;
    private boolean mIsShowPairingDialog;
    @NonNull
    private CBPeripheralState mState;
    @NonNull
    private CBPeripheralDetailedState mDetailedState;

    CBConnector(
            @NonNull CBPeripheral peripheral,
            @NonNull Callback callback,
            @NonNull Looper looper) {
        super(CBConnector.class.getSimpleName(), looper);

        mPeripheralRef = new WeakReference<>(peripheral);
        mCallback = callback;
        mConfig = new CBConfig();

        addState(mDefaultState);
        addState(mUnconnectedState, mDefaultState);
        addState(mConnectingState, mDefaultState);
        addState(mConnectedState, mDefaultState);
        addState(mDisconnectingState, mDefaultState);
        addState(mConnectStartingState, mConnectingState);
        addState(mPairRemovingState, mConnectingState);
        addState(mPairingState, mConnectingState);
        addState(mGattConnectingState, mConnectingState);
        addState(mServiceDiscoveringState, mConnectingState);

        addState(mConnectCancelingState, mConnectingState);
        addState(mCleanupConnectionState, mConnectingState);
        addState(mConnectionRetryReadyState, mConnectingState);

        addState(mConnectCanceledState, mUnconnectedState);
        addState(mConnectFailedState, mUnconnectedState);
        addState(mDisconnectedState, mUnconnectedState);

        mState = CBPeripheralState.Disconnected;
        mDetailedState = CBPeripheralDetailedState.Unconnected;
        setInitialState(mUnconnectedState);

        setTag(CBLog.TAG);
        setDbg(CBLog.OUTPUT_LOG_ENABLED);
        start();
    }

    boolean isConnected() {
        return CBPeripheralState.Connected == getState();
    }

    CBPeripheralState getState() {
        final CBPeripheralState state;
        if (getHandler().isCurrentThread()) {
            state = mState;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mState);
                    callback.unlock();
                }
            });
            callback.lock();
            state = (CBPeripheralState) callback.getResult();
        }
        return state;
    }

    CBPeripheralDetailedState getDetailedState() {
        final CBPeripheralDetailedState state;
        if (getHandler().isCurrentThread()) {
            state = mDetailedState;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mDetailedState);
                    callback.unlock();
                }
            });
            callback.lock();
            state = (CBPeripheralDetailedState) callback.getResult();
        }
        return state;
    }

    @NonNull
    public Bundle getConfig(@Nullable final List<CBConfig.Key> keys) {
        final Bundle config;
        if (getHandler().isCurrentThread()) {
            config = mConfig.get(keys);
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mConfig.get(keys));
                    callback.unlock();
                }
            });
            callback.lock();
            config = (Bundle) callback.getResult();
        }
        return config;
    }

    public void setConfig(@NonNull final Bundle config) {
        if (getHandler().isCurrentThread()) {
            mConfig.set(config);
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mConfig.set(config);
                }
            });
        }
    }

    private void _assistPairingDialogIfNeeded() {
        if (!mConfig.isAssistPairingDialogEnabled()) {
            return;
        }
        // Show pairing dialog mandatorily.
        // The app calls start discovery and cancel interface so that app will show pairing dialog each time
        // based on specification that Android O/S shows the dialog when the app pairs with a device
        // within 60 seconds after cancel discovery.
        BluetoothManager bluetoothManager = (BluetoothManager) mPeripheralRef.get().getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (null != bluetoothManager) {
            bluetoothManager.getAdapter().startDiscovery();
            bluetoothManager.getAdapter().cancelDiscovery();
        }
    }

    private void _autoPairingIfNeeded(CBConstants.PairingVariant variant) {
        switch (variant) {
            case Pin:
            case Pin16Digits:
                if (mConfig.isAutoEnterThePinCodeEnabled() && !mConfig.getPinCode().isEmpty()) {
                    mPeripheralRef.get().setPin(mConfig.getPinCode());
                }
                break;
            case Passkey:
                if (mConfig.isAutoEnterThePinCodeEnabled() && !mConfig.getPinCode().isEmpty()) {
                    mPeripheralRef.get().setPasskey(mConfig.getPinCode());
                }
                break;
            case PasskeyConfirmation:
                if (mConfig.isAutoPairingEnabled()) {
                    mPeripheralRef.get().setPairingConfirmation(true);
                }
                break;
            case Consent:
                if (mConfig.isAutoPairingEnabled()) {
                    mPeripheralRef.get().setPairingConfirmation(true);
                }
                break;
            case DisplayPasskey:
                break;
            case DisplayPin:
                break;
            case OobConsent:
                break;
            default:
                break;
        }
    }

    private void _setState(@NonNull CBPeripheralState state) {
        if (mState == state) {
            return;
        }
        mState = state;
        mCallback.onStateChanged(state);
    }

    private void _setDetailedState(final @NonNull CBPeripheralDetailedState detailedState) {
        if (mDetailedState == detailedState) {
            return;
        }
        _setState(sStateMap.get(detailedState));
        mDetailedState = detailedState;
        mCallback.onDetailedStateChanged(detailedState);
    }

    interface Callback {

        void didConnect();

        void didFailToConnect();

        void didDisconnectPeripheral();

        void onStateChanged(@NonNull CBPeripheralState newState);

        void onDetailedStateChanged(@NonNull CBPeripheralDetailedState newState);
    }

    private class DefaultState extends State {
        @Override
        public boolean processMessage(@NonNull Message msg) {
            return StateMachine.HANDLED;
        }
    }

    private class UnconnectedState extends State {
        public void enter(Object[] transferObjects) {
            if (mPeripheralRef.get().hasGatt()) {
                if (mConfig.isUseRefreshWhenDisconnect()) {
                    mPeripheralRef.get().refreshGatt();
                }
                mPeripheralRef.get().closeGatt();
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_CONNECT:
                    transitionTo(mConnectStartingState);
                    break;
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }
    }

    private class ConnectingState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            mConnectionRetryCount = 0;
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_CANCEL_PERIPHERAL_CONNECTION:
                    transitionTo(mConnectCancelingState);
                    break;
                case EVT_BOND_NONE:
                case EVT_BONDING:
                case EVT_GATT_DISCONNECTED:
                    transitionToCleanupState();
                    break;
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        private void transitionToCleanupState() {
            if (mIsShowPairingDialog) {
                // No retry when connection failed in showing pairing dialog.
                // ex) Select [Cancel] / Invalid PIN input
                CBLog.w("Pairing canceled or timeout or invalid PIN input.");
                Object[] transferObjects = {CleanupConnectionState.NOT_RETRY};
                transitionTo(mCleanupConnectionState, transferObjects);
            } else {
                // Retry when unexpected connection failed.
                CBLog.e("Connection failed.");
                Object[] transferObjects = {CleanupConnectionState.RETRY};
                transitionTo(mCleanupConnectionState, transferObjects);
            }
        }
    }

    private class ConnectedState extends State {

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.Connected);
            mCallback.didConnect();
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_CONNECT:
                    mCallback.didFailToConnect();
                    break;
                case EVT_CANCEL_PERIPHERAL_CONNECTION:
                    transitionTo(mDisconnectingState);
                    break;
                case EVT_BOND_NONE:
                case EVT_GATT_DISCONNECTED:
                    transitionTo(mDisconnectingState);
                    break;
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }
    }

    private class DisconnectingState extends State {

        private static final int EVT_DISCONNECTION_TIMEOUT = LOCAL_EVT_BASE + 0x0001;
        private static final long DISCONNECTION_WAIT_TIME = 15 * 1000;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.Disconnecting);
            teardownOrTransitionToNextState();
            sendMessageDelayed(EVT_DISCONNECTION_TIMEOUT, DISCONNECTION_WAIT_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_CONNECT:
                    mCallback.didFailToConnect();
                    break;
                case EVT_BOND_NONE:
                case EVT_GATT_DISCONNECTED:
                    teardownOrTransitionToNextState();
                    break;
                case EVT_DISCONNECTION_TIMEOUT:
                    CBLog.e("Disconnection timeout.");
                    // There are cases when timeout has occurred without notification of
                    // ACL Disconnected or Bond None and move to next state in theses cases.
                    transitionTo(mDisconnectedState);
                    break;
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_DISCONNECTION_TIMEOUT);
        }

        private boolean isTeardownCompleted() {
            if (mPeripheralRef.get().isGattConnected()) {
                CBLog.i("Gatt disconnecting.");
                return false;
            }
            if (CBPeripheral.BondState.Bonding == mPeripheralRef.get().getBondState()) {
                CBLog.i("Bond process canceling.");
                return false;
            }
            CBLog.i("Teardown completed.");
            return true;
        }

        private void teardownOrTransitionToNextState() {
            if (isTeardownCompleted()) {
                transitionTo(mDisconnectedState);
            } else {
                if (mPeripheralRef.get().isGattConnected()) {
                    mPeripheralRef.get().disconnectGatt();
                } else if (CBPeripheral.BondState.Bonding == mPeripheralRef.get().getBondState()) {
                    mPeripheralRef.get().cancelBondProcess();
                }
            }
        }
    }

    private class ConnectStartingState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.ConnectStarting);
            mIsShowPairingDialog = false;
            if (CBConfig.RemoveBondOption.UsedBeforeConnectionProcessEveryTime == mConfig.getRemoveBondOption()
                    && mPeripheralRef.get().isBonded()) {
                transitionTo(mPairRemovingState);
            } else if (CBConfig.CreateBondOption.UsedBeforeGattConnection == mConfig.getCreateBondOption()
                    && !mPeripheralRef.get().isBonded()) {
                transitionTo(mPairingState);
            } else {
                transitionTo(mGattConnectingState);
            }
        }
    }

    private class PairRemovingState extends State {

        private static final int EVT_TIMEOUT = LOCAL_EVT_BASE + 0x0001;

        private static final long TIMEOUT_SEC = 5 * 1000;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.PairRemoving);
            if (!mPeripheralRef.get().removeBond()) {
                Object[] objects = {CleanupConnectionState.RETRY};
                transitionTo(mCleanupConnectionState, objects);
                return;
            }
            sendMessageDelayed(EVT_TIMEOUT, TIMEOUT_SEC);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_BOND_NONE:
                    removeMessages(EVT_TIMEOUT);
                    CBLog.i("Pair removed.");
                    if (CBConfig.CreateBondOption.UsedBeforeGattConnection == mConfig.getCreateBondOption()
                            && !mPeripheralRef.get().isBonded()) {
                        transitionTo(mPairingState);
                    } else {
                        transitionTo(mGattConnectingState);
                    }
                    break;
                case EVT_TIMEOUT: {
                    CBLog.e("timeout.");
                    Object[] objects = {CleanupConnectionState.RETRY};
                    transitionTo(mCleanupConnectionState, objects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_TIMEOUT);
        }
    }

    private class PairingState extends State {

        private static final int EVT_PAIRING_TIMEOUT = LOCAL_EVT_BASE + 0x0001;

        private static final long PAIRING_WAIT_TIME = 15 * 1000;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.Pairing);
            _assistPairingDialogIfNeeded();
            if (!mPeripheralRef.get().createBond()) {
                Object[] objects = {CleanupConnectionState.RETRY};
                transitionTo(mCleanupConnectionState, objects);
                return;
            }
            sendMessageDelayed(EVT_PAIRING_TIMEOUT, PAIRING_WAIT_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_PAIRING_REQUEST:
                    removeMessages(EVT_PAIRING_TIMEOUT);
                    mIsShowPairingDialog = true;
                    _autoPairingIfNeeded((CBConstants.PairingVariant) msg.obj);
                    break;
                case EVT_BONDING:
                    break;
                case EVT_BONDED:
                    removeMessages(EVT_PAIRING_TIMEOUT);
                    mIsShowPairingDialog = false;
                    transitionToNextStateIfPaired();
                    break;
                case EVT_PAIRING_TIMEOUT: {
                    CBLog.e("Pairing timeout.");
                    Object[] objects = {CleanupConnectionState.RETRY};
                    transitionTo(mCleanupConnectionState, objects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_PAIRING_TIMEOUT);
        }

        private void transitionToNextStateIfPaired() {
            if (!mPeripheralRef.get().isBonded()) {
                CBLog.i("Wait bonded.");
                return;
            }
            CBLog.i("Pairing completed.");
            if (!mPeripheralRef.get().isGattConnected()) {
                transitionTo(mGattConnectingState);
            } else {
                transitionTo(mConnectedState);
            }
        }
    }

    private class GattConnectingState extends State {

        private static final int EVT_GATT_CONNECTION_TIMEOUT = LOCAL_EVT_BASE + 0x0001;
        private static final int EVT_CONNECTION_STABLED = LOCAL_EVT_BASE + 0x0002;

        private static final long GATT_CONNECTION_WAIT_TIME = 15 * 1000;

        private boolean mNotBeenPairing;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.GattConnecting);
            mNotBeenPairing = false;
            _assistPairingDialogIfNeeded();
            if (!mPeripheralRef.get().connectGatt()) {
                Object[] objects = {CleanupConnectionState.RETRY};
                transitionTo(mCleanupConnectionState, objects);
                return;
            }
            sendMessageDelayed(EVT_GATT_CONNECTION_TIMEOUT, GATT_CONNECTION_WAIT_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_PAIRING_REQUEST:
                    removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
                    mIsShowPairingDialog = true;
                    _autoPairingIfNeeded((CBConstants.PairingVariant) msg.obj);
                    break;
                case EVT_GATT_CONNECTED:
                    removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
                    long stableConnectionWaitTime = 0;
                    if (mConfig.isStableConnectionEnabled()) {
                        stableConnectionWaitTime = mConfig.getStableConnectionWaitTime();
                    }
                    sendMessageDelayed(EVT_CONNECTION_STABLED, stableConnectionWaitTime);
                    break;
                case EVT_BONDING:
                    break;
                case EVT_BONDED:
                    mIsShowPairingDialog = false;
                    transitionToNextStateIfConnectionStabled();
                    break;
                case EVT_CONNECTION_STABLED:
                    if (CBPeripheral.BondState.None == mPeripheralRef.get().getBondState()) {
                        // Target device does not pair by connectGatt() if pairing function
                        // is not run after GATT connect within defined time.
                        mNotBeenPairing = true;
                        CBLog.i("Not been pairing in the connection process.");
                    }
                    transitionToNextStateIfConnectionStabled();
                    break;
                case EVT_GATT_CONNECTION_TIMEOUT: {
                    CBLog.e("Gatt connection timeout.");
                    Object[] objects = {CleanupConnectionState.RETRY};
                    transitionTo(mCleanupConnectionState, objects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
            removeMessages(EVT_CONNECTION_STABLED);
        }

        private void transitionToNextStateIfConnectionStabled() {
            if (!mPeripheralRef.get().isGattConnected()) {
                CBLog.i("Gatt connecting.");
                return;
            }
            if (hasMessage(EVT_CONNECTION_STABLED)) {
                CBLog.i("Wait connection stabled.");
                return;
            }
            if (!mNotBeenPairing && !mPeripheralRef.get().isBonded()) {
                CBLog.i("Wait bonded.");
                return;
            }
            CBLog.i("Gatt connection completed.");
            transitionTo(mServiceDiscoveringState);
        }
    }

    private class ServiceDiscoveringState extends State {

        private static final int EVT_START = LOCAL_EVT_BASE + 0x0001;
        private static final int EVT_EXEC = LOCAL_EVT_BASE + 0x0002;
        private static final int EVT_TIMEOUT = LOCAL_EVT_BASE + 0x0003;
        private static final int EVT_VERIFY_FAILED = LOCAL_EVT_BASE + 0x0004;

        private static final int RETRY_FOR_VERIFY_FAILED_MAX = 2;

        private static final long TIMEOUT_MS = 30 * 1000;
        private static final long EXEC_INTERVAL = 5 * 1000;

        private int mRetryForVerifyFailedCount;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.ServiceDiscovering);
            mRetryForVerifyFailedCount = 0;
            List<BluetoothGattService> services = mPeripheralRef.get().getServices();
            if (0 == services.size()) {
                sendMessage(EVT_START);
            } else {
                sendMessage(EVT_DISCOVER_SERVICE_SUCCESS);
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_START:
                    sendMessageDelayed(EVT_TIMEOUT, TIMEOUT_MS);
                    sendMessage(EVT_EXEC);
                    break;
                case EVT_EXEC:
                    mPeripheralRef.get().discoverServices();
                    sendMessageDelayed(EVT_EXEC, EXEC_INTERVAL);
                    break;
                case EVT_DISCOVER_SERVICE_SUCCESS:
                    removeMessages(EVT_TIMEOUT);
                    removeMessages(EVT_EXEC);
                    if (!verifyServices()) {
                        CBLog.e("Detected abnormality in services.");
                        sendMessage(EVT_VERIFY_FAILED);
                        break;
                    }
                    CBLog.i("Discover service success.");
                    if (CBConfig.CreateBondOption.UsedAfterServicesDiscovered == mConfig.getCreateBondOption()
                            && !mPeripheralRef.get().isBonded()) {
                        transitionTo(mPairingState);
                    } else {
                        transitionTo(mConnectedState);
                    }
                    break;
                case EVT_DISCOVER_SERVICE_FAILURE: {
                    removeMessages(EVT_TIMEOUT);
                    removeMessages(EVT_EXEC);
                    CBLog.e("Discover service failure.");
                    Object[] objects = {CleanupConnectionState.RETRY};
                    transitionTo(mCleanupConnectionState, objects);
                    break;
                }
                case EVT_VERIFY_FAILED:
                    if (RETRY_FOR_VERIFY_FAILED_MAX > mRetryForVerifyFailedCount) {
                        mRetryForVerifyFailedCount++;
                        CBLog.w("Discover service retry. count:" + mRetryForVerifyFailedCount);
                        sendMessage(EVT_START);
                    } else {
                        CBLog.e("Discover service failed because retry count reaches the maximum value.");
                        sendMessage(EVT_DISCOVER_SERVICE_FAILURE);
                    }
                    break;
                case EVT_TIMEOUT:
                    CBLog.e("Discover service timeout.");
                    sendMessage(EVT_DISCOVER_SERVICE_FAILURE);
                    break;
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_START);
            removeMessages(EVT_EXEC);
            removeMessages(EVT_TIMEOUT);
            removeMessages(EVT_VERIFY_FAILED);
        }

        private boolean verifyServices() {
            List<BluetoothGattService> services = mPeripheralRef.get().getServices();
            if (0 >= services.size()) {
                CBLog.e("0 >= services.size()");
                return false;
            }
            for (BluetoothGattService service : services) {
                if (null == service.getCharacteristics()) {
                    CBLog.e("null == service.getCharacteristics()");
                    return false;
                } else if (0 >= service.getCharacteristics().size()) {
                    CBLog.e("0 >= service.getCharacteristics().size()");
                    return false;
                }
            }
            return true;
        }
    }

    private class ConnectCancelingState extends State {

        private static final int EVT_CONNECT_CANCEL_TIMEOUT = LOCAL_EVT_BASE + 0x0001;
        private static final long CONNECT_CANCEL_WAIT_TIME = 15 * 1000;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.ConnectCanceling);
            teardownOrTransitionToNextState();
            sendMessageDelayed(EVT_CONNECT_CANCEL_TIMEOUT, CONNECT_CANCEL_WAIT_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_BOND_NONE:
                case EVT_GATT_DISCONNECTED:
                    teardownOrTransitionToNextState();
                    break;
                case EVT_CONNECT_CANCEL_TIMEOUT:
                    CBLog.e("Connect cancel timeout.");
                    // There are cases when timeout has occurred without notification of
                    // ACL Disconnected or Bond None and move to next state in theses cases.
                    transitionTo(mConnectCanceledState);
                    break;
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_CONNECT_CANCEL_TIMEOUT);
        }

        private boolean isTeardownCompleted() {
            if (mPeripheralRef.get().isGattConnected()) {
                CBLog.i("Gatt disconnecting.");
                return false;
            }
            if (CBPeripheral.BondState.Bonding == mPeripheralRef.get().getBondState()) {
                CBLog.i("Bond process canceling.");
                return false;
            }
            CBLog.i("Teardown completed.");
            return true;
        }

        private void teardownOrTransitionToNextState() {
            if (isTeardownCompleted()) {
                transitionTo(mConnectCanceledState);
            } else {
                if (mPeripheralRef.get().isGattConnected()) {
                    mPeripheralRef.get().disconnectGatt();
                } else if (CBPeripheral.BondState.Bonding == mPeripheralRef.get().getBondState()) {
                    mPeripheralRef.get().cancelBondProcess();
                }
            }
        }
    }

    private class CleanupConnectionState extends State {

        static final int NOT_RETRY = 0;
        static final int RETRY = 1;

        private static final int EVT_GATT_CLOSED = LOCAL_EVT_BASE + 0x0001;
        private static final int EVT_CLEANUP_TIMEOUT = LOCAL_EVT_BASE + 0x0002;

        private static final long CLEANUP_WAIT_TIME = 15 * 1000;

        private boolean mHasRetryRequest;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.CleanupConnection);
            int retry = (int) transferObjects[0];
            mHasRetryRequest = (RETRY == retry);
            cleanupOrTransitionToNextState();
            sendMessageDelayed(EVT_CLEANUP_TIMEOUT, CLEANUP_WAIT_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_BOND_NONE:
                case EVT_GATT_DISCONNECTED:
                case EVT_GATT_CLOSED:
                    cleanupOrTransitionToNextState();
                    break;
                case EVT_CLEANUP_TIMEOUT:
                    transitionTo(mConnectFailedState);
                    break;
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_GATT_CLOSED);
            removeMessages(EVT_CLEANUP_TIMEOUT);
        }

        private boolean isCleanupCompleted() {
            if (mPeripheralRef.get().isGattConnected()) {
                CBLog.i("Gatt disconnecting.");
                return false;
            }
            if (CBPeripheral.BondState.Bonding == mPeripheralRef.get().getBondState()) {
                CBLog.i("Bond process canceling.");
                return false;
            }
            if (mPeripheralRef.get().hasGatt()) {
                CBLog.i("Gatt closing.");
                return false;
            }
            CBLog.i("Cleanup completed.");
            return true;
        }

        private void cleanup() {
            if (mPeripheralRef.get().isGattConnected()) {
                mPeripheralRef.get().disconnectGatt();
            } else if (CBPeripheral.BondState.Bonding == mPeripheralRef.get().getBondState()) {
                mPeripheralRef.get().cancelBondProcess();
            } else if (mPeripheralRef.get().hasGatt()) {
                if (mConfig.isUseRefreshWhenDisconnect()) {
                    mPeripheralRef.get().refreshGatt();
                }
                mPeripheralRef.get().closeGatt();
                sendMessage(EVT_GATT_CLOSED);
            }
        }

        private void cleanupOrTransitionToNextState() {
            if (!isCleanupCompleted()) {
                cleanup();
                return;
            }
            if (mHasRetryRequest && mConfig.isConnectionRetryEnabled()) {
                transitionTo(mConnectionRetryReadyState);
            } else {
                CBLog.w("Connection finished because not request a retry.");
                transitionTo(mConnectFailedState);
            }
        }
    }

    private class ConnectionRetryReadyState extends State {

        private static final int EVT_RETRY = LOCAL_EVT_BASE + 0x0001;

        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.ConnectionRetryReady);
            if (CBConfig.RETRY_UNLIMITED == mConfig.getConnectionRetryCount() ||
                    mConnectionRetryCount < mConfig.getConnectionRetryCount()) {
                mConnectionRetryCount++;
                sendMessageDelayed(EVT_RETRY, mConfig.getConnectionRetryDelayTime());
            } else {
                CBLog.e("Connection failed because retry count reaches the maximum value.");
                transitionTo(mConnectFailedState);
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_RETRY: {
                    CBLog.w("Connection retry. count:" + mConnectionRetryCount);
                    transitionTo(mConnectStartingState);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_RETRY);
        }
    }

    private class ConnectCanceledState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.ConnectCanceled);
            mCallback.didFailToConnect();
        }
    }

    private class ConnectionFailedState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.ConnectionFailed);
            mCallback.didFailToConnect();
        }
    }

    private class DisconnectedState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            _setDetailedState(CBPeripheralDetailedState.Disconnected);
            mCallback.didDisconnectPeripheral();
        }
    }
}
