package jp.co.ohq.blesampleomron.view.fragment;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import jp.co.ohq.ble.advertising.EachUserData;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.BluetoothPowerController;
import jp.co.ohq.blesampleomron.controller.ScanController;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.controller.util.Common;
import jp.co.ohq.blesampleomron.model.entity.DeviceInfo;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.adapter.AbstractListAdapter;
import jp.co.ohq.blesampleomron.view.dialog.SimpleDialog;
import jp.co.ohq.utility.Bundler;

public class DiscoveredDevicesFragment extends BaseFragment implements
        AdapterView.OnItemClickListener,
        SimpleDialog.Callback,
        BluetoothPowerController.Listener,
        ScanController.Listener {

    private static final String ARG_ONLY_PAIRING_MODE = "ARG_ONLY_PAIRING_MODE";
    private static final String ARG_INFO_BUTTON_VISIBILITY = "ARG_INFO_BUTTON_VISIBILITY";
    private static final String ARG_INFORM_OF_REGISTERED_DEVICE = "ARG_INFORM_OF_REGISTERED_DEVICE";
    private static final int DIALOG_REQ_CODE_FILTER_LIST = 0;
    private BluetoothPowerController mBluetoothPowerController;
    private ScanController mScanController;
    private boolean mIsOnlyPairingMode;
    private OHQDeviceCategory mFilteringDeviceCategory;
    private DiscoveredDeviceListAdapter mDiscoveredDeviceListAdapter;
    private EventListener mListener;
    private TextView mEmptyTextView;
    private Realm mRealm;

    public static DiscoveredDevicesFragment newInstance(boolean infoButtonEnabled, boolean onlyPairingMode, boolean compareRegisteredDevices) {
        DiscoveredDevicesFragment fragment = new DiscoveredDevicesFragment();
        fragment.setArguments(Bundler.bundle(
                ARG_ONLY_PAIRING_MODE, onlyPairingMode,
                ARG_INFO_BUTTON_VISIBILITY, infoButtonEnabled,
                ARG_INFORM_OF_REGISTERED_DEVICE, compareRegisteredDevices
        ));
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AppLog.vMethodIn();
        super.onAttach(context);
        if (null != getParentFragment() && getParentFragment() instanceof EventListener) {
            mListener = (EventListener) getParentFragment();
        } else if (context instanceof EventListener) {
            mListener = (EventListener) context;
        } else {
            throw new AndroidRuntimeException("Parent is must be implement 'EventListener'");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        mBluetoothPowerController = new BluetoothPowerController(this);
        mScanController = new ScanController(this);

        mIsOnlyPairingMode = getArguments().getBoolean(ARG_ONLY_PAIRING_MODE, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        return inflater.inflate(R.layout.fragment_discoved_devices, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mEmptyTextView = (TextView) view.findViewById(R.id.emptyTextView);
        ListView deviceList = (ListView) view.findViewById(R.id.list);
        deviceList.setEmptyView(mEmptyTextView);
        deviceList.setOnItemClickListener(this);
        mDiscoveredDeviceListAdapter = new DiscoveredDeviceListAdapter(getContext(), mRealm);
        mDiscoveredDeviceListAdapter.setInfoButtonVisibility(getArguments().getBoolean(ARG_INFO_BUTTON_VISIBILITY, false));
        mDiscoveredDeviceListAdapter.setInformOfRegisteredDevice(getArguments().getBoolean(ARG_INFORM_OF_REGISTERED_DEVICE, false));
        deviceList.setAdapter(mDiscoveredDeviceListAdapter);
    }

    @Override
    public void onResume() {
        AppLog.vMethodIn();
        super.onResume();
        mBluetoothPowerController.onResume();
        mScanController.onResume();

        refreshView(mBluetoothPowerController.state());
        if (mBluetoothPowerController.state()) {
            mScanController.startScan();
        }
    }

    @Override
    public void onPause() {
        AppLog.vMethodIn();
        super.onPause();
        mBluetoothPowerController.onPause();
        mScanController.onPause();

        mScanController.stopScan();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_device_discovery, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.select_filter);
        Drawable icon = DrawableCompat.wrap(item.getIcon());
        DrawableCompat.setTint(icon, ContextCompat.getColor(getContext(), Common.getDeviceCategoryColorResource(mFilteringDeviceCategory)));
        DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN);
        mDiscoveredDeviceListAdapter.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.select_filter) {
            new SimpleDialog.Builder(this)
                    .items(
                            getString(R.string.blood_pressure_monitor),
                            getString(R.string.body_composition_monitor),
                            getString(R.string.weight_scale),
                            getString(R.string.no_filter)
                    )
                    .negative(getString(R.string.cancel))
                    .requestCode(DIALOG_REQ_CODE_FILTER_LIST)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppLog.vMethodIn();
        DiscoveredDevice discoverDevice = mDiscoveredDeviceListAdapter.getItem(position);
        switch (view.getId()) {
            case R.id.detailInfoButton:
                mListener.onFragmentEvent(Event.DetailInfoSelected, Bundler.bundle(
                        EventArg.DiscoveredDevice.name(), discoverDevice
                ));
                break;
            default:
                EachUserData data = discoverDevice.getEachUserData();
                if (data != null) {
                    List<EachUserData.User> users = data.getUsers();
                    if (users != null) {
                        for (EachUserData.User user : users) {
                            if (user.numberOfRecords != 0 && user.lastSequenceNumber == 0) {
                                AppLog.d(getString(R.string.equipment_failure));
                                Toast.makeText(getActivity(), R.string.equipment_failure, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                }
                if (getArguments().getBoolean(ARG_INFORM_OF_REGISTERED_DEVICE, false)) {
                    DeviceInfo deviceInfo = mRealm.where(DeviceInfo.class).equalTo(
                            "users.name", AppConfig.sharedInstance().getNameOfCurrentUser()).equalTo("address", discoverDevice.getAddress()).findFirst();
                    if (null != deviceInfo) {
                        break;
                    }
                }
                mListener.onFragmentEvent(Event.DeviceSelected, Bundler.bundle(
                        EventArg.DiscoveredDevice.name(), discoverDevice
                ));
                break;
        }
    }

    @Override
    public void onSimpleDialogSucceeded(int requestCode, int resultCode, Bundle params) {

        if (DIALOG_REQ_CODE_FILTER_LIST != requestCode) {
            throw new AndroidRuntimeException("DIALOG_REQ_CODE_FILTER_LIST != requestCode");
        }

        switch (resultCode) {
            case 0: // Blood Pressure Monitor
                mFilteringDeviceCategory = OHQDeviceCategory.BloodPressureMonitor;
                break;
            case 1: // Body Composition Monitor
                mFilteringDeviceCategory = OHQDeviceCategory.BodyCompositionMonitor;
                break;
            case 2: // Weight Scale
                mFilteringDeviceCategory = OHQDeviceCategory.WeightScale;
                break;
            case 3: // Non
                mFilteringDeviceCategory = null;
                break;
            default:
                return;
        }
        mScanController.setFilteringDeviceCategory(mFilteringDeviceCategory);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSimpleDialogCancelled(int requestCode, Bundle params) {
        AppLog.vMethodIn();
    }

    @Override
    public void onBluetoothStateChanged(boolean enable) {
        refreshView(enable);
        if (enable) {
            mScanController.startScan();
        }
    }

    @Override
    public void onScan(@NonNull List<DiscoveredDevice> discoveredDevices) {
        List<DiscoveredDevice> deviceList = new LinkedList<>();
        if (mIsOnlyPairingMode) {
            for (DiscoveredDevice device : discoveredDevices) {
                EachUserData eachUserData = device.getEachUserData();
                if (null == eachUserData) {
                    deviceList.add(device);
                } else if (eachUserData.isPairingMode()) {
                    deviceList.add(device);
                }
            }
        } else {
            deviceList.addAll(discoveredDevices);
        }
        mDiscoveredDeviceListAdapter.clear();
        mDiscoveredDeviceListAdapter.addAll(deviceList);
    }

    @Override
    public void onScanCompletion(@NonNull OHQCompletionReason reason) {

    }

    private void refreshView(boolean bluetoothPowerState) {
        mDiscoveredDeviceListAdapter.clear();
        if (bluetoothPowerState) {
            getActivity().setTitle(getString(R.string.scanning).toUpperCase());
            if (mIsOnlyPairingMode) {
                mEmptyTextView.setText(getString(R.string.pairing_mode_require));
            } else {
                mEmptyTextView.setText("");
            }
        } else {
            getActivity().setTitle(getString(R.string.scan).toUpperCase());
            mEmptyTextView.setText(getString(R.string.bluetooth_invalid_message));
        }
    }

    public enum Event {
        DeviceSelected,
        DetailInfoSelected,
    }

    public enum EventArg {
        DiscoveredDevice,
    }

    public interface EventListener {
        void onFragmentEvent(Event event, Bundle args);
    }

    private static class DiscoveredDeviceListAdapter extends AbstractListAdapter<DiscoveredDevice> {
        @NonNull
        private final String mCurrentUserName = AppConfig.sharedInstance().getNameOfCurrentUser();
        @NonNull
        private Realm mRealm;
        private boolean mInfoButtonVisible;
        private boolean mInformOfRegisteredDevice;

        DiscoveredDeviceListAdapter(@NonNull Context context, @NonNull Realm realm) {
            super(context);
            mRealm = realm;
        }

        void setInfoButtonVisibility(boolean visible) {
            mInfoButtonVisible = visible;
        }

        void setInformOfRegisteredDevice(boolean enable) {
            mInformOfRegisteredDevice = enable;
        }

        @NonNull
        @Override
        protected View onCreateView(int position, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.list_item_device, parent, false);
            view.findViewById(R.id.newIcon).setVisibility(View.GONE);
            view.findViewById(R.id.userIndex).setVisibility(View.GONE);
            if (!mInfoButtonVisible) {
                view.findViewById(R.id.detailInfoButton).setVisibility(View.GONE);
            }
            ViewHolder holder = new ViewHolder();
            holder.deviceCategoryColorLabel = view.findViewById(R.id.deviceCategoryColorLabel);
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.address = (TextView) view.findViewById(R.id.summary1);
            holder.localName = (TextView) view.findViewById(R.id.summary2);
            holder.standardLabel = (TextView) view.findViewById(R.id.standardLabel);
            holder.omronExLabel = (TextView) view.findViewById(R.id.omronExLabel);
            holder.rssi = (TextView) view.findViewById(R.id.rssi);
            holder.infoButton = (ImageButton) view.findViewById(R.id.detailInfoButton);

            final ListView parentList = (ListView) parent;
            holder.infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    parentList.performItemClick(view, position, view.getId());
                }
            });

            view.setTag(holder);
            return view;
        }

        @Override
        protected void onBindView(final int position, @NonNull View view) {
            DiscoveredDevice device = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.deviceCategoryColorLabel.setBackgroundResource(Common.getDeviceCategoryColorResource(device.getDeviceCategory()));
            if (null != device.getModelName()) {
                holder.title.setText(device.getModelName());
                holder.address.setText(device.getAddress());
            } else {
                holder.title.setText(device.getAddress());
                holder.address.setVisibility(View.GONE);
            }
            if (null != device.getCompleteLocalName()) {
                holder.localName.setText(device.getCompleteLocalName());
            } else {
                holder.localName.setText(device.getLocalName());
            }
            if (!device.isOmronExtensionProtocolSupported()) {
                holder.omronExLabel.setVisibility(View.GONE);
            }
            holder.rssi.setText(String.format(Locale.US, "%d", device.getRssi()));
            holder.infoButton.setTag(position);

            if (mInformOfRegisteredDevice) {
                DeviceInfo deviceInfo = mRealm.where(DeviceInfo.class).equalTo(
                        "users.name", mCurrentUserName).equalTo("address", device.getAddress()).findFirst();
                if (null != deviceInfo) {
                    view.findViewById(R.id.registeredLayout).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.registeredLayout).setVisibility(View.GONE);
                }
            }
        }

        private static class ViewHolder {
            View deviceCategoryColorLabel;
            TextView title;
            TextView address;
            TextView localName;
            TextView standardLabel;
            TextView omronExLabel;
            TextView rssi;
            ImageButton infoButton;
        }
    }
}
