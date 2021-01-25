package jp.co.ohq.blesampleomron.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;

import com.neovisionaries.bluetooth.ble.advertising.ADStructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.OHQDeviceManager;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQDeviceInfoKey;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.Types;

public class ScanController {

    private static final long BATCHED_SCAN_INTERVAL = 1000;

    @NonNull
    private final Handler mHandler;
    @NonNull
    private final Listener mListener;
    @NonNull
    private final OHQDeviceManager mOHQDeviceManager;
    @NonNull
    private final LinkedHashMap<String, DiscoveredDevice> mDiscoveredDevices = new LinkedHashMap<>();
    @Nullable
    private OHQDeviceCategory mFilteringDeviceCategory;
    private boolean mIsScanning;
    @NonNull
    private final Runnable mBatchedScanRunnable = new Runnable() {
        @Override
        public void run() {
            _onBatchedScan(new LinkedList<>(mDiscoveredDevices.values()));
            mHandler.postDelayed(this, BATCHED_SCAN_INTERVAL);
        }
    };
    private boolean mHasRestartRequest;

    public ScanController(@NonNull Listener listener) {
        mHandler = new Handler();
        mListener = listener;
        mOHQDeviceManager = OHQDeviceManager.sharedInstance();
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void setFilteringDeviceCategory(@Nullable final OHQDeviceCategory deviceCategory) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _setFilteringDeviceCategory(deviceCategory);
            }
        });
    }

    public void startScan() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _startScan(mFilteringDeviceCategory);
            }
        });
    }

    public void stopScan() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _stopScan();
            }
        });
    }

    private void _setFilteringDeviceCategory(@Nullable final OHQDeviceCategory deviceCategory) {
        if (null != deviceCategory) {
            AppLog.vMethodIn(deviceCategory.name());
        } else {
            AppLog.vMethodIn("null");
        }
        mFilteringDeviceCategory = deviceCategory;
        if (mIsScanning) {
            mHasRestartRequest = true;
            _stopScan();
        }
    }

    private void _startScan(@Nullable OHQDeviceCategory filteringDeviceCategory) {
        AppLog.vMethodIn();

        if (mIsScanning) {
            AppLog.e("Already scanning.");
            return;
        }

        List<OHQDeviceCategory> scanFilter = new ArrayList<>();
        if (null != filteringDeviceCategory) {
            AppLog.d("filteringDeviceCategory:" + filteringDeviceCategory);
            scanFilter.add(filteringDeviceCategory);
        }

        mOHQDeviceManager.scanForDevicesWithCategories(
                scanFilter,
                new OHQDeviceManager.ScanObserverBlock() {
                    @Override
                    public void onDiscoveredDevice(@NonNull final Map<OHQDeviceInfoKey, Object> deviceInfo) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                _onScan(deviceInfo);
                            }
                        });
                    }
                },
                new OHQDeviceManager.CompletionBlock() {
                    @Override
                    public void onSessionComplete(@NonNull final OHQCompletionReason reason) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                _onScanCompletion(reason);
                            }
                        });
                    }
                });

        mIsScanning = true;
        mDiscoveredDevices.clear();
        mHandler.postDelayed(mBatchedScanRunnable, BATCHED_SCAN_INTERVAL);
    }

    private void _stopScan() {
        AppLog.vMethodIn();

        if (!mIsScanning) {
            return;
        }

        mOHQDeviceManager.stopScan();
    }

    private void _onScan(@NonNull final Map<OHQDeviceInfoKey, Object> deviceInfo) {
        if (!mIsScanning) {
            AppLog.e("Scanning is stopped.");
            return;
        }

        final String address;
        if (!deviceInfo.containsKey(OHQDeviceInfoKey.AddressKey)) {
            throw new AndroidRuntimeException("The address must be present.");
        }
        if (null == (address = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.AddressKey)))) {
            throw new AndroidRuntimeException("The address must be present.");
        }

        final DiscoveredDevice discoveredDevice;
        if (mDiscoveredDevices.containsKey(address)) {
            AppLog.d("Update discovered device. " + address);
            discoveredDevice = mDiscoveredDevices.get(address);
        } else {
            AppLog.d("New discovered device. " + address);
            discoveredDevice = new DiscoveredDevice(address);
        }

        if (deviceInfo.containsKey(OHQDeviceInfoKey.AdvertisementDataKey)) {
            List<ADStructure> advertisementData = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.AdvertisementDataKey));
            discoveredDevice.setAdvertisementData(advertisementData);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.CategoryKey)) {
            OHQDeviceCategory deviceCategory = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.CategoryKey));
            discoveredDevice.setDeviceCategory(deviceCategory);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.RSSIKey)) {
            int rssi = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.RSSIKey));
            discoveredDevice.setRssi(rssi);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.ModelNameKey)) {
            String modelName = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.ModelNameKey));
            discoveredDevice.setModelName(modelName);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.LocalNameKey)) {
            String localName = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.LocalNameKey));
            discoveredDevice.setLocalName(localName);
        }

        mDiscoveredDevices.put(address, discoveredDevice);
    }

    private void _onScanCompletion(@NonNull final OHQCompletionReason reason) {
        AppLog.vMethodIn(reason.name());

        mHandler.removeCallbacks(mBatchedScanRunnable);
        mIsScanning = false;

        if (mHasRestartRequest) {
            mHasRestartRequest = false;
            _startScan(mFilteringDeviceCategory);
        } else {
            mListener.onScanCompletion(reason);
        }
    }

    private void _onBatchedScan(@NonNull List<DiscoveredDevice> discoveredDevices) {
        if (!mIsScanning) {
            AppLog.e("Scanning is stopped.");
            return;
        }
        AppLog.d(discoveredDevices.toString());
        mListener.onScan(discoveredDevices);
    }

    public interface Listener {
        void onScan(@NonNull List<DiscoveredDevice> discoveredDevices);
        void onScanCompletion(@NonNull final OHQCompletionReason reason);
    }
}
