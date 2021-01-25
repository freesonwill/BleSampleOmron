//
//  CBPeripheral.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.SynchronizeCallback;
import jp.co.ohq.utility.Types;

public class CBPeripheral extends AndroidPeripheral {

    private static final int EVENT_TIMEOUT = 10000;

    @NonNull
    private final LinkedList<ValueUpdatingEvent> mValueUpdatingEventQueue = new LinkedList<>();
    @NonNull
    private final InternalCallback mCallbackOfManager;
    @NonNull
    private final Handler mCallbackHandler;
    @NonNull
    private final CBConnector mConnector;
    @NonNull
    private List<CBService> mServices = new ArrayList<>();
    private boolean mIsValueUpdatingEventRunning = false;
    @Nullable
    private CBPeripheralDelegate mDelegate;
    @NonNull
    private final Runnable mEventTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            CBLog.e("Event timeout.");
            _confirmValueUpdatingEvent(CBStatusCode.GATT_INTERNAL_ERROR);
        }
    };

    CBPeripheral(
            @NonNull final Context context,
            @NonNull final BluetoothDevice bluetoothDevice,
            @NonNull final InternalCallback callbackOfManager,
            @NonNull final Looper looperOfManager) {
        super(context, bluetoothDevice, null);
        mCallbackOfManager = callbackOfManager;
        mCallbackHandler = new Handler(looperOfManager);
        mConnector = _createConnector();
    }

    public void delegate(@Nullable final CBPeripheralDelegate delegate) {
        CBLog.vMethodIn();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mDelegate = delegate;
            }
        });
    }

    public void discoverServices(@NonNull List<CBUUID> serviceUUIDs) {
    }

    public void discoverIncludedServices(@NonNull List<CBUUID> includedServiceUUIDs, @NonNull CBService service) {
    }

    @NonNull
    public List<CBService> services() {
        final List<CBService> ret;
        if (getHandler().isCurrentThread()) {
            ret = mServices;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mServices);
                    callback.unlock();
                }
            });
            callback.lock();
            ret = Types.autoCast(callback.getResult());
        }
        return ret;
    }

    public void discoverCharacteristics(@NonNull List<CBUUID[]> serviceUUIDs, @NonNull CBService service) {
    }

    public void discoverDescriptors(@NonNull CBCharacteristic characteristic) {
    }

    public void readValue(@NonNull final CBCharacteristic characteristic) {
        CBLog.vMethodIn();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mValueUpdatingEventQueue.add(new ValueUpdatingEvent(ValueUpdatingEvent.Type.ReadCharacteristic, characteristic));
                _startValueUpdatingEvent();
            }
        });
    }

    public void readValue(@NonNull final CBDescriptor descriptor) {
        CBLog.vMethodIn();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mValueUpdatingEventQueue.add(new ValueUpdatingEvent(ValueUpdatingEvent.Type.ReadDescriptor, descriptor));
                _startValueUpdatingEvent();
            }
        });
    }

    public void writeValue(@NonNull final byte[] data, @NonNull final CBCharacteristic characteristic, @NonNull final CBCharacteristicWriteType type) {
        CBLog.vMethodIn();
        characteristic.getBluetoothGattCharacteristic().setValue(data);
        characteristic.getBluetoothGattCharacteristic().setWriteType(type.value());
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mValueUpdatingEventQueue.add(new ValueUpdatingEvent(ValueUpdatingEvent.Type.WriteCharacteristic, characteristic));
                _startValueUpdatingEvent();
            }
        });
    }

    public void writeValue(@NonNull final byte[] data, @NonNull final CBDescriptor descriptor) {
        CBLog.vMethodIn();
        descriptor.getBluetoothGattDescriptor().setValue(data);
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mValueUpdatingEventQueue.add(new ValueUpdatingEvent(ValueUpdatingEvent.Type.WriteDescriptor, descriptor));
                _startValueUpdatingEvent();
            }
        });
    }

    public void setNotifyValue(final boolean enabled, @NonNull final CBCharacteristic characteristic) {
        CBLog.vMethodIn();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mValueUpdatingEventQueue.add(new ValueUpdatingEvent(ValueUpdatingEvent.Type.Notify, characteristic, enabled));
                _startValueUpdatingEvent();
            }
        });
    }

    @NonNull
    public CBPeripheralState state() {
        return mConnector.getState();
    }

    @NonNull
    public Bundle getConfig(@Nullable final List<CBConfig.Key> keys) {
        return mConnector.getConfig(keys);
    }

    public void setConfig(@NonNull final Bundle config) {
        mConnector.setConfig(config);
    }

    @NonNull
    public CBPeripheralDetailedState detailedState() {
        return mConnector.getDetailedState();
    }

    private void _startValueUpdatingEvent() {
        CBLog.vMethodIn();

        if (mValueUpdatingEventQueue.isEmpty()) {
            CBLog.e("ValueUpdatingEvent is Empty.");
            return;
        }
        if (mIsValueUpdatingEventRunning) {
            CBLog.d("Value Updating Event Running.");
            return;
        }
        mIsValueUpdatingEventRunning = true;

        ValueUpdatingEvent event = mValueUpdatingEventQueue.peek();

        if (!mConnector.isConnected()) {
            CBLog.e("!mConnector.isConnected()");
            _confirmValueUpdatingEvent(CBStatusCode.GATT_INTERNAL_ERROR);
            return;
        }

        boolean result = false;
        switch (event.type) {
            case ReadCharacteristic:
                result = _readValue(event.characteristic);
                break;
            case ReadDescriptor:
                result = _readValue(event.descriptor);
                break;
            case WriteCharacteristic:
                result = _writeValue(event.characteristic);
                break;
            case WriteDescriptor:
                result = _writeValue(event.descriptor);
                break;
            case Notify:
                result = _setNotifyValue(event.boolArg, event.characteristic);
                break;
            default:
                CBLog.e("Unknown event type.");
                break;
        }
        if (!result) {
            _confirmValueUpdatingEvent(CBStatusCode.GATT_INTERNAL_ERROR);
            return;
        }

        getHandler().postDelayed(mEventTimeoutRunnable, EVENT_TIMEOUT);
    }

    private void _confirmValueUpdatingEvent(final int status) {
        CBLog.vMethodIn();
        if (!mIsValueUpdatingEventRunning) {
            CBLog.e("!mIsValueUpdatingEventRunning");
            return;
        }
        mIsValueUpdatingEventRunning = false;

        getHandler().removeCallbacks(mEventTimeoutRunnable);

        final ValueUpdatingEvent event = mValueUpdatingEventQueue.poll();
        switch (event.type) {
            case ReadCharacteristic:
                _didUpdateValueFor(event.characteristic, status);
                break;
            case ReadDescriptor:
                _didUpdateValueFor(event.descriptor, status);
                break;
            case WriteCharacteristic:
                _didWriteValueFor(event.characteristic, status);
                break;
            case WriteDescriptor:
                _didWriteValueFor(event.descriptor, status);
                break;
            case Notify:
                _didUpdateNotificationStateFor(event.characteristic, status);
                break;
        }

        if (!mValueUpdatingEventQueue.isEmpty()) {
            _startValueUpdatingEvent();
        }
    }

    private boolean _readValue(
            @NonNull final CBCharacteristic characteristic) {
        CBLog.vMethodIn(characteristic.uuid().toString());

        boolean result = readCharacteristic(characteristic.getBluetoothGattCharacteristic());
        if (!result) {
            CBLog.e("readCharacteristic() failed.");
            return false;
        }

        return true;
    }

    private boolean _readValue(
            @NonNull final CBDescriptor descriptor) {
        CBLog.vMethodIn(descriptor.uuid().toString());

        boolean result = readDescriptor(descriptor.getBluetoothGattDescriptor());
        if (!result) {
            CBLog.e("readDescriptor() failed.");
            return false;
        }

        return true;
    }

    private boolean _writeValue(
            @NonNull CBCharacteristic characteristic) {
        CBLog.vMethodIn(characteristic.uuid().toString());

        boolean result = writeCharacteristic(characteristic.getBluetoothGattCharacteristic());
        if (!result) {
            CBLog.e("writeCharacteristic() failed.");
            return false;
        }

        return true;
    }

    private boolean _writeValue(
            @NonNull final CBDescriptor descriptor) {
        CBLog.vMethodIn(descriptor.uuid().toString());

        boolean result = writeDescriptor(descriptor.getBluetoothGattDescriptor());
        if (!result) {
            CBLog.e("writeDescriptor() failed.");
            return false;
        }

        return true;
    }

    private boolean _setNotifyValue(
            boolean enable,
            @NonNull CBCharacteristic characteristic) {
        CBLog.vMethodIn(characteristic.uuid().toString());

        CBDescriptor descriptor = characteristic.descriptor(new CBUUID(CBDescriptor.CBUUIDClientCharacteristicConfigurationString));
        if (null == descriptor) {
            CBLog.e("null == descriptor");
            return false;
        }

        BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristic.getBluetoothGattCharacteristic();
        BluetoothGattDescriptor bluetoothGattDescriptor = descriptor.getBluetoothGattDescriptor();

        byte[] value;
        int bluetoothGattCharacteristicProperties = bluetoothGattCharacteristic.getProperties();
        if (CBCharacteristicProperties.Indicate.contains(bluetoothGattCharacteristicProperties)) {
            if (enable) {
                CBLog.d("Enable indication.");
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
            } else {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            }
        } else if (CBCharacteristicProperties.Notify.contains(bluetoothGattCharacteristicProperties)) {
            if (enable) {
                CBLog.d("Enable notification.");
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            } else {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            }
        } else {
            CBLog.e("Notification unsupported.");
            return false;
        }

        boolean result = bluetoothGattDescriptor.setValue(value);
        if (!result) {
            CBLog.e("Desc set value failed.");
            return false;
        }

        result = setCharacteristicNotification(bluetoothGattCharacteristic, enable);
        if (!result) {
            CBLog.e("setCharacteristicNotification() failed.");
            return false;
        }

        result = writeDescriptor(bluetoothGattDescriptor);
        if (!result) {
            CBLog.e("writeDescriptor() failed.");
            return false;
        }

        return true;
    }

    private void _didUpdateValueFor(@NonNull final CBCharacteristic characteristic, final int status) {
        _didUpdateValueFor(characteristic, characteristic.value(), status);
    }

    private void _didUpdateValueFor(@NonNull final CBCharacteristic characteristic, @NonNull final byte[] value, final int status) {
        CBLog.vMethodIn(characteristic.uuid().toString());
        if (null == mDelegate) {
            CBLog.w("null == mDelegate");
            return;
        }
        final CBPeripheralDelegate delegate = mDelegate;
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.didUpdateValueFor(CBPeripheral.this, characteristic, value, status);
            }
        });
    }

    private void _didUpdateValueFor(@NonNull final CBDescriptor descriptor, final int status) {
        CBLog.vMethodIn(descriptor.uuid().toString());
        if (null == mDelegate) {
            CBLog.w("null == mDelegate");
            return;
        }
        final CBPeripheralDelegate delegate = mDelegate;
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.didUpdateValueFor(CBPeripheral.this, descriptor, status);
            }
        });
    }

    private void _didWriteValueFor(@NonNull final CBCharacteristic characteristic, final int status) {
        CBLog.vMethodIn(characteristic.uuid().toString());
        if (null == mDelegate) {
            CBLog.w("null == mDelegate");
            return;
        }
        final CBPeripheralDelegate delegate = mDelegate;
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.didWriteValueFor(CBPeripheral.this, characteristic, status);
            }
        });
    }

    private void _didWriteValueFor(@NonNull final CBDescriptor descriptor, final int status) {
        CBLog.vMethodIn(descriptor.uuid().toString());
        if (null == mDelegate) {
            CBLog.w("null == mDelegate");
            return;
        }
        final CBPeripheralDelegate delegate = mDelegate;
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.didWriteValueFor(CBPeripheral.this, descriptor, status);
            }
        });
    }

    private void _didUpdateNotificationStateFor(@NonNull final CBCharacteristic characteristic, final int status) {
        CBLog.vMethodIn(characteristic.uuid().toString());
        if (null == mDelegate) {
            CBLog.w("null == mDelegate");
            return;
        }
        final CBPeripheralDelegate delegate = mDelegate;
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.didUpdateNotificationStateFor(CBPeripheral.this, characteristic, status);
            }
        });
    }

    @Nullable
    private CBCharacteristic searchingCBCharacteristic(@NonNull List<CBService> services, @NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        for (CBService service : services) {
            for (CBCharacteristic characteristic : service.characteristics()) {
                if (characteristic.getBluetoothGattCharacteristic().equals(bluetoothGattCharacteristic)) {
                    return characteristic;
                }
            }
        }
        return null;
    }

    @NonNull
    private CBConnector _createConnector() {
        CBConnector.Callback callback = new CBConnector.Callback() {
            @Override
            public void didConnect() {
                mServices = createCBServices(CBPeripheral.this);
                mValueUpdatingEventQueue.clear();
                mIsValueUpdatingEventRunning = false;
                mCallbackOfManager.didConnect(CBPeripheral.this);
            }

            @Override
            public void didFailToConnect() {
                mCallbackOfManager.didFailToConnect(CBPeripheral.this);
            }

            @Override
            public void didDisconnectPeripheral() {
                mCallbackOfManager.didDisconnectPeripheral(CBPeripheral.this);
            }

            @Override
            public void onStateChanged(@NonNull CBPeripheralState newState) {
                mCallbackOfManager.onStateChanged(CBPeripheral.this, newState);
            }

            @Override
            public void onDetailedStateChanged(@NonNull CBPeripheralDetailedState newState) {
                mCallbackOfManager.onDetailedStateChanged(CBPeripheral.this, newState);
            }
        };

        return new CBConnector(this, callback, getHandler().getLooper());
    }

    @Override
    void onPairingRequest(@NonNull final CBConstants.PairingVariant variant) {
        CBLog.vMethodIn(variant.name());
        mCallbackOfManager.onPairingRequest(this, variant);
        mConnector.sendMessage(CBConnector.EVT_PAIRING_REQUEST, variant);
    }

    @Override
    void onBondStateChanged(@NonNull BondState newState) {
        CBLog.vMethodIn(newState.name());
        mCallbackOfManager.onBondStateChanged(this, newState);
        switch (newState) {
            case Bonded:
                mConnector.sendMessage(CBConnector.EVT_BONDED);
                break;
            case Bonding:
                mConnector.sendMessage(CBConnector.EVT_BONDING);
                break;
            case None:
                mConnector.sendMessage(CBConnector.EVT_BOND_NONE);
                break;
        }
    }

    @Override
    void onAclConnectionStateChanged(@NonNull AclConnectionState newState) {
        CBLog.vMethodIn(newState.name());
        mCallbackOfManager.onAclConnectionStateChanged(this, newState);
        switch (newState) {
            case Connected:
                mConnector.sendMessage(CBConnector.EVT_ACL_CONNECTED);
                break;
            case Disconnected:
                mConnector.sendMessage(CBConnector.EVT_ACL_DISCONNECTED);
                break;
        }
    }

    @Override
    void onGattConnectionStateChanged(@NonNull GattConnectionState newState, int status) {
        CBLog.vMethodIn(newState.name());
        mCallbackOfManager.onGattConnectionStateChanged(this, newState, status);
        switch (newState) {
            case Connected:
                mConnector.sendMessage(CBConnector.EVT_GATT_CONNECTED);
                break;
            case Disconnected:
                mConnector.sendMessage(CBConnector.EVT_GATT_DISCONNECTED, status);
                break;
        }
    }

    @Override
    void onServicesDiscovered(int status) {
        CBLog.vMethodIn();
        if (CBStatusCode.GATT_SUCCESS == status) {
            mConnector.sendMessage(CBConnector.EVT_DISCOVER_SERVICE_SUCCESS);
        } else {
            mConnector.sendMessage(CBConnector.EVT_DISCOVER_SERVICE_FAILURE, status);
        }
    }

    @Override
    void onCharacteristicRead(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic, final int status) {
        CBLog.vMethodIn();
        _confirmValueUpdatingEvent(status);
    }

    @Override
    void onDescriptorRead(@NonNull BluetoothGattDescriptor bluetoothGattDescriptor, final int status) {
        CBLog.vMethodIn();
        _confirmValueUpdatingEvent(status);
    }

    @Override
    void onCharacteristicWrite(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic, final int status) {
        CBLog.vMethodIn();
        _confirmValueUpdatingEvent(status);
    }

    @Override
    void onDescriptorWrite(@NonNull BluetoothGattDescriptor bluetoothGattDescriptor, final int status) {
        CBLog.vMethodIn();
        _confirmValueUpdatingEvent(status);
    }

    @Override
    void onCharacteristicChanged(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic, @NonNull byte[] value) {
        CBLog.vMethodIn();
        final CBCharacteristic characteristic = searchingCBCharacteristic(mServices, bluetoothGattCharacteristic);
        if (null == characteristic) {
            CBLog.e("null == characteristic");
            return;
        }
        _didUpdateValueFor(characteristic, value, CBStatusCode.GATT_SUCCESS);
    }

    @Override
    void onReliableWriteCompleted(int status) {

    }

    @Override
    void onReadRemoteRssi(int rssi, int status) {

    }

    @Override
    void onMtuChanged(int mtu, int status) {

    }

    void connect() {
        mConnector.sendMessage(CBConnector.EVT_CONNECT);
    }

    void cancelPeripheralConnection() {
        mConnector.sendMessage(CBConnector.EVT_CANCEL_PERIPHERAL_CONNECTION);
    }

    @NonNull
    private List<CBService> createCBServices(@NonNull CBPeripheral peripheral) {
        List<CBService> services = new ArrayList<>();
        for (BluetoothGattService bluetoothGattService : peripheral.getServices()) {
            services.add(new CBService(peripheral, bluetoothGattService));
        }
        outputAttributeLog(services);
        return services;
    }

    private void outputAttributeLog(@NonNull List<CBService> services) {
        for (CBService service : services) {
            CBLog.i(service.toString());
        }
    }

    interface InternalCallback {

        void didConnect(@NonNull CBPeripheral peripheral);

        void didFailToConnect(@NonNull CBPeripheral peripheral);

        void didDisconnectPeripheral(@NonNull CBPeripheral peripheral);

        void onStateChanged(@NonNull CBPeripheral peripheral, @NonNull CBPeripheralState newState);

        void onDetailedStateChanged(@NonNull CBPeripheral peripheral, @NonNull CBPeripheralDetailedState newState);

        void onPairingRequest(@NonNull CBPeripheral peripheral, @NonNull CBConstants.PairingVariant variant);

        void onBondStateChanged(@NonNull CBPeripheral peripheral, @NonNull BondState bondState);

        void onAclConnectionStateChanged(@NonNull CBPeripheral peripheral, @NonNull AclConnectionState aclConnectionState);

        void onGattConnectionStateChanged(@NonNull CBPeripheral peripheral, @NonNull GattConnectionState gattConnectionState, int status);
    }

    private static class ValueUpdatingEvent {
        @NonNull
        final Type type;
        final CBCharacteristic characteristic;
        final CBDescriptor descriptor;
        final byte[] bytesArg;
        final boolean boolArg;

        ValueUpdatingEvent(@NonNull Type type, @NonNull CBCharacteristic characteristic) {
            this.type = type;
            this.characteristic = characteristic;
            this.descriptor = null;
            this.bytesArg = null;
            this.boolArg = false;
        }

        ValueUpdatingEvent(@NonNull Type type, @NonNull CBCharacteristic characteristic, boolean boolArg) {
            this.type = type;
            this.characteristic = characteristic;
            this.descriptor = null;
            this.bytesArg = null;
            this.boolArg = boolArg;
        }

        ValueUpdatingEvent(@NonNull Type type, @NonNull CBDescriptor descriptor) {
            this.type = type;
            this.characteristic = null;
            this.descriptor = descriptor;
            this.bytesArg = null;
            this.boolArg = false;
        }

        enum Type {
            ReadCharacteristic, ReadDescriptor, WriteCharacteristic, WriteDescriptor, Notify
        }
    }
}
