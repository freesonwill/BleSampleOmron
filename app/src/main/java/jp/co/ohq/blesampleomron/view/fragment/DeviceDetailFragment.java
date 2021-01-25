package jp.co.ohq.blesampleomron.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DeviceInfo;
import jp.co.ohq.blesampleomron.model.entity.MultipleLineItem;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.adapter.MultipleLineListAdapter;
import jp.co.ohq.blesampleomron.view.dialog.EditTextDialog;
import jp.co.ohq.blesampleomron.view.dialog.SimpleDialog;
import jp.co.ohq.utility.Bundler;

import static jp.co.ohq.blesampleomron.R.string.address;


public class DeviceDetailFragment extends BaseFragment implements
        EditTextDialog.Callback,
        AdapterView.OnItemClickListener,
        SimpleDialog.Callback {

    private static final String ARG_DEVICE_ADDRESS = "ARG_DEVICE_ADDRESS";

    private static final int DIALOG_REQ_CODE_DELETE = 1;
    private Realm mRealm;
    private String mAddress;
    private MultipleLineListAdapter mListAdapter;
    private EventListener mListener;
    private DeviceInfo mDeviceInfo;
    private int mLastSequenceNumberPosition;

    public static DeviceDetailFragment newInstance(@NonNull String address) {
        DeviceDetailFragment fragment = new DeviceDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AppLog.vMethodIn();
        super.onAttach(context);
        if (!(context instanceof EventListener)) {
            throw new AndroidRuntimeException("Activity is must be implement 'EventListener'");
        }
        mListener = (EventListener) context;

        Bundle args = getArguments();
        mAddress = args.getString(ARG_DEVICE_ADDRESS);
        if (null == mAddress) {
            throw new IllegalArgumentException("Argument '" + ARG_DEVICE_ADDRESS + "' must not be null.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mRealm = Realm.getDefaultInstance();
        mDeviceInfo = mRealm.where(DeviceInfo.class).equalTo(
                "users.name", AppConfig.sharedInstance().getNameOfCurrentUser()).equalTo("address", mAddress).findFirst();
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
        View rootView = inflater.inflate(R.layout.fragment_device_detail, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        mListAdapter = new MultipleLineListAdapter(getContext(), deviceInfoToItems(mDeviceInfo));
        listView.setAdapter(mListAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        AppLog.vMethodIn();
        inflater.inflate(R.menu.fragment_device_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.vMethodIn();
        int id = item.getItemId();
        if (id == R.id.delete) {
            if (OHQDeviceCategory.BodyCompositionMonitor == mDeviceInfo.getDeviceCategory() ||
                    OHQDeviceCategory.BloodPressureMonitor == mDeviceInfo.getDeviceCategory() &&
                            Protocol.OmronExtension == mDeviceInfo.getProtocol()) {
                new SimpleDialog.Builder(this)
                        .items(
                                getString(R.string.delete_user_message),
                                getString(R.string.forget_device_message)
                        )
                        .negative(getString(R.string.cancel))
                        .requestCode(DIALOG_REQ_CODE_DELETE)
                        .show();
            } else {
                new SimpleDialog.Builder(this)
                        .message(getString(R.string.delete_device_message))
                        .positive(getString(R.string.ok))
                        .negative(getString(R.string.cancel))
                        .requestCode(DIALOG_REQ_CODE_DELETE)
                        .show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSimpleDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        AppLog.vMethodIn();

        if (DIALOG_REQ_CODE_DELETE != requestCode) {
            throw new AndroidRuntimeException("DIALOG_REQ_CODE_DELETE != requestCode");
        }

        switch (resultCode) {
            case 0: // delete_user_message
                mListener.onFragmentEvent(Event.DeleteUser, Bundler.bundle(
                        EventArg.DeviceAddress.name(), mAddress
                ));
                break;
            case 1: // forget_device_message
            case DialogInterface.BUTTON_POSITIVE:
                mListener.onFragmentEvent(Event.ForgetDevice, Bundler.bundle(
                        EventArg.DeviceAddress.name(), mAddress
                ));
                break;
            default:
                break;
        }
    }

    @Override
    public void onSimpleDialogCancelled(int requestCode, Bundle params) {
        AppLog.vMethodIn();
    }

    @Override
    public void onEditTextDialogEdited(String resultCode) {
        AppLog.d(resultCode);
        mRealm.beginTransaction();
        mDeviceInfo.setSequenceNumberOfLatestRecord(Integer.valueOf(resultCode));
        mRealm.commitTransaction();
        mListAdapter.getItem(mLastSequenceNumberPosition).setSummary(String.valueOf(mDeviceInfo.getSequenceNumberOfLatestRecord()));
    }

    @Override
    public void onEditTextDialogCanceled() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MultipleLineItem item = mListAdapter.getItem(position);
        if (getString(R.string.last_sequence_number).equals(item.getTitle())) {
            mLastSequenceNumberPosition = position;
            EditTextDialog.newInstance(getString(R.string.last_sequence_number),
                    String.valueOf(mDeviceInfo.getSequenceNumberOfLatestRecord())).show(getChildFragmentManager(), "");
        }
    }

    @NonNull
    private List<MultipleLineItem> deviceInfoToItems(@NonNull DeviceInfo deviceInfo) {
        AppLog.vMethodIn();
        List<MultipleLineItem> items = new LinkedList<>();
        items.add(new MultipleLineItem(getString(R.string.device_information)));
        if (null != deviceInfo.getAddress()) {
            items.add(new MultipleLineItem(getString(address), deviceInfo.getAddress()));
        }
        if (null != deviceInfo.getLocalName()) {
            items.add(new MultipleLineItem(getString(R.string.local_name), deviceInfo.getLocalName()));
        }
        items.add(new MultipleLineItem(getString(R.string.protocol)));
        if (null != deviceInfo.getProtocol()) {
            items.add(new MultipleLineItem(deviceInfo.getProtocol().name(), null));
        }
        if (null != deviceInfo.getUserIndex() ||
                null != deviceInfo.getConsentCode() ||
                null != deviceInfo.getSequenceNumberOfLatestRecord()) {
            items.add(new MultipleLineItem(getString(R.string.registration_information)));
        }
        if (null != deviceInfo.getUserIndex()) {
            items.add(new MultipleLineItem(getString(R.string.user_index), deviceInfo.getUserIndex()));
        }
        if (null != deviceInfo.getConsentCode()) {
            items.add(new MultipleLineItem(getString(R.string.consent_code), String.format(Locale.US, "0x%04x", deviceInfo.getConsentCode())));
        }
        if (null != deviceInfo.getSequenceNumberOfLatestRecord()) {
            items.add(new MultipleLineItem(getString(R.string.last_sequence_number), deviceInfo.getSequenceNumberOfLatestRecord()));
        }
        return items;
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return mDeviceInfo.getModelName().toUpperCase();
    }

    public enum Event {
        DeleteUser,
        ForgetDevice
    }

    public enum EventArg {
        DeviceAddress,
    }

    public interface EventListener {
        void onFragmentEvent(Event event, Bundle args);
    }
}
