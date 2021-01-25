package jp.co.ohq.androidcorebluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.co.ohq.utility.SynchronizeCallback;

import static android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED;

public class CBCentralManager extends CBManager {

    @NonNull
    private final CBConfig mConfig = new CBConfig();
    @NonNull
    private final LinkedHashMap<String, CBPeripheral> mPeripherals = new LinkedHashMap<>();
    @NonNull
    private final CBCentralManagerDelegate mDelegate;
    @NonNull
    private final CBScanner mScanner;

    public CBCentralManager(
            @NonNull final Context context,
            @NonNull final CBCentralManagerDelegate delegate,
            @Nullable Looper looper) {
        super(context, looper);
        mDelegate = delegate;
        CBScanner.ScanListener scanListener = new CBScanner.ScanListener() {
            @Override
            void onScan(@NonNull final BluetoothDevice bluetoothDevice, final int rssi, final @NonNull byte[] scanRecord) {
                if (getHandler().isCurrentThread()) {
                    _onScan(bluetoothDevice, rssi, scanRecord);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            _onScan(bluetoothDevice, rssi, scanRecord);
                        }
                    });
                }
            }
        };
        mScanner = new CBScanner(context, scanListener, looper);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CBConstants.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(@NonNull Context context, @NonNull final Intent intent) {
                CBLog.vMethodIn();
                if (getHandler().isCurrentThread()) {
                    _onBroadcastReceived(intent);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            _onBroadcastReceived(intent);
                        }
                    });
                }
            }
        };
        getContext().registerReceiver(broadcastReceiver, intentFilter);

        _initPeripherals();
    }

    public boolean isScanning() {
        boolean ret;
        if (getHandler().isCurrentThread()) {
            ret = mScanner.isScanning();
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mScanner.isScanning());
                    callback.unlock();
                }
            });
            callback.lock();
            ret = (boolean) callback.getResult();
        }
        return ret;
    }

    public void connect(@NonNull final CBPeripheral peripheral) {
        if (getHandler().isCurrentThread()) {
            _connect(peripheral);
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    _connect(peripheral);
                }
            });
        }
    }

    public void cancelPeripheralConnection(@NonNull final CBPeripheral peripheral) {
        if (getHandler().isCurrentThread()) {
            _cancelPeripheralConnection(peripheral);
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    _cancelPeripheralConnection(peripheral);
                }
            });
        }
    }

    @NonNull
    public List<CBPeripheral> retrieveConnectedPeripherals(@NonNull final List<CBUUID> serviceUUIDs) {
        return new ArrayList<>();
    }

    @Nullable
    public CBPeripheral retrievePeripherals(@NonNull final String address) {
        final CBPeripheral peripheral;
        if (getHandler().isCurrentThread()) {
            peripheral = _retrievePeripherals(address);
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(_retrievePeripherals(address));
                    callback.unlock();
                }
            });
            callback.lock();
            peripheral = (CBPeripheral) callback.getResult();
        }
        return peripheral;
    }

    public void scanForPeripherals(@NonNull final List<CBUUID> serviceUUIDs) {
//        if (!isPermissionGranted(LE_SCAN_PERMISSIONS)) {
//            throw new SecurityException("Permission denied.");
//        }
        if (getHandler().isCurrentThread()) {
            _scanForPeripherals(serviceUUIDs);
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    _scanForPeripherals(serviceUUIDs);
                }
            });
        }
    }

    public void stopScan() {
        if (getHandler().isCurrentThread()) {
            _stopScan();
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    _stopScan();
                }
            });
        }
    }

    public Bundle getDefaultConfig(@Nullable final List<CBConfig.Key> keys) {
        final Bundle config;
        if (getHandler().isCurrentThread()) {
            config = mConfig.getDefault(keys);
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mConfig.getDefault(keys));
                    callback.unlock();
                }
            });
            callback.lock();
            config = (Bundle) callback.getResult();
        }
        CBLog.d(config.toString());
        return config;
    }

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
        CBLog.d(config.toString());
        return config;
    }

    public void setConfig(@NonNull final Bundle config) {
        CBLog.d(config.toString());
        if (getHandler().isCurrentThread()) {
            _setConfig(config);
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    _setConfig(config);
                }
            });
        }
    }

    @Override
    protected void onStateChanged(@NonNull final CBManagerState newState) {
        if (CBManagerState.PoweredOff == newState) {
            _deinitPeripherals();
        } else if (CBManagerState.PoweredOn == newState) {
            _initPeripherals();
        }
        mDelegate.centralManagerDidUpdateState(this, newState);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isPermissionGranted(final String[] permissions) {
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            for (String permission : permissions) {
                if (PackageManager.PERMISSION_DENIED == getContext().checkSelfPermission(permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void _initPeripherals() {
        mPeripherals.clear();
        Set<BluetoothDevice> bondedDevices = getAdapter().getBondedDevices();
        if (null != bondedDevices) {
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                mPeripherals.put(bluetoothDevice.getAddress(), _createBlePeripheral(bluetoothDevice));
            }
        }
    }

    private void _deinitPeripherals() {
        for (Map.Entry<String, CBPeripheral> entry : mPeripherals.entrySet()) {
            CBPeripheral peripheral = entry.getValue();
            peripheral.cancelPeripheralConnection();
        }
    }

    private void _setConfig(@NonNull Bundle config) {
        mConfig.set(config);
        for (Map.Entry<String, CBPeripheral> entry : mPeripherals.entrySet()) {
            CBPeripheral peripheral = entry.getValue();
            peripheral.setConfig(config);
        }
    }

    private void _scanForPeripherals(@NonNull final List<CBUUID> serviceUUIDs) {
        if (CBManagerState.PoweredOn != state()) {
            CBLog.e("Bluetooth not work.");
            return;
        }
        mScanner.scanForPeripherals(serviceUUIDs, 0);
    }

    private void _stopScan() {
        if (CBManagerState.PoweredOn != state()) {
            CBLog.w("Bluetooth not work.");
            return;
        }
        mScanner.stopScan();
    }

    @Nullable
    private CBPeripheral _retrievePeripherals(@NonNull String address) {
        CBPeripheral peripheral;
        if (mPeripherals.containsKey(address)) {
            peripheral = mPeripherals.get(address);
            CBLog.d("From the cache.");
        } else {
            try {
                BluetoothDevice bluetoothDevice = getAdapter().getRemoteDevice(address);
                peripheral = _createBlePeripheral(bluetoothDevice);
                mPeripherals.put(address, peripheral);
                CBLog.d("From the OS.");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                peripheral = null;
            }
        }
        return peripheral;
    }

    private void _connect(@NonNull final CBPeripheral peripheral) {
        if (CBManagerState.PoweredOn != state()) {
            CBLog.e("Bluetooth not work.");
            return;
        }
        peripheral.connect();
    }

    private void _cancelPeripheralConnection(@NonNull final CBPeripheral peripheral) {
        if (CBManagerState.PoweredOn != state()) {
            CBLog.e("Bluetooth not work.");
            return;
        }
        peripheral.cancelPeripheralConnection();
    }

    private void _onScan(@NonNull final BluetoothDevice bluetoothDevice, final int rssi, final @NonNull byte[] scanRecord) {
        final CBPeripheral peripheral;
        if (mPeripherals.containsKey(bluetoothDevice.getAddress())) {
            peripheral = mPeripherals.get(bluetoothDevice.getAddress());
        } else {
            CBLog.i("New peripheral detected. addr:" + bluetoothDevice.getAddress());
            peripheral = _createBlePeripheral(bluetoothDevice);
            mPeripherals.put(bluetoothDevice.getAddress(), peripheral);
        }
        mDelegate.didDiscover(this, peripheral, scanRecord, rssi);
    }

    private void _onBroadcastReceived(@NonNull Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String address = bluetoothDevice.getAddress();
        String action = intent.getAction();
        if (!mPeripherals.containsKey(address)) {
            CBLog.w("Ignore the " + action + " broadcast. target:" + address);
            return;
        }
        CBPeripheral peripheral = mPeripherals.get(address);
        if (CBConstants.ACTION_PAIRING_REQUEST.equals(action)) {
            CBConstants.PairingVariant pairingVariant = CBConstants.PairingVariant.valueOf(
                    intent.getIntExtra(CBConstants.EXTRA_PAIRING_VARIANT, CBConstants.PAIRING_VARIANT_UNKNOWN));
            CBLog.iOsApi("Received ACTION_PAIRING_REQUEST of " + address + ". variant:" + pairingVariant.name());
            peripheral.notifyPairingRequest(pairingVariant);
        } else if (ACTION_BOND_STATE_CHANGED.equals(action)) {
            AndroidPeripheral.BondState prevState = AndroidPeripheral.BondState.valueOf(
                    intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE));
            AndroidPeripheral.BondState newState = AndroidPeripheral.BondState.valueOf(
                    intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE));
            CBLog.iOsApi("Received ACTION_BOND_STATE_CHANGED[" + prevState.name() + " -> " + newState.name() + "] of " + address + ".");
            peripheral.setBondState(newState);
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            CBLog.iOsApi("Received ACTION_ACL_CONNECTED of " + address + ".");
            peripheral.setAclConnectionState(AndroidPeripheral.AclConnectionState.Connected);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            CBLog.iOsApi("Received ACTION_ACL_DISCONNECTED of " + address + ".");
            peripheral.setAclConnectionState(AndroidPeripheral.AclConnectionState.Disconnected);
        }
    }

    @NonNull
    private CBPeripheral _createBlePeripheral(@NonNull final BluetoothDevice bluetoothDevice) {
        final CBPeripheral.InternalCallback peripheralCallback = new CBPeripheral.InternalCallback() {
            @Override
            public void didConnect(@NonNull final CBPeripheral peripheral) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.didConnect(CBCentralManager.this, peripheral);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.didConnect(CBCentralManager.this, peripheral);
                        }
                    });
                }
            }

            @Override
            public void didFailToConnect(@NonNull final CBPeripheral peripheral) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.didFailToConnect(CBCentralManager.this, peripheral);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.didFailToConnect(CBCentralManager.this, peripheral);
                        }
                    });
                }
            }

            @Override
            public void didDisconnectPeripheral(@NonNull final CBPeripheral peripheral) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.didDisconnectPeripheral(CBCentralManager.this, peripheral);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.didDisconnectPeripheral(CBCentralManager.this, peripheral);
                        }
                    });
                }
            }

            @Override
            public void onStateChanged(@NonNull final CBPeripheral peripheral, @NonNull final CBPeripheralState newState) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.onConnectionStateChanged(CBCentralManager.this, peripheral, newState);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.onConnectionStateChanged(CBCentralManager.this, peripheral, newState);
                        }
                    });
                }
            }

            @Override
            public void onDetailedStateChanged(@NonNull final CBPeripheral peripheral, @NonNull final CBPeripheralDetailedState newState) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.onDetailedStateChanged(CBCentralManager.this, peripheral, newState);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.onDetailedStateChanged(CBCentralManager.this, peripheral, newState);
                        }
                    });
                }
            }

            @Override
            public void onPairingRequest(@NonNull final CBPeripheral peripheral, @NonNull CBConstants.PairingVariant variant) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.onPairingRequest(CBCentralManager.this, peripheral);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.onPairingRequest(CBCentralManager.this, peripheral);
                        }
                    });
                }
            }

            @Override
            public void onBondStateChanged(@NonNull final CBPeripheral peripheral, @NonNull final CBPeripheral.BondState bondState) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.onBondStateChanged(CBCentralManager.this, peripheral, bondState);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.onBondStateChanged(CBCentralManager.this, peripheral, bondState);
                        }
                    });
                }
            }

            @Override
            public void onAclConnectionStateChanged(@NonNull final CBPeripheral peripheral, @NonNull final CBPeripheral.AclConnectionState aclConnectionState) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.onAclConnectionStateChanged(CBCentralManager.this, peripheral, aclConnectionState);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.onAclConnectionStateChanged(CBCentralManager.this, peripheral, aclConnectionState);
                        }
                    });
                }
            }

            @Override
            public void onGattConnectionStateChanged(@NonNull final CBPeripheral peripheral, @NonNull final CBPeripheral.GattConnectionState gattConnectionState, final int status) {
                if (getHandler().isCurrentThread()) {
                    mDelegate.onGattConnectionStateChanged(CBCentralManager.this, peripheral, gattConnectionState, status);
                } else {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDelegate.onGattConnectionStateChanged(CBCentralManager.this, peripheral, gattConnectionState, status);
                        }
                    });
                }
            }
        };

        return new CBPeripheral(getContext(), bluetoothDevice, peripheralCallback, getHandler().getLooper());
    }
}
