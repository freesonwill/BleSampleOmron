//
//  BleScannerOld.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.SynchronizeCallback;

class CBScanner {
    private static final int MSG_ON_SCAN = 1;
    @NonNull
    private final Handler mHandler;
    @NonNull
    private final ScanListener mScanListener;
    @NonNull
    private final BluetoothAdapter mBluetoothAdapter;
    @NonNull
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            CBLog.iOsApi(device.getAddress());
            mHandler.sendMessage(MSG_ON_SCAN, new Object[]{device, rssi, scanRecord});
        }
    };
    private boolean mIsScanning;
    @Nullable
    private ScanCallback mScanCallback;
    @NonNull
    private final Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            _stopScan(Reason.Timeout);
        }
    };

    public CBScanner(
            @NonNull Context context,
            @NonNull ScanListener scanListener,
            @Nullable Looper looper) {
        if (null == looper) {
            HandlerThread thread = new HandlerThread("ScannerThread");
            thread.start();
            looper = thread.getLooper();
        }
        mHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                Object[] objects = (Object[]) msg.obj;
                _onLeScan((BluetoothDevice) objects[0], (int) objects[1], (byte[]) objects[2]);
            }
        };

        mScanListener = scanListener;
        BluetoothManager btm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = btm.getAdapter();

        context.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
                        int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                        if (BluetoothAdapter.STATE_OFF != bluetoothState) {
                            return;
                        }
                        if (mHandler.isCurrentThread()) {
                            _stopScan(Reason.PoweredOff);
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    _stopScan(Reason.PoweredOff);
                                }
                            });
                        }
                    }
                },
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    public boolean isScanning() {
        boolean ret;
        if (mHandler.isCurrentThread()) {
            ret = mIsScanning;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mIsScanning);
                    callback.unlock();
                }
            });
            callback.lock();
            ret = (boolean) callback.getResult();
        }
        return ret;
    }

    public void scanForPeripherals(@NonNull final List<CBUUID> serviceUUIDs, final int timeout) {
        if (mHandler.isCurrentThread()) {
            _startScan(serviceUUIDs, timeout);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    _startScan(serviceUUIDs, timeout);
                }
            });
        }
    }

    public void stopScan() {
        if (mHandler.isCurrentThread()) {
            _stopScan(Reason.StopRequest);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    _stopScan(Reason.StopRequest);
                }
            });
        }
    }

    private void _startScan(@NonNull final List<CBUUID> serviceUUIDs, int timeout) {
        CBLog.vMethodIn(serviceUUIDs.toString());

        if (BluetoothAdapter.STATE_ON != mBluetoothAdapter.getState()) {
            mScanListener.onScanStartFailure(Reason.PoweredOff);
            return;
        }

        if (mIsScanning) {
            mScanListener.onScanStartFailure(Reason.AlreadyScanning);
            return;
        }

        List<UUID> androidUUIDs = new ArrayList<>();
        for (CBUUID serviceUUID : serviceUUIDs) {
            androidUUIDs.add(serviceUUID.androidUUID());
        }

        boolean result;
        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            result = startOldScan(androidUUIDs, mLeScanCallback);
        } else {
            result = startNewScan(androidUUIDs);
        }
        if (!result) {
            mScanListener.onScanStartFailure(Reason.OSNativeError);
            return;
        }

        mHandler.removeMessages(MSG_ON_SCAN);

        if (0 < timeout) {
            mHandler.postDelayed(mTimeoutRunnable, timeout);
        }

        mIsScanning = true;
        mScanListener.onScanStarted();
    }

    private void _stopScan(Reason reason) {
        CBLog.vMethodIn(reason.name());

        if (!mIsScanning) {
            return;
        }

        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            stopOldScan(mLeScanCallback);
        } else {
            stopNewScan();
        }

        mHandler.removeCallbacks(mTimeoutRunnable);

        mIsScanning = false;
        mScanListener.onScanStopped(reason);
    }

    private void _onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        if (!mIsScanning) {
            CBLog.w("Already stopped.");
            return;
        }
        mScanListener.onScan(device, rssi, scanRecord);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
    private boolean startOldScan(@NonNull final List<UUID> serviceUUIDs, @NonNull BluetoothAdapter.LeScanCallback leScanCallback) {
        boolean ret;
        CBLog.iOsApi("startLeScan() exec.");
        if (serviceUUIDs.isEmpty()) {
            ret = mBluetoothAdapter.startLeScan(leScanCallback);
        } else {
            ret = mBluetoothAdapter.startLeScan(serviceUUIDs.toArray(new UUID[0]), leScanCallback);
        }
        if (ret) {
            CBLog.d("startLeScan() called. ret=true");
        } else {
            CBLog.e("startLeScan() called. ret=false");
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
    private void stopOldScan(@NonNull BluetoothAdapter.LeScanCallback leScanCallback) {
        CBLog.iOsApi("stopLeScan() exec.");
        mBluetoothAdapter.stopLeScan(leScanCallback);
        CBLog.d("stopLeScan() called.");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean startNewScan(@NonNull final List<UUID> serviceUUIDs) {
        if (null != mScanCallback) {
            CBLog.e("null != mScanCallback");
            return false;
        }
        if (null == mBluetoothAdapter.getBluetoothLeScanner()) {
            CBLog.e("null == mBluetoothAdapter.getBluetoothLeScanner()");
            return false;
        }
        boolean ret;
        List<ScanFilter> filters = new ArrayList<>();
        for (UUID serviceUuid : serviceUUIDs) {
            ParcelUuid parcelUuid = new ParcelUuid(serviceUuid);
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(parcelUuid)
                    .build();
            filters.add(filter);
        }
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (null == result.getScanRecord()) {
                    mLeScanCallback.onLeScan(result.getDevice(), result.getRssi(), null);
                } else {
                    mLeScanCallback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        CBLog.iOsApi("scanForPeripherals() exec.");
        try {
            mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
            CBLog.d("scanForPeripherals() called. ret=true");
            mScanCallback = scanCallback;
            ret = true;
        } catch (Exception e) {
            CBLog.e(e.getMessage());
            CBLog.e("scanForPeripherals() called. ret=false");
            ret = false;
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopNewScan() {
        if (null == mScanCallback) {
            CBLog.e("null == mScanCallback");
            return;
        }
        if (null != mBluetoothAdapter.getBluetoothLeScanner()) {
            CBLog.iOsApi("stopScan() exec.");
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            CBLog.d("stopScan() called.");
        } else {
            CBLog.e("null == mBluetoothAdapter.getBluetoothLeScanner()");
        }
        mScanCallback = null;
    }

    public enum Reason {
        PoweredOff,
        AlreadyScanning,
        OSNativeError,
        StopRequest,
        Timeout
    }

    abstract static class ScanListener {
        void onScanStarted() {
        }

        /**
         * @see Reason#PoweredOff
         * @see Reason#AlreadyScanning
         * @see Reason#OSNativeError
         */
        void onScanStartFailure(@NonNull Reason reason) {
        }

        /**
         * @see Reason#PoweredOff
         * @see Reason#StopRequest
         * @see Reason#Timeout
         */
        void onScanStopped(@NonNull Reason reason) {
        }

        void onScan(@NonNull BluetoothDevice device, int rssi, @NonNull byte[] scanRecord) {
        }
    }
}
