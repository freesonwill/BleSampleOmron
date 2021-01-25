package jp.co.ohq.blesampleomron.controller;

import android.support.annotation.NonNull;

import jp.co.ohq.ble.OHQDeviceManager;
import jp.co.ohq.ble.enumerate.OHQDeviceManagerState;
import jp.co.ohq.utility.Handler;

public class BluetoothPowerController {

    @NonNull
    private final Handler mHandler;
    @NonNull
    private final Listener mListener;
    private boolean mState;

    public BluetoothPowerController(@NonNull Listener listener) {
        mHandler = new Handler();
        mListener = listener;
        mState = _convertState(OHQDeviceManager.sharedInstance().state());
    }

    public boolean state() {
        return mState;
    }

    public void onResume() {
        mState = _convertState(OHQDeviceManager.sharedInstance().state());
        OHQDeviceManager.sharedInstance().setStateMonitor(new OHQDeviceManager.StateMonitor() {
            @Override
            public void onStateChanged(@NonNull final OHQDeviceManagerState state) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        _onStateChanged(state);
                    }
                });
            }
        });
    }

    public void onPause() {
        OHQDeviceManager.sharedInstance().setStateMonitor(null);
    }

    private void _onStateChanged(@NonNull final OHQDeviceManagerState state) {
        if (mState == _convertState(state)) {
            return;
        }
        mState = !mState;
        mListener.onBluetoothStateChanged(mState);
    }

    private boolean _convertState(@NonNull final OHQDeviceManagerState state) {
        boolean enable;
        switch (state) {
            case PoweredOn:
                enable = true;
                break;
            case PoweredOff:
                enable = false;
                break;
            default:
                enable = false;
                break;
        }
        return enable;
    }

    public interface Listener {
        void onBluetoothStateChanged(boolean enable);
    }
}
