//
//  AndroidPeripheral.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.SynchronizeCallback;

abstract class AndroidPeripheral {

    @NonNull
    private final Handler mHandler;
    @NonNull
    private final Context mContext;
    @NonNull
    private final BluetoothDevice mBluetoothDevice;
    @NonNull
    private final String mAddress;
    @Nullable
    private final String mLocalName;
    @NonNull
    private BondState mBondState;
    @NonNull
    private AclConnectionState mAclConnectionState;
    @NonNull
    private GattConnectionState mGattConnectionState;
    @Nullable
    private BluetoothGatt mBluetoothGatt;

    AndroidPeripheral(
            @NonNull Context context,
            @NonNull BluetoothDevice bluetoothDevice,
            @Nullable Looper looper) {
        if (null == looper) {
            HandlerThread thread = new HandlerThread("Peripheral-" + bluetoothDevice.getAddress());
            thread.start();
            looper = thread.getLooper();
        }
        mHandler = new Handler(looper);

        mContext = context;
        mBluetoothDevice = bluetoothDevice;
        mAddress = bluetoothDevice.getAddress();
        mLocalName = bluetoothDevice.getName();
        mBondState = BondState.valueOf(bluetoothDevice.getBondState());
        mAclConnectionState = AclConnectionState.Unknown;
        mGattConnectionState = GattConnectionState.Disconnected;
        mBluetoothGatt = null;

        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (null == bluetoothManager) {
            throw new AndroidRuntimeException("null == bluetoothManager");
        }
        int gattConnectionState = bluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT);
        if (BluetoothProfile.STATE_DISCONNECTED != gattConnectionState) {
            CBLog.w("Illegal onGattConnectionStateChanged state is BluetoothProfile.STATE_DISCONNECTED != gattConnectionState");
        }
    }

    @NonNull
    public String getAddress() {
        return mAddress;
    }

    @Nullable
    public String getLocalName() {
        return mLocalName;
    }

    void notifyPairingRequest(@NonNull final CBConstants.PairingVariant pairingVariant) {
        if (mHandler.isCurrentThread()) {
            onPairingRequest(pairingVariant);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onPairingRequest(pairingVariant);
                }
            });
        }
    }

    public boolean isBonded() {
        return BondState.Bonded == getBondState();
    }

    public BondState getBondState() {
        final BondState state;
        if (mHandler.isCurrentThread()) {
            state = mBondState;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mBondState);
                    callback.unlock();
                }
            });
            callback.lock();
            state = (BondState) callback.getResult();
        }
        return state;
    }

    void setBondState(@NonNull final BondState state) {
        if (mHandler.isCurrentThread()) {
            mBondState = state;
            onBondStateChanged(mBondState);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBondState = state;
                    onBondStateChanged(mBondState);
                }
            });
        }
    }

    public boolean isAclConnected() {
        return AclConnectionState.Connected == getAclConnectionState();
    }

    @NonNull
    public AclConnectionState getAclConnectionState() {
        final AclConnectionState state;
        if (mHandler.isCurrentThread()) {
            state = mAclConnectionState;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mAclConnectionState);
                    callback.unlock();
                }
            });
            callback.lock();
            state = (AclConnectionState) callback.getResult();
        }
        return state;
    }

    void setAclConnectionState(@NonNull final AclConnectionState state) {
        if (mHandler.isCurrentThread()) {
            mAclConnectionState = state;
            onAclConnectionStateChanged(mAclConnectionState);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAclConnectionState = state;
                    onAclConnectionStateChanged(mAclConnectionState);
                }
            });
        }
    }

    public boolean isGattConnected() {
        return GattConnectionState.Connected == getGattConnectionState();
    }

    @NonNull
    public GattConnectionState getGattConnectionState() {
        final GattConnectionState state;
        if (mHandler.isCurrentThread()) {
            state = mGattConnectionState;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mGattConnectionState);
                    callback.unlock();
                }
            });
            callback.lock();
            state = (GattConnectionState) callback.getResult();
        }
        return state;
    }

    @NonNull
    Context getContext() {
        return mContext;
    }

    @NonNull
    Handler getHandler() {
        return mHandler;
    }

    @SuppressLint("NewApi")
    boolean createBond() {
        boolean ret = false;
        CBLog.iOsApi("createBond() exec.");
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            try {
                ret = (Boolean) invokeMethod(mBluetoothDevice, "createBond", null, null);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            ret = mBluetoothDevice.createBond();
        }
        if (ret) {
            CBLog.d("createBond() called. ret=true");
        } else {
            CBLog.e("createBond() called. ret=false");
        }
        return ret;
    }

    boolean cancelBondProcess() {
        boolean ret = false;
        CBLog.iOsApi("cancelBondProcess() exec.");
        try {
            ret = (Boolean) invokeMethod(mBluetoothDevice, "cancelBondProcess", null, null);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (ret) {
            CBLog.d("cancelBondProcess() called. ret=true");
        } else {
            CBLog.e("cancelBondProcess() called. ret=false");
        }
        return ret;
    }

    boolean removeBond() {
        boolean ret = false;
        CBLog.iOsApi("removeBond() exec.");
        try {
            ret = (Boolean) invokeMethod(mBluetoothDevice, "removeBond", null, null);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (ret) {
            CBLog.d("removeBond() called. ret=true");
        } else {
            CBLog.e("removeBond() called. ret=false");
        }
        return ret;
    }

    @SuppressLint("NewApi")
    boolean setPairingConfirmation(boolean enable) {
        boolean ret = false;
        CBLog.iOsApi("setPairingConfirmation(" + enable + ") exec.");
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            try {
                ret = (Boolean) invokeMethod(
                        mBluetoothDevice,
                        "setPairingConfirmation",
                        new Class<?>[]{boolean.class},
                        new Object[]{enable}
                );
            } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else if (Build.VERSION_CODES.N <= Build.VERSION.SDK_INT) {
            try {
                ret = mBluetoothDevice.setPairingConfirmation(enable);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            ret = mBluetoothDevice.setPairingConfirmation(enable);
        }
        if (ret) {
            CBLog.d("setPairingConfirmation() called. ret=true");
        } else {
            CBLog.e("setPairingConfirmation() called. ret=false");
        }
        return ret;
    }

    @SuppressLint("NewApi")
    boolean setPin(String pinCode) {
        boolean ret = false;
        byte[] pin = _convertPinToBytes(pinCode);
        if (null == pin) {
            CBLog.e("null == pin");
            return false;
        }

        CBLog.iOsApi("setPin(" + pinCode + ") exec.");
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            try {
                ret = (Boolean) invokeMethod(
                        mBluetoothDevice,
                        "setPin",
                        new Class<?>[]{byte[].class},
                        new Object[]{pin}
                );
            } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            ret = mBluetoothDevice.setPin(pin);
        }
        if (ret) {
            CBLog.d("setPin() called. ret=true");
        } else {
            CBLog.e("setPin() called. ret=false");
        }
        return ret;
    }

    boolean setPasskey(String pinCode) {
        boolean ret = false;
        CBLog.iOsApi("setPasskey(" + pinCode + ") exec.");
        try {
            ByteBuffer converter = ByteBuffer.allocate(4);
            converter.order(ByteOrder.nativeOrder());
            converter.putInt(Integer.parseInt(pinCode));
            byte[] pin = converter.array();
            ret = (Boolean) invokeMethod(
                    invokeMethod(BluetoothDevice.class, "setPasskey", null, null),
                    "setPasskey",
                    new Class<?>[]{BluetoothDevice.class, boolean.class, int.class, byte[].class},
                    new Object[]{mBluetoothDevice, true, pin.length, pin}
            );
        } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (ret) {
            CBLog.d("setPasskey() called. ret=true");
        } else {
            CBLog.e("setPasskey() called. ret=false");
        }
        return ret;
    }

    boolean hasGatt() {
        return null != mBluetoothGatt;
    }

    boolean connectGatt() {
        if (null != mBluetoothGatt) {
            CBLog.e("null != mBluetoothGatt");
            return false;
        }

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                CBLog.i(String.format(Locale.US, "newState=%d status=%d(0x%02x)", newState, status, status));
                final GattConnectionState gattConnectionState = GattConnectionState.valueOf(newState);
                CBLog.iOsApi("Received " + mAddress + " of " + gattConnectionState.name() + ". status:" +
                        String.format(Locale.US, "status=%d(0x%02x)", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGattConnectionState = gattConnectionState;
                        onGattConnectionStateChanged(mGattConnectionState, status);
                    }
                });
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                CBLog.iOsApi(String.format(Locale.US, "status=%d(0x%02x)", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onServicesDiscovered(status);
                    }
                });
            }

            @Override
            public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
                CBLog.iOsApi(characteristic.getUuid().toString() + " " +
                        String.format(Locale.US, "status=%d(0x%02x)", status, status));
                if (CBStatusCode.GATT_SUCCESS == status && null != characteristic.getValue()) {
                    CBLog.iOsApi("raw data : " + byteDataToHexString(characteristic.getValue()));
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onCharacteristicRead(characteristic, status);
                    }
                });
            }

            @Override
            public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
                CBLog.iOsApi(characteristic.getUuid().toString() + " " +
                        String.format(Locale.US, "status=%d(0x%02x)", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onCharacteristicWrite(characteristic, status);
                    }
                });
            }

            @Override
            public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                CBLog.iOsApi(characteristic.getUuid().toString());
                final byte[] value = characteristic.getValue();
                CBLog.iOsApi("raw data : " + byteDataToHexString(value));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onCharacteristicChanged(characteristic, value);
                    }
                });
            }

            @Override
            public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
                CBLog.iOsApi(descriptor.getCharacteristic().getUuid().toString() + " " +
                        descriptor.getUuid().toString() + " " +
                        String.format(Locale.US, "status=%d(0x%02x)", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onDescriptorRead(descriptor, status);
                    }
                });
            }

            @Override
            public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
                CBLog.iOsApi(descriptor.getCharacteristic().getUuid().toString() + " " +
                        descriptor.getUuid().toString() + " " +
                        String.format(Locale.US, "status=%d(0x%02x)", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onDescriptorWrite(descriptor, status);
                    }
                });
            }

            @Override
            public void onReliableWriteCompleted(final BluetoothGatt gatt, final int status) {
                CBLog.iOsApi(String.format(Locale.US, "status=%d(0x%02x)", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onReliableWriteCompleted(status);
                    }
                });
            }

            @Override
            public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, final int status) {
                CBLog.iOsApi("rssi=" + rssi + " " + String.format(Locale.US, "status=%d(0x%02x) ", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onReadRemoteRssi(rssi, status);
                    }
                });
            }

            @Override
            public void onMtuChanged(final BluetoothGatt gatt, final int mtu, final int status) {
                CBLog.iOsApi("mtu=" + mtu + " " + String.format(Locale.US, "status=%d(0x%02x) ", status, status));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidPeripheral.this.onMtuChanged(mtu, status);
                    }
                });
            }
        };

        CBLog.iOsApi("connectGatt() exec.");
        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, gattCallback);
        if (null != mBluetoothGatt) {
            CBLog.d("connectGatt() called. ret=Not Null");
        } else {
            CBLog.e("connectGatt() called. ret=Null");
        }
        return null != mBluetoothGatt;
    }

    boolean disconnectGatt() {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("disconnect() exec.");
        mBluetoothGatt.disconnect();
        CBLog.d("disconnect() called.");
        return true;
    }

    boolean discoverServices() {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("discoverServices() exec.");
        boolean ret = mBluetoothGatt.discoverServices();
        if (ret) {
            CBLog.d("discoverServices() called. ret=true");
        } else {
            CBLog.e("discoverServices() called. ret=false");
        }
        return ret;
    }

    boolean refreshGatt() {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        boolean ret = false;
        CBLog.iOsApi("refresh() exec.");
        try {
            ret = (Boolean) invokeMethod(mBluetoothGatt, "refresh", null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret) {
            CBLog.d("refresh() called. ret=true");
        } else {
            CBLog.e("refresh() called. ret=false");
        }
        return ret;
    }

    boolean closeGatt() {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("close() exec.");
        mBluetoothGatt.close();
        CBLog.d("close() called.");
        mBluetoothGatt = null;
        return true;
    }

    @NonNull
    List<BluetoothGattService> getServices() {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return new ArrayList<>();
        }
        CBLog.iOsApi("getServices() exec.");
        List<BluetoothGattService> ret = mBluetoothGatt.getServices();
        if (null == ret) {
            CBLog.e("getServices() called. ret=Null");
            return new ArrayList<>();
        }
        if (0 == ret.size()) {
            CBLog.d("getServices() called. ret.size=0");
        } else {
            CBLog.d("getServices() called. ret=Not Null");
        }
        return ret;
    }

    boolean setCharacteristicNotification(@NonNull final BluetoothGattCharacteristic characteristic, boolean enable) {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("setCharacteristicNotification(" + characteristic.getUuid().toString() + ", " + enable + ") exec.");
        boolean ret = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (ret) {
            CBLog.d("setCharacteristicNotification() called. ret=true");
        } else {
            CBLog.e("setCharacteristicNotification() called. ret=false");
        }
        return ret;
    }

    boolean readCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic) {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("readCharacteristic(" + characteristic.getUuid().toString() + ") exec.");
        boolean ret = mBluetoothGatt.readCharacteristic(characteristic);
        if (ret) {
            CBLog.d("readCharacteristic() called. ret=true");
        } else {
            CBLog.e("readCharacteristic() called. ret=false");
        }
        return ret;
    }

    boolean writeCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic) {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("writeCharacteristic(" + characteristic.getUuid().toString() + ") exec.");
        CBLog.iOsApi("raw data : " + byteDataToHexString(characteristic.getValue()));
        boolean ret = mBluetoothGatt.writeCharacteristic(characteristic);
        if (ret) {
            CBLog.d("writeCharacteristic() called. ret=true");
        } else {
            CBLog.e("writeCharacteristic() called. ret=false");
        }
        return ret;
    }

    boolean readDescriptor(@NonNull final BluetoothGattDescriptor descriptor) {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("readDescriptor(" + descriptor.getCharacteristic().getUuid().toString() + ", " +
                descriptor.getUuid().toString() + ") exec.");
        boolean ret = mBluetoothGatt.readDescriptor(descriptor);
        if (ret) {
            CBLog.d("readDescriptor() called. ret=true");
        } else {
            CBLog.e("readDescriptor() called. ret=false");
        }
        return ret;
    }

    boolean writeDescriptor(@NonNull final BluetoothGattDescriptor descriptor) {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        CBLog.iOsApi("writeDescriptor(" + descriptor.getCharacteristic().getUuid().toString() + ", " +
                descriptor.getUuid().toString() + ") exec.");
        boolean ret = mBluetoothGatt.writeDescriptor(descriptor);
        if (ret) {
            CBLog.d("writeDescriptor() called. ret=true");
        } else {
            CBLog.e("writeDescriptor() called. ret=false");
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    boolean requestMtu(int mtu) {
        if (null == mBluetoothGatt) {
            CBLog.e("null == mBluetoothGatt");
            return false;
        }
        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            CBLog.e("VERSION_CODES.LOLLIPOP > VERSION.SDK_INT");
            return false;
        }
        CBLog.iOsApi("requestMtu(" + mtu + ") exec.");
        boolean ret = mBluetoothGatt.requestMtu(mtu);
        if (ret) {
            CBLog.d("requestMtu() called. ret=true");
        } else {
            CBLog.e("requestMtu() called. ret=false");
        }
        return ret;
    }

    private byte[] _convertPinToBytes(String pin) {
        byte[] ret = null;
        try {
            Class<?>[] types = {
                    String.class
            };
            Object[] args = {
                    pin
            };
            ret = (byte[]) invokeMethod(mBluetoothDevice, "convertPinToBytes", types, args);
        } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @NonNull
    private Object invokeMethod(@NonNull Object target, @NonNull String methodName, @Nullable Class<?>[] parameterClasses, @Nullable Object[] paramterValues)
            throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class<?> clazz = target.getClass();
        Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
        return method.invoke(target, paramterValues);
    }

    private String byteDataToHexString(@NonNull byte[] data) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }

    abstract void onPairingRequest(@NonNull CBConstants.PairingVariant variant);

    abstract void onBondStateChanged(@NonNull BondState bondState);

    abstract void onAclConnectionStateChanged(@NonNull AclConnectionState aclConnectionState);

    abstract void onGattConnectionStateChanged(@NonNull GattConnectionState gattConnectionState, int status);

    abstract void onServicesDiscovered(int status);

    abstract void onCharacteristicRead(@NonNull BluetoothGattCharacteristic characteristic, int status);

    abstract void onCharacteristicWrite(@NonNull BluetoothGattCharacteristic characteristic, int status);

    abstract void onCharacteristicChanged(@NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value);

    abstract void onDescriptorRead(@NonNull BluetoothGattDescriptor descriptor, int status);

    abstract void onDescriptorWrite(@NonNull BluetoothGattDescriptor descriptor, int status);

    abstract void onReliableWriteCompleted(int status);

    abstract void onReadRemoteRssi(int rssi, int status);

    abstract void onMtuChanged(int mtu, int status);

    public enum BondState {
        None(BluetoothDevice.BOND_NONE),
        Bonding(BluetoothDevice.BOND_BONDING),
        Bonded(BluetoothDevice.BOND_BONDED);
        private int value;

        BondState(int value) {
            this.value = value;
        }

        static BondState valueOf(int value) {
            for (BondState type : values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return None;
        }

        int value() {
            return value;
        }
    }

    public enum AclConnectionState {
        Disconnected,
        Connected,
        Unknown
    }

    public enum GattConnectionState {
        Disconnected(BluetoothProfile.STATE_DISCONNECTED),
        Connected(BluetoothProfile.STATE_CONNECTED);
        private int value;

        GattConnectionState(int value) {
            this.value = value;
        }

        static GattConnectionState valueOf(int value) {
            for (GattConnectionState type : values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return Disconnected;
        }

        int value() {
            return value;
        }
    }
}
