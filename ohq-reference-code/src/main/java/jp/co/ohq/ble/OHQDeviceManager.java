package jp.co.ohq.ble;

import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.UUIDs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jp.co.ohq.androidcorebluetooth.CBCentralManager;
import jp.co.ohq.androidcorebluetooth.CBCentralManagerDelegate;
import jp.co.ohq.androidcorebluetooth.CBManagerState;
import jp.co.ohq.androidcorebluetooth.CBPeripheral;
import jp.co.ohq.androidcorebluetooth.CBPeripheralDetailedState;
import jp.co.ohq.androidcorebluetooth.CBPeripheralState;
import jp.co.ohq.androidcorebluetooth.CBUUID;
import jp.co.ohq.ble.advertising.ADPayloadParser;
import jp.co.ohq.ble.advertising.EachUserData;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDataType;
import jp.co.ohq.ble.enumerate.OHQDetailedState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQDeviceInfoKey;
import jp.co.ohq.ble.enumerate.OHQDeviceManagerState;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.SynchronizeCallback;
import jp.co.ohq.utility.Types;

public class OHQDeviceManager {

    public static final int DEFAULT_CONSENT_CODE = 0x020E;

    @Nullable
    private static OHQDeviceManager sInstance;
    @NonNull
    private final Handler mHandler;
    @NonNull
    private final CBCentralManager mCBCentralManager;
    @NonNull
    private final LinkedHashMap<CBPeripheral, SessionInfo> mSessionInfoList = new LinkedHashMap<>();
    @Nullable
    private ScanObserverBlock mScanObserverBlock;
    @Nullable
    private CompletionBlock mScanCompletionBlock;
    @Nullable
    private StateMonitor mStateMonitor;

    private OHQDeviceManager(@NonNull Context context) {
        final HandlerThread thread = new HandlerThread(getClass().getSimpleName());
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mCBCentralManager = _initCentralManager(context, mHandler.getLooper());
    }

    @NonNull
    public static OHQDeviceManager init(@NonNull Context context) {
        if (null != sInstance) {
            throw new IllegalStateException("An instance has already been created.");
        }
        return sInstance = new OHQDeviceManager(context);
    }

    @NonNull
    public static OHQDeviceManager sharedInstance() {
        if (null == sInstance) {
            throw new IllegalStateException("Instance has not been created.");
        }
        return sInstance;
    }

    public void setStateMonitor(@Nullable final StateMonitor stateMonitor) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mStateMonitor = stateMonitor;
            }
        });
    }

    @NonNull
    public OHQDeviceManagerState state() {
        final OHQDeviceManagerState ret;
        if (mHandler.isCurrentThread()) {
            ret = OHQDeviceManagerState.valueOf(mCBCentralManager.state());
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(OHQDeviceManagerState.valueOf(mCBCentralManager.state()));
                    callback.unlock();
                }
            });
            callback.lock();
            ret = Types.autoCast(callback.getResult());
        }
        return ret;
    }

    public Bundle getDefaultConfig(@Nullable final List<OHQConfig.Key> keys) {
        final Bundle config;
        if (mHandler.isCurrentThread()) {
            config = mCBCentralManager.getDefaultConfig(keys);
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mCBCentralManager.getDefaultConfig(keys));
                    callback.unlock();
                }
            });
            callback.lock();
            config = Types.autoCast(callback.getResult());
        }
        return config;
    }

    public Bundle getConfig(@Nullable final List<OHQConfig.Key> keys) {
        final Bundle config;
        if (mHandler.isCurrentThread()) {
            config = mCBCentralManager.getConfig(keys);
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mCBCentralManager.getConfig(keys));
                    callback.unlock();
                }
            });
            callback.lock();
            config = Types.autoCast(callback.getResult());
        }
        return config;
    }

    public void setConfig(@NonNull final Bundle config) {
        if (mHandler.isCurrentThread()) {
            mCBCentralManager.setConfig(config);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCBCentralManager.setConfig(config);
                }
            });
        }
    }

    public void scanForDevicesWithCategories(
            @NonNull final List<OHQDeviceCategory> categories,
            @NonNull final ScanObserverBlock observer,
            @NonNull final CompletionBlock completion) {
        OHQLog.vMethodIn();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _scanForDevicesWithCategories(categories, observer, completion);
            }
        });
    }

    public void stopScan() {
        OHQLog.vMethodIn();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _stopScan(OHQCompletionReason.Canceled);
            }
        });
    }

    public void startSessionWithDevice(
            @NonNull final String address,
            @NonNull final DataObserverBlock dataObserver,
            @NonNull final ConnectionObserverBlock connectionObserver,
            @NonNull final CompletionBlock completion,
            @NonNull final Map<OHQSessionOptionKey, Object> options) {
        OHQLog.vMethodIn();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _startSessionWithDevice(address, dataObserver, connectionObserver, completion, options, null);
            }
        });
    }

    public void startSessionWithDevice(
            @NonNull final String address,
            @NonNull final DataObserverBlock dataObserver,
            @NonNull final ConnectionObserverBlock connectionObserver,
            @NonNull final CompletionBlock completion,
            @NonNull final Map<OHQSessionOptionKey, Object> options,
            @NonNull final DebugMonitor debugMonitor) {
        OHQLog.vMethodIn();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _startSessionWithDevice(address, dataObserver, connectionObserver, completion, options, debugMonitor);
            }
        });
    }

    public void cancelSessionWithDevice(@NonNull final String address) {
        OHQLog.vMethodIn();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _cancelSessionWithDevice(address, OHQCompletionReason.Canceled);
            }
        });
    }

    private CBCentralManager _initCentralManager(@NonNull Context context, @NonNull Looper looper) {
        final CBCentralManagerDelegate delegate = new CBCentralManagerDelegate() {
            @Override
            public void didConnect(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral) {
                _didConnect(peripheral);
            }

            @Override
            public void didDisconnectPeripheral(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral) {
                _didDisconnectPeripheral(peripheral);
            }

            @Override
            public void didFailToConnect(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral) {
                _didFailToConnect(peripheral);
            }

            @Override
            public void didDiscover(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull byte[] advertisementData, int rssi) {
                _didDiscover(peripheral, advertisementData, rssi);
            }

            @Override
            public void centralManagerDidUpdateState(@NonNull CBCentralManager central, @NonNull CBManagerState newState) {
                _didStateChanged(newState);
            }

            @Override
            public void onConnectionStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheralState newState) {
            }

            @Override
            public void onDetailedStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheralDetailedState newState) {
                if (null != mSessionInfoList.get(peripheral)) {
                    OHQDetailedState detailedState = _convertDetailedState(newState);
                    if (null != detailedState) {
                        mSessionInfoList.get(peripheral).mDebugMonitor.onDetailedStateChanged(detailedState);
                    }
                }
            }

            @Override
            public void onPairingRequest(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral) {
                if (null != mSessionInfoList.get(peripheral)) {
                    mSessionInfoList.get(peripheral).mDebugMonitor.onPairingRequest();
                }
            }

            @Override
            public void onBondStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheral.BondState bondState) {
                if (null != mSessionInfoList.get(peripheral)) {
                    mSessionInfoList.get(peripheral).mDebugMonitor.onBondStateChanged(bondState);
                }
            }

            @Override
            public void onAclConnectionStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheral.AclConnectionState aclConnectionState) {
                if (null != mSessionInfoList.get(peripheral)) {
                    mSessionInfoList.get(peripheral).mDebugMonitor.onAclConnectionStateChanged(aclConnectionState);
                }
            }

            @Override
            public void onGattConnectionStateChanged(@NonNull CBCentralManager central, @NonNull CBPeripheral peripheral, @NonNull CBPeripheral.GattConnectionState gattConnectionState, int status) {
                if (null != mSessionInfoList.get(peripheral)) {
                    mSessionInfoList.get(peripheral).mDebugMonitor.onGattConnectionStateChanged(gattConnectionState);
                }
            }
        };
        return new CBCentralManager(context, delegate, looper);
    }

    private void _scanForDevicesWithCategories(
            @NonNull final List<OHQDeviceCategory> categories,
            @NonNull final ScanObserverBlock observer,
            @NonNull final CompletionBlock completion) {
        OHQLog.vMethodIn(categories.toString());

        // Check the Bluetooth state.
        if (CBManagerState.PoweredOn != mCBCentralManager.state()) {
            _executeCompletionBlock(completion, OHQCompletionReason.PoweredOff);
            return;
        }

        // Check the scanning.
        if (null != mScanCompletionBlock) {
            _executeCompletionBlock(completion, OHQCompletionReason.Busy);
            return;
        }

        mScanObserverBlock = new ScanObserverBlock() {
            @Override
            public void onDiscoveredDevice(@NonNull Map<OHQDeviceInfoKey, Object> deviceInfo) {
                OHQDeviceCategory deviceCategory = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.CategoryKey));
                if (!categories.isEmpty() && !categories.contains(deviceCategory)) {
                    OHQLog.w("Not covered device category.");
                    return;
                }
                observer.onDiscoveredDevice(deviceInfo);
            }
        };

        mScanCompletionBlock = new CompletionBlock() {
            @Override
            public void onSessionComplete(@NonNull final OHQCompletionReason aReason) {
                OHQLog.vMethodIn(aReason.name());
                mScanObserverBlock = null;
                mScanCompletionBlock = null;
                mCBCentralManager.stopScan();
                completion.onSessionComplete(aReason);
            }
        };

        // Start to scan.
        mCBCentralManager.scanForPeripherals(new ArrayList<CBUUID>());
    }

    private void _stopScan(@NonNull OHQCompletionReason reason) {
        OHQLog.vMethodIn();
        if (null != mScanCompletionBlock) {
            mScanCompletionBlock.onSessionComplete(reason);
        }
    }

    private void _startSessionWithDevice(
            @NonNull final String address,
            @NonNull final DataObserverBlock dataObserver,
            @NonNull final ConnectionObserverBlock connectionObserver,
            @NonNull final CompletionBlock completion,
            @NonNull final Map<OHQSessionOptionKey, Object> options,
            @Nullable final DebugMonitor debugMonitor) {
        OHQLog.vMethodIn();

        // Check the Bluetooth state.
        if (CBManagerState.PoweredOn != mCBCentralManager.state()) {
            OHQLog.e("Bluetooth not available.");
            _executeCompletionBlock(completion, OHQCompletionReason.PoweredOff);
            return;
        }

        // Get Peripheral object
        final CBPeripheral peripheral = mCBCentralManager.retrievePeripherals(address);
        if (null == peripheral) {
            OHQLog.e("Peripheral not found.");
            _executeCompletionBlock(completion, OHQCompletionReason.InvalidDeviceIdentifier);
            return;
        }

        // Checks if the connected.
        if (CBPeripheralState.Disconnected != peripheral.state()) {
            OHQLog.e("Bad state.");
            _executeCompletionBlock(completion, OHQCompletionReason.Busy);
            return;
        }

        // Checks if the processing.
        if (mSessionInfoList.containsKey(peripheral)) {
            OHQLog.e("Bad state.");
            _executeCompletionBlock(completion, OHQCompletionReason.Busy);
            return;
        }

        final SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.connectionTimeoutTimer = new Timer(mHandler.getLooper().getThread().getName(), true);
        if (options.containsKey(OHQSessionOptionKey.ConnectionWaitTimeKey)) {
            long connectionWaitTime = Types.autoCast(options.get(OHQSessionOptionKey.ConnectionWaitTimeKey));
            OHQLog.d("connectionWaitTime:" + connectionWaitTime);
            sessionInfo.connectionTimeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (CBPeripheralState.Connecting != peripheral.state()) {
                        return;
                    }
                    _cancelSessionWithDevice(address, OHQCompletionReason.ConnectionTimedOut);
                }
            }, connectionWaitTime);
        }
        sessionInfo.dataObserverBlock = new DataObserverBlock() {
            @Override
            public void onDataReceived(@NonNull final OHQDataType aDataType, @NonNull final Object aData) {
                OHQLog.vMethodIn(aDataType.name());
                dataObserver.onDataReceived(aDataType, aData);
            }
        };
        sessionInfo.connectionObserverBlock = new ConnectionObserverBlock() {
            @Override
            public void onConnectionStateChanged(@NonNull final OHQConnectionState aState) {
                OHQLog.vMethodIn(aState.name());
                connectionObserver.onConnectionStateChanged(aState);
            }
        };
        sessionInfo.completionBlock = new CompletionBlock() {
            @Override
            public void onSessionComplete(@NonNull final OHQCompletionReason aReason) {
                OHQLog.vMethodIn(aReason.name());
                sessionInfo.connectionTimeoutTimer.cancel();
                mSessionInfoList.remove(peripheral);
                completion.onSessionComplete(aReason);
            }
        };
        sessionInfo.options = options;

        sessionInfo.mDebugMonitor = new DebugMonitor() {
            @Override
            public void onDetailedStateChanged(@NonNull final OHQDetailedState detailedState) {
                if (null != debugMonitor) {
                    debugMonitor.onDetailedStateChanged(detailedState);
                }
            }

            @Override
            public void onPairingRequest() {
                if (null != debugMonitor) {
                    debugMonitor.onPairingRequest();
                }
            }

            @Override
            public void onBondStateChanged(@NonNull final CBPeripheral.BondState bondState) {
                if (null != debugMonitor) {
                    debugMonitor.onBondStateChanged(bondState);
                }
            }

            @Override
            public void onAclConnectionStateChanged(@NonNull final CBPeripheral.AclConnectionState aclConnectionState) {
                if (null != debugMonitor) {
                    debugMonitor.onAclConnectionStateChanged(aclConnectionState);
                }
            }

            @Override
            public void onGattConnectionStateChanged(@NonNull final CBPeripheral.GattConnectionState gattConnectionState) {
                if (null != debugMonitor) {
                    debugMonitor.onGattConnectionStateChanged(gattConnectionState);
                }
            }
        };

        mSessionInfoList.put(peripheral, sessionInfo);
        mCBCentralManager.connect(peripheral);
        sessionInfo.connectionObserverBlock.onConnectionStateChanged(OHQConnectionState.Connecting);
    }

    private void _cancelSessionWithDevice(@NonNull final String address, @NonNull final OHQCompletionReason completionReason) {
        OHQLog.vMethodIn(completionReason.name());
        CBPeripheral targetPeripheral = null;
        for (CBPeripheral peripheral : mSessionInfoList.keySet()) {
            if (address.equals(peripheral.getAddress())) {
                targetPeripheral = peripheral;
            }
        }
        if (null == targetPeripheral) {
            OHQLog.e("invalid address");
            return;
        }
        SessionInfo sessionInfo = mSessionInfoList.get(targetPeripheral);
        final CompletionBlock completionBlock = mSessionInfoList.get(targetPeripheral).completionBlock;
        sessionInfo.completionBlock = new CompletionBlock() {
            @Override
            public void onSessionComplete(@NonNull OHQCompletionReason aReason) {
                completionBlock.onSessionComplete(completionReason);
            }
        };
        sessionInfo.connectionObserverBlock.onConnectionStateChanged(OHQConnectionState.Disconnecting);
        mCBCentralManager.cancelPeripheralConnection(targetPeripheral);
    }

    private void _executeCompletionBlock(
            @NonNull final CompletionBlock completion,
            @NonNull final OHQCompletionReason reason) {
        completion.onSessionComplete(reason);
    }

    private void _didStateChanged(@NonNull final CBManagerState newState) {
        OHQLog.vMethodIn(newState.name());
        if (CBManagerState.PoweredOff == newState) {
            _stopScan(OHQCompletionReason.PoweredOff);
        }
        final StateMonitor state = mStateMonitor;
        if (null != state) {
            state.onStateChanged(OHQDeviceManagerState.valueOf(newState));
        }
    }

    private void _didDiscover(@NonNull CBPeripheral peripheral, @NonNull byte[] scanRecord, int rssi) {
        List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanRecord);
        OHQDeviceCategory deviceCategory = _verifyingDeviceCategoryFromData(structures);
        if (OHQDeviceCategory.Unknown == deviceCategory) {
            OHQLog.w("OHQDeviceCategory.Unknown == deviceCategory");
            return;
        }
        Map<OHQDeviceInfoKey, Object> deviceInfo = new HashMap<>();
        deviceInfo.put(OHQDeviceInfoKey.AddressKey, peripheral.getAddress());
        deviceInfo.put(OHQDeviceInfoKey.AdvertisementDataKey, structures);
        deviceInfo.put(OHQDeviceInfoKey.RSSIKey, rssi);
        deviceInfo.put(OHQDeviceInfoKey.CategoryKey, deviceCategory);
        if (null != peripheral.getLocalName()) {
            deviceInfo.put(OHQDeviceInfoKey.LocalNameKey, peripheral.getLocalName());
        } else {
            OHQLog.w("Local name is null.");
        }
        OHQLog.d(deviceInfo.toString());
        if (null != mScanObserverBlock) {
            mScanObserverBlock.onDiscoveredDevice(deviceInfo);
        }
    }

    private void _didConnect(@NonNull final CBPeripheral peripheral) {
        OHQLog.vMethodIn();
        if (!mSessionInfoList.containsKey(peripheral)) {
            OHQLog.e("Invalid peripheral.");
            return;
        }
        final SessionInfo sessionInfo = mSessionInfoList.get(peripheral);
        sessionInfo.connectionTimeoutTimer.cancel();
        sessionInfo.connectionObserverBlock.onConnectionStateChanged(OHQConnectionState.Connected);

        OHQDevice device = new OHQDevice(mHandler.getLooper(), peripheral,
                new OHQDevice.Delegate() {
                    @Override
                    public void dataObserver(@NonNull OHQDataType aDataType, @NonNull Object aData) {
                        sessionInfo.dataObserverBlock.onDataReceived(aDataType, aData);
                    }

                    @Override
                    public void didAbortTransferWithReason(@NonNull OHQCompletionReason aReason) {
                        _abortCommunicationForPeripheral(peripheral, aReason);
                    }

                    @Override
                    public void onStateChanged(@NonNull OHQDetailedState newState) {
                        sessionInfo.mDebugMonitor.onDetailedStateChanged(newState);
                    }
                }, sessionInfo.options);

        device.startTransfer();

        sessionInfo.device = device;
    }

    private void _didFailToConnect(@NonNull CBPeripheral peripheral) {
        OHQLog.vMethodIn();
        if (!mSessionInfoList.containsKey(peripheral)) {
            OHQLog.e("Invalid peripheral.");
            return;
        }
        SessionInfo sessionInfo = mSessionInfoList.get(peripheral);

        sessionInfo.connectionObserverBlock.onConnectionStateChanged(OHQConnectionState.Disconnected);
        sessionInfo.completionBlock.onSessionComplete(OHQCompletionReason.FailedToConnect);
    }

    private void _didDisconnectPeripheral(@NonNull CBPeripheral peripheral) {
        OHQLog.vMethodIn();
        if (!mSessionInfoList.containsKey(peripheral)) {
            OHQLog.e("Invalid peripheral.");
            return;
        }
        SessionInfo sessionInfo = mSessionInfoList.get(peripheral);

        sessionInfo.dataObserverBlock.onDataReceived(OHQDataType.MeasurementRecords, sessionInfo.device.getMeasurementRecords());
        sessionInfo.connectionObserverBlock.onConnectionStateChanged(OHQConnectionState.Disconnected);
        sessionInfo.completionBlock.onSessionComplete(OHQCompletionReason.Disconnected);
    }

    private void _abortCommunicationForPeripheral(@NonNull CBPeripheral peripheral, @NonNull final OHQCompletionReason reason) {
        OHQLog.vMethodIn(reason.name());
        if (!mSessionInfoList.containsKey(peripheral)) {
            OHQLog.e("Invalid peripheral.");
            return;
        }
        SessionInfo sessionInfo = mSessionInfoList.get(peripheral);

        if (CBPeripheralState.Connected != peripheral.state()) {
            OHQLog.e("Bad state.");
            return;
        }

        final CompletionBlock completionBlock = sessionInfo.completionBlock;
        sessionInfo.completionBlock = new CompletionBlock() {
            @Override
            public void onSessionComplete(@NonNull OHQCompletionReason aReason) {
                completionBlock.onSessionComplete(reason);
            }
        };

        sessionInfo.connectionObserverBlock.onConnectionStateChanged(OHQConnectionState.Disconnecting);
        mCBCentralManager.cancelPeripheralConnection(peripheral);
    }

    @NonNull
    private OHQDeviceCategory _verifyingDeviceCategoryFromData(@NonNull List<ADStructure> structures) {
        OHQDeviceCategory deviceCategory = OHQDeviceCategory.Unknown;
        boolean hasEachUserData = false;
        for (ADStructure structure : structures) {
            if (structure instanceof UUIDs) {
                UUIDs uuids = (UUIDs) structure;
                for (UUID androidUUID : uuids.getUUIDs()) {
                    CBUUID uuid = new CBUUID(androidUUID);
                    OHQLog.d(uuid.toString());
                    if (OHQUUIDDefines.Service.BodyComposition.uuid().equals(uuid)) {
                        deviceCategory = OHQDeviceCategory.BodyCompositionMonitor;
                    } else if (OHQUUIDDefines.Service.BloodPressure.uuid().equals(uuid)) {
                        deviceCategory = OHQDeviceCategory.BloodPressureMonitor;
                    } else if (OHQUUIDDefines.Service.WeightScale.uuid().equals(uuid)) {
                        deviceCategory = OHQDeviceCategory.WeightScale;
                    }
                }
            } else if (structure instanceof EachUserData) {
                hasEachUserData = true;
            }
        }
        if (OHQDeviceCategory.WeightScale == deviceCategory && hasEachUserData) {
            deviceCategory = OHQDeviceCategory.BodyCompositionMonitor;
        }
        return deviceCategory;
    }

    private OHQDetailedState _convertDetailedState(CBPeripheralDetailedState src) {
        OHQDetailedState ret = null;
        switch (src) {
            case Unconnected:
                ret = OHQDetailedState.Unconnected;
                break;
            case ConnectStarting:
                ret = OHQDetailedState.ConnectStarting;
                break;
            case PairRemoving:
                ret = OHQDetailedState.PairRemoving;
                break;
            case Pairing:
                ret = OHQDetailedState.Pairing;
                break;
            case GattConnecting:
                ret = OHQDetailedState.GattConnecting;
                break;
            case ServiceDiscovering:
                ret = OHQDetailedState.ServiceDiscovering;
                break;
            case ConnectCanceling:
                ret = OHQDetailedState.ConnectCanceling;
                break;
            case CleanupConnection:
                ret = OHQDetailedState.CleanupConnection;
                break;
            case ConnectionRetryReady:
                ret = OHQDetailedState.ConnectionRetryReady;
                break;
            case ConnectCanceled:
                ret = OHQDetailedState.ConnectCanceled;
                break;
            case ConnectionFailed:
                ret = OHQDetailedState.ConnectionFailed;
                break;
            case Connected:
                ret = OHQDetailedState.CommunicationReady;
                break;
            case Disconnecting:
                ret = OHQDetailedState.Disconnecting;
                break;
            case Disconnected:
                ret = OHQDetailedState.Disconnected;
                break;
            default:
                break;
        }
        return ret;
    }

    public interface ScanObserverBlock {
        void onDiscoveredDevice(@NonNull Map<OHQDeviceInfoKey, Object> deviceInfo);
    }

    public interface DataObserverBlock {
        void onDataReceived(@NonNull OHQDataType aDataType, @NonNull Object aData);
    }

    public interface ConnectionObserverBlock {
        void onConnectionStateChanged(@NonNull OHQConnectionState aState);
    }

    public interface CompletionBlock {
        void onSessionComplete(@NonNull OHQCompletionReason aReason);
    }

    public interface DebugMonitor {
        void onDetailedStateChanged(@NonNull OHQDetailedState newState);

        void onPairingRequest();

        void onBondStateChanged(@NonNull CBPeripheral.BondState bondState);

        void onAclConnectionStateChanged(@NonNull CBPeripheral.AclConnectionState aclConnectionState);

        void onGattConnectionStateChanged(@NonNull CBPeripheral.GattConnectionState gattConnectionState);
    }

    public interface StateMonitor {
        void onStateChanged(@NonNull OHQDeviceManagerState state);
    }

    private static class SessionInfo {
        Timer connectionTimeoutTimer;
        DataObserverBlock dataObserverBlock;
        ConnectionObserverBlock connectionObserverBlock;
        CompletionBlock completionBlock;
        Map<OHQSessionOptionKey, Object> options;
        DebugMonitor mDebugMonitor;

        OHQDevice device;
    }
}
