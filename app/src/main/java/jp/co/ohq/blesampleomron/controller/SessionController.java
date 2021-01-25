package jp.co.ohq.blesampleomron.controller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.Map;

import jp.co.ohq.androidcorebluetooth.CBPeripheral;
import jp.co.ohq.ble.OHQDeviceManager;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDataType;
import jp.co.ohq.ble.enumerate.OHQDetailedState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.SessionData;
import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.SynchronizeCallback;
import jp.co.ohq.utility.Types;

public class SessionController implements
        OHQDeviceManager.DataObserverBlock,
        OHQDeviceManager.ConnectionObserverBlock,
        OHQDeviceManager.CompletionBlock,
        OHQDeviceManager.DebugMonitor {

    @NonNull
    private final SessionData mSessionData = new SessionData();
    @NonNull
    private final Handler mHandler;
    @NonNull
    private final Listener mListener;
    @Nullable
    private final OHQDeviceManager.DebugMonitor mDebugMonitor;
    @NonNull
    private final OHQDeviceManager mOHQDeviceManager;
    @Nullable
    private String mSessionAddress;

    public SessionController(@NonNull Listener listener) {
        this(listener, null);
    }

    public SessionController(@NonNull Listener listener, @Nullable OHQDeviceManager.DebugMonitor debugMonitor) {
        AppLog.vMethodIn();
        mHandler = new Handler();
        mListener = listener;
        mDebugMonitor = debugMonitor;
        mOHQDeviceManager = OHQDeviceManager.sharedInstance();
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public boolean isInSession() {
        final boolean ret;
        if (mHandler.isCurrentThread()) {
            ret = null != mSessionAddress;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(null != mSessionAddress);
                    callback.unlock();
                }
            });
            callback.lock();
            ret = Types.autoCast(callback.getResult());
        }
        return ret;
    }

    public void setConfig(@NonNull final Bundle config) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOHQDeviceManager.setConfig(config);
            }
        });
    }

    public void startSession(
            @NonNull final String address,
            @NonNull final Map<OHQSessionOptionKey, Object> option) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _startSession(address, option);
            }
        });
    }

    public void cancel() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _cancelSession();
            }
        });
    }

    @Override
    public void onDataReceived(@NonNull final OHQDataType aDataType, @NonNull final Object aData) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _onDataReceived(aDataType, aData);
            }
        });
    }

    @Override
    public void onConnectionStateChanged(@NonNull final OHQConnectionState aState) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _onConnectionStateChanged(aState);
            }
        });
    }

    @Override
    public void onSessionComplete(@NonNull final OHQCompletionReason aReason) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _onSessionComplete(aReason);
            }
        });
    }

    @Override
    public void onDetailedStateChanged(@NonNull final OHQDetailedState newState) {
        AppLog.vMethodIn(newState.name());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != mDebugMonitor) {
                    mDebugMonitor.onDetailedStateChanged(newState);
                }
            }
        });
    }

    @Override
    public void onPairingRequest() {
        AppLog.vMethodIn();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != mDebugMonitor) {
                    mDebugMonitor.onPairingRequest();
                }
            }
        });
    }

    @Override
    public void onBondStateChanged(@NonNull final CBPeripheral.BondState bondState) {
        AppLog.vMethodIn(bondState.name());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != mDebugMonitor) {
                    mDebugMonitor.onBondStateChanged(bondState);
                }
            }
        });
    }

    @Override
    public void onAclConnectionStateChanged(@NonNull final CBPeripheral.AclConnectionState aclConnectionState) {
        AppLog.vMethodIn(aclConnectionState.name());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != mDebugMonitor) {
                    mDebugMonitor.onAclConnectionStateChanged(aclConnectionState);
                }
            }
        });
    }

    @Override
    public void onGattConnectionStateChanged(@NonNull final CBPeripheral.GattConnectionState gattConnectionState) {
        AppLog.vMethodIn(gattConnectionState.name());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != mDebugMonitor) {
                    mDebugMonitor.onGattConnectionStateChanged(gattConnectionState);
                }
            }
        });
    }

    private void _startSession(
            @NonNull final String address,
            @NonNull final Map<OHQSessionOptionKey, Object> option) {
        AppLog.vMethodIn(address + " " + option.toString());
        if (null != mSessionAddress) {
            AppLog.e("null != mSessionAddress");
            return;
        }
        mSessionData.setOption(option);
        mOHQDeviceManager.startSessionWithDevice(
                address,
                this,
                this,
                this,
                option,
                this);
        mSessionAddress = address;
    }

    private void _cancelSession() {
        AppLog.vMethodIn();
        if (null == mSessionAddress) {
            AppLog.e("null == mSessionAddress");
            return;
        }
        mOHQDeviceManager.cancelSessionWithDevice(mSessionAddress);
        mSessionAddress = null;
    }

    private void _onDataReceived(@NonNull OHQDataType dataType, @NonNull Object data) {
        switch (dataType) {
            case DeviceCategory:
                OHQDeviceCategory deviceCategory = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + deviceCategory.name());
                mSessionData.setDeviceCategory(deviceCategory);
                break;
            case ModelName:
                String modelName = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + modelName);
                mSessionData.setModelName(modelName);
                break;
            case CurrentTime:
                String currentTime = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + currentTime);
                mSessionData.setCurrentTime(currentTime);
                break;
            case BatteryLevel:
                int batteryLevel = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + batteryLevel);
                mSessionData.setBatteryLevel((Integer) data);
                break;
            case RegisteredUserIndex:
                int registeredUserIndex = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + registeredUserIndex);
                mSessionData.setUserIndex(registeredUserIndex);
                break;
            case AuthenticatedUserIndex:
                int authenticatedUserIndex = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + authenticatedUserIndex);
                mSessionData.setUserIndex(authenticatedUserIndex);
                break;
            case DeletedUserIndex:
                int deletedUserIndex = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + deletedUserIndex);
                mSessionData.setUserIndex(deletedUserIndex);
                break;
            case UserData:
                Map<OHQUserDataKey, Object> userData = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + userData.toString());
                mSessionData.setUserData(userData);
                break;
            case DatabaseChangeIncrement:
                long databaseChangeIncrement = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + databaseChangeIncrement);
                mSessionData.setDatabaseChangeIncrement(databaseChangeIncrement);
                break;
            case SequenceNumberOfLatestRecord:
                int sequenceNumberOfLatestRecord = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + sequenceNumberOfLatestRecord);
                mSessionData.setSequenceNumberOfLatestRecord(sequenceNumberOfLatestRecord);
                break;
            case MeasurementRecords:
                LinkedList<Map<OHQMeasurementRecordKey, Object>> measurementRecords = Types.autoCast(data);
                AppLog.d(dataType.name() + " " + measurementRecords.toString());
                mSessionData.setMeasurementRecords(measurementRecords);
                break;
            default:
                break;
        }
    }

    private void _onConnectionStateChanged(@NonNull OHQConnectionState connectionState) {
        AppLog.vMethodIn(connectionState.name());
        mListener.onConnectionStateChanged(connectionState);
    }

    private void _onSessionComplete(@NonNull OHQCompletionReason completionReason) {
        AppLog.vMethodIn(completionReason.name());
        mSessionAddress = null;
        mSessionData.setCompletionReason(completionReason);
        mListener.onSessionComplete(mSessionData);
    }

    public interface Listener {
        void onConnectionStateChanged(@NonNull OHQConnectionState connectionState);

        void onSessionComplete(@NonNull SessionData sessionData);
    }
}
