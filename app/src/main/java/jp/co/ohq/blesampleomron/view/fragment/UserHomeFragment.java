package jp.co.ohq.blesampleomron.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import jp.co.ohq.blesampleomron.model.entity.UserInfo;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.adapter.AbstractListAdapter;
import jp.co.ohq.blesampleomron.view.dialog.SimpleDialog;
import jp.co.ohq.utility.Bundler;

import static jp.co.ohq.blesampleomron.R.id.deviceCategoryColorLabel;

public class UserHomeFragment extends BaseFragment implements
        AdapterView.OnItemClickListener,
        SimpleDialog.Callback,
        BluetoothPowerController.Listener,
        ScanController.Listener {

    private static final int NO_RECORD = 0;

    private static final int DIALOG_REQ_CODE_TRANSFER = 0;
    private Realm mRealm;
    private EventListener mListener;
    private ListAdapter mListAdapter;
    private BluetoothPowerController mBluetoothPowerController;
    private ScanController mScanController;

    public static UserHomeFragment newInstance() {
        return new UserHomeFragment();
    }

    @Override
    public void onAttach(Context context) {
        AppLog.vMethodIn();
        super.onAttach(context);

        if (null != getParentFragment() && getParentFragment() instanceof UserHomeFragment.EventListener) {
            mListener = (UserHomeFragment.EventListener) getParentFragment();
        } else if (context instanceof UserHomeFragment.EventListener) {
            mListener = (UserHomeFragment.EventListener) context;
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
    }

    @Override
    public void onDestroy() {
        AppLog.vMethodIn();
        super.onDestroy();
        mRealm.close();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_home, container, false);

        ListView list = (ListView) rootView.findViewById(R.id.list);
        list.setOnItemClickListener(this);
        mListAdapter = new ListAdapter(getActivity());
        list.setAdapter(mListAdapter);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentEvent(Event.AddDevice, new Bundle());
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        AppLog.vMethodIn();
        super.onResume();
        mBluetoothPowerController.onResume();
        mScanController.onResume();

        refreshList();
        if (mBluetoothPowerController.state() && !mListAdapter.isEmpty()) {
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppLog.vMethodIn();
        ListItem item = mListAdapter.getItem(position);
        Bundle args = Bundler.bundle(
                EventArg.DeviceAddress.name(), item.address
        );
        switch (view.getId()) {
            case R.id.detailInfoButton:
                mListener.onFragmentEvent(Event.DeviceInfo, args);
                break;
            default:
                if (item.isBreakdown) {
                    AppLog.d(getString(R.string.equipment_failure));
                    Toast.makeText(getActivity(), R.string.equipment_failure, Toast.LENGTH_SHORT).show();
                    break;
                }
                new SimpleDialog.Builder(this)
                        .message(getString(R.string.transfer_message))
                        .positive(getString(R.string.ok))
                        .negative(getString(R.string.cancel))
                        .requestCode(DIALOG_REQ_CODE_TRANSFER)
                        .params(args)
                        .show();
                break;
        }
    }

    @Override
    public void onSimpleDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        switch (requestCode) {
            case DIALOG_REQ_CODE_TRANSFER:
                if (DialogInterface.BUTTON_POSITIVE == resultCode) {
                    mListener.onFragmentEvent(Event.TransferToDevice, params);
                }
                break;
        }
    }

    @Override
    public void onSimpleDialogCancelled(int requestCode, Bundle params) {
        AppLog.vMethodIn();
    }

    @Override
    public void onBluetoothStateChanged(boolean enable) {
        AppLog.vMethodIn("enable : " + enable);
        if (enable && !mListAdapter.isEmpty()) {
            mScanController.startScan();
        }
    }

    @Override
    public void onScan(@NonNull List<DiscoveredDevice> discoveredDevices) {
        AppLog.vMethodIn();
        updateListWithDiscoverDevices(discoveredDevices);
    }

    @Override
    public void onScanCompletion(@NonNull OHQCompletionReason reason) {

    }

    private void refreshList() {
        if (AppConfig.sharedInstance().getNameOfCurrentUser().equals(AppConfig.GUEST)) {
            return;
        }
        UserInfo userInfo = mRealm.where(UserInfo.class)
                .equalTo("name", AppConfig.sharedInstance().getNameOfCurrentUser()).findFirst();
        List<ListItem> mItems = new LinkedList<>();
        for (DeviceInfo deviceInfo : userInfo.getRegisteredDevices()) {
            mItems.add(new ListItem(deviceInfo));
        }
        mListAdapter.clear();
        mListAdapter.addAll(mItems);
    }

    private void updateListWithDiscoverDevices(@NonNull List<DiscoveredDevice> discoverDevices) {
        for (ListItem item : mListAdapter.getItems()) {
            if (null == item.userIndex) {
                AppLog.d("null == item.userIndex");
                continue;
            }
            DiscoveredDevice target = null;
            for (DiscoveredDevice discoverDevice : discoverDevices) {
                if (item.address.equals(discoverDevice.getAddress())) {
                    target = discoverDevice;
                }
            }
            if (null == target) {
                AppLog.d("null == target");
                continue;
            }
            EachUserData eachUserData;
            if (null == (eachUserData = target.getEachUserData())) {
                AppLog.d("null == (eachUserData = target.fetchManufacturerDataOfOmron())");
                continue;
            }
            EachUserData.User user = eachUserData.getUsers().get(item.userIndex - 1);
            if (null == user) {
                AppLog.d("null == user");
                continue;
            }

            if (NO_RECORD == user.lastSequenceNumber && 0 != user.numberOfRecords) {
                item.isBreakdown = true;
            }
            else {
                item.isBreakdown = false;
            }

            if (0 == user.numberOfRecords) {
                AppLog.d("0 == user.numberOfRecords");
                continue;
            }
            if (NO_RECORD == user.lastSequenceNumber) {
                AppLog.d("NO_RECORD == user.lastSequenceNumber");
                continue;
            }

            if (Protocol.BluetoothStandard == item.protocol && null == item.lastSequenceNumber) {
                AppLog.d("Protocol.OmronExtension != item.protocol && null == item.lastSequenceNumber");
                continue;
            }

            if (null == item.lastSequenceNumber) {
                item.hasNewRecords = true;
            } else if (item.lastSequenceNumber < user.lastSequenceNumber) {
                item.hasNewRecords = true;
            }
        }
        mListAdapter.notifyDataSetChanged();
    }

    public enum Event {
        TransferToDevice, DeviceInfo, AddDevice,
    }

    public enum EventArg {
        DeviceAddress,
    }

    public interface EventListener {
        void onFragmentEvent(@NonNull Event event, Bundle args);
    }

    private static class ListItem {
        public boolean hasNewRecords;
        public boolean isBreakdown;
        public String address;
        public String localName;
        public String modelName;
        public Integer userIndex;
        public Protocol protocol;
        public OHQDeviceCategory deviceCategory;
        public Integer lastSequenceNumber;

        public ListItem(DeviceInfo deviceInfo) {
            hasNewRecords = false;
            isBreakdown = false;
            address = deviceInfo.getAddress();
            localName = deviceInfo.getLocalName();
            modelName = deviceInfo.getModelName();
            userIndex = deviceInfo.getUserIndex();
            protocol = deviceInfo.getProtocol();
            deviceCategory = deviceInfo.getDeviceCategory();
            lastSequenceNumber = deviceInfo.getSequenceNumberOfLatestRecord();
        }
    }

    private static class ListAdapter extends AbstractListAdapter<ListItem> {

        public ListAdapter(@NonNull Context context) {
            super(context);
        }

        @NonNull
        @Override
        protected View onCreateView(int position, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.list_item_device, parent, false);

            view.findViewById(R.id.rssiLayout).setVisibility(View.GONE);

            ViewHolder holder = new ViewHolder();
            holder.deviceCategoryColorLabel = view.findViewById(deviceCategoryColorLabel);
            holder.newIcon = (ImageView) view.findViewById(R.id.newIcon);
            holder.modelName = (TextView) view.findViewById(R.id.title);
            holder.address = (TextView) view.findViewById(R.id.summary1);
            holder.localName = (TextView) view.findViewById(R.id.summary2);
            holder.standardLabel = (TextView) view.findViewById(R.id.standardLabel);
            holder.omronExLabel = (TextView) view.findViewById(R.id.omronExLabel);
            holder.userIndex = (TextView) view.findViewById(R.id.userIndex);
            holder.intoButton = (ImageButton) view.findViewById(R.id.detailInfoButton);

            final ListView parentList = (ListView) parent;
            holder.intoButton.setOnClickListener(new View.OnClickListener() {
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
            ListItem item = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.deviceCategoryColorLabel.setBackgroundResource(Common.getDeviceCategoryColorResource(item.deviceCategory));
            if (item.hasNewRecords) {
                holder.newIcon.setVisibility(View.VISIBLE);
            } else {
                holder.newIcon.setVisibility(View.INVISIBLE);
            }
            holder.modelName.setText(item.modelName);
            holder.address.setText(item.address);
            holder.localName.setText(item.localName);
            if (Protocol.BluetoothStandard == item.protocol) {
                holder.standardLabel.setVisibility(View.VISIBLE);
                holder.omronExLabel.setVisibility(View.GONE);
            } else {
                holder.standardLabel.setVisibility(View.GONE);
                holder.omronExLabel.setVisibility(View.VISIBLE);
            }
            if (null != item.userIndex) {
                holder.userIndex.setVisibility(View.VISIBLE);
                holder.userIndex.setText(String.format(Locale.US, "%d", item.userIndex));
            } else {
                holder.userIndex.setVisibility(View.GONE);
            }
            holder.intoButton.setTag(position);
        }

        private static class ViewHolder {
            View deviceCategoryColorLabel;
            ImageView newIcon;
            TextView modelName;
            TextView address;
            TextView localName;
            TextView standardLabel;
            TextView omronExLabel;
            TextView userIndex;
            ImageButton intoButton;
        }
    }
}
