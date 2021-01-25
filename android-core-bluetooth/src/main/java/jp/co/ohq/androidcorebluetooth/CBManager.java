package jp.co.ohq.androidcorebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.SynchronizeCallback;

class CBManager {

    @NonNull
    private final Context mContext;
    @NonNull
    private final Handler mHandler;
    @Nullable
    private final BluetoothAdapter mBluetoothAdapter;
    @NonNull
    private CBManagerState mState;

    CBManager(@NonNull final Context context, @Nullable Looper looper) {
        if (null == looper) {
            final HandlerThread thread = new HandlerThread(getClass().getSimpleName());
            thread.start();
            looper = thread.getLooper();
        }
        mContext = context;
        mHandler = new Handler(looper);
        BluetoothManager btm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (null == btm) {
            mBluetoothAdapter = null;
            mState = CBManagerState.Unsupported;
            return;
        }
        mBluetoothAdapter = btm.getAdapter();
        if (null == mBluetoothAdapter) {
            mState = CBManagerState.Unsupported;
            return;
        }
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mState = CBManagerState.Unsupported;
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mState = CBManagerState.PoweredOn;
        } else {
            mState = CBManagerState.PoweredOff;
        }

        context.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
                        CBLog.vMethodIn();
                        int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                        final BluetoothState bluetoothState = BluetoothState.valueOf(newState);
                        if (mHandler.isCurrentThread()) {
                            _bluetoothStateChanged(bluetoothState);
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    _bluetoothStateChanged(bluetoothState);
                                }
                            });
                        }
                    }
                },
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    public void powerOn() {
        if (mHandler.isCurrentThread()) {
            _powerOn();
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    _powerOn();
                }
            });
        }
    }

    public void powerOff() {
        if (mHandler.isCurrentThread()) {
            _powerOff();
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    _powerOff();
                }
            });
        }
    }

    public CBManagerState state() {
        final CBManagerState ret;
        if (mHandler.isCurrentThread()) {
            ret = mState;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mState);
                    callback.unlock();
                }
            });
            callback.lock();
            ret = (CBManagerState) callback.getResult();
        }
        return ret;
    }

    @NonNull
    protected Context getContext() {
        return mContext;
    }

    @NonNull
    protected Handler getHandler() {
        return mHandler;
    }

    @NonNull
    protected BluetoothAdapter getAdapter() {
        if (null == mBluetoothAdapter) {
            throw new NullPointerException("null == mBluetoothAdapter");
        }
        return mBluetoothAdapter;
    }

    protected void onStateChanged(@NonNull CBManagerState newState) {
    }

    private void _bluetoothStateChanged(@NonNull BluetoothState newState) {
        CBLog.iOsApi("Received ACTION_STATE_CHANGED. newState:" + newState.name());
        CBManagerState previousState = mState;
        if (BluetoothState.On == newState) {
            mState = CBManagerState.PoweredOn;
        } else {
            mState = CBManagerState.PoweredOff;
        }
        if (previousState != mState) {
            onStateChanged(mState);
        }
    }

    private void _powerOn() {
        if (null == mBluetoothAdapter) {
            CBLog.e("null == mBluetoothAdapter");
            return;
        }
        if (CBManagerState.Unsupported == mState) {
            CBLog.e("CBManagerState.Unsupported == mState");
            return;
        }
        mBluetoothAdapter.enable();
    }

    private void _powerOff() {
        if (null == mBluetoothAdapter) {
            CBLog.e("null == mBluetoothAdapter");
            return;
        }
        if (CBManagerState.Unsupported == mState) {
            CBLog.e("CBManagerState.Unsupported == mState");
            return;
        }
        mBluetoothAdapter.disable();
    }

    private enum BluetoothState {
        On(BluetoothAdapter.STATE_ON),
        Off(BluetoothAdapter.STATE_OFF),
        TurningOn(BluetoothAdapter.STATE_TURNING_ON),
        TurningOff(BluetoothAdapter.STATE_TURNING_OFF);
        private int value;

        BluetoothState(int value) {
            this.value = value;
        }

        static BluetoothState valueOf(int value) {
            for (BluetoothState type : values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return BluetoothState.Off;
        }

        int value() {
            return value;
        }
    }
}
