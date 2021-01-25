package jp.co.ohq.blesampleomron.view.fragment.controller;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AndroidRuntimeException;
import android.view.View;

import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.model.enumerate.ActivityRequestCode;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.activity.DeviceDetailActivity;
import jp.co.ohq.blesampleomron.view.activity.DiscoveredDeviceSelectionActivity;
import jp.co.ohq.blesampleomron.view.activity.RegisterDeviceActivity;
import jp.co.ohq.blesampleomron.view.activity.ResultActivity;
import jp.co.ohq.blesampleomron.view.activity.SessionActivity;
import jp.co.ohq.blesampleomron.view.fragment.GuestHomeFragment;
import jp.co.ohq.blesampleomron.view.fragment.UserHomeFragment;
import jp.co.ohq.utility.Types;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class HomeFragmentController extends BaseFragmentController implements
        GuestHomeFragment.EventListener,
        UserHomeFragment.EventListener {

    private DiscoveredDevice mDiscoveredDevice;

    public static HomeFragmentController newInstance() {
        return new HomeFragmentController();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onViewCreated(view, savedInstanceState);
        replaceChildFragment();
    }

    @Override
    public void onFragmentEvent(@NonNull GuestHomeFragment.Event event, Bundle args) {
        AppLog.vMethodIn();
        switch (event) {
            case AddDevice: {
                startActivityForResult(DiscoveredDeviceSelectionActivity.newIntent(
                        getContext(),
                        false,
                        false,
                        true
                ), ActivityRequestCode.DiscoveredDeviceSelection.hashCode16());
                break;
            }
            case ChangeToNormalMode: {
                DiscoveredDevice discoveredDevice = args.getParcelable(GuestHomeFragment.EventArg.DiscoveredDevice.name());
                if (null == discoveredDevice) {
                    throw new AndroidRuntimeException("null == discoveredDevice");
                }
                startActivityForResult(SessionActivity.newIntentForChangeToNormalMode(getContext(), discoveredDevice), ActivityRequestCode.Session.hashCode16());
                break;
            }
            case ChangeToUnregisteredUserMode: {
                DiscoveredDevice discoveredDevice = args.getParcelable(GuestHomeFragment.EventArg.DiscoveredDevice.name());
                if (null == discoveredDevice) {
                    throw new AndroidRuntimeException("null == discoveredDevice");
                }
                startActivityForResult(SessionActivity.newIntentForChangeToUnregisteredUserMode(getContext(), discoveredDevice), ActivityRequestCode.Session.hashCode16());
                break;
            }
            case ReceiveMeasurementRecords: {
                DiscoveredDevice discoveredDevice = args.getParcelable(GuestHomeFragment.EventArg.DiscoveredDevice.name());
                if (null == discoveredDevice) {
                    throw new AndroidRuntimeException("null == discoveredDevice");
                }
                Map<OHQUserDataKey, Object> userData = Types.autoCast(args.getSerializable(GuestHomeFragment.EventArg.UserData.name()));
                if (null == userData) {
                    throw new AndroidRuntimeException("null == userData");
                }
                startActivityForResult(SessionActivity.newIntentForUnregisteredUserModeTransfer(getContext(), discoveredDevice, userData), ActivityRequestCode.Session.hashCode16());
                break;
            }
        }
    }

    @Override
    public void onFragmentEvent(@NonNull UserHomeFragment.Event event, Bundle args) {
        switch (event) {
            case TransferToDevice: {
                String address = args.getString(UserHomeFragment.EventArg.DeviceAddress.name());
                if (null == address) {
                    throw new AndroidRuntimeException("null == address");
                }
                startActivityForResult(SessionActivity.newIntentForTransfer(getContext(), address), ActivityRequestCode.Session.hashCode16());
                break;
            }
            case DeviceInfo: {
                String address = args.getString(UserHomeFragment.EventArg.DeviceAddress.name());
                if (null == address) {
                    throw new AndroidRuntimeException("null == address");
                }
                startActivity(DeviceDetailActivity.newIntent(getContext(), address));
                break;
            }
            case AddDevice: {
                startActivity(RegisterDeviceActivity.newIntent(getContext()));
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.vMethodIn();
        super.onActivityResult(requestCode, resultCode, data);
        final ActivityRequestCode requestCodeEnum;
        try {
            requestCodeEnum = ActivityRequestCode.valueOf(requestCode);
        } catch (IllegalArgumentException e) {
            return;
        }
        switch (requestCodeEnum) {
            case Session:
                onSessionActivityResult(resultCode, data);
                break;
            case DiscoveredDeviceSelection:
                onDeviceSelectionActivityResult(resultCode, data);
                break;
        }
    }

    private void onSessionActivityResult(int resultCode, Intent data) {
        AppLog.vMethodIn();
        switch (resultCode) {
            case RESULT_OK:
                HistoryData historyData = data.getParcelableExtra(SessionActivity.Result.ResultData.name());
                if (null == historyData) {
                    throw new NullPointerException("null == resultData");
                }
                startActivity(ResultActivity.newIntent(getContext(), historyData));
                break;
            case RESULT_CANCELED:
            default:
                break;
        }
    }

    private void onDeviceSelectionActivityResult(int resultCode, Intent data) {
        AppLog.vMethodIn();
        if (data != null) {
            mDiscoveredDevice = data.getParcelableExtra(DiscoveredDeviceSelectionActivity.Result.SelectedDevice.name());
        }
        switch (resultCode) {
            case RESULT_OK:
                replaceChildFragment();
                break;
            case RESULT_CANCELED:
            default:
                break;
        }
    }

    private void replaceChildFragment() {
        final Fragment fragment;
        if (AppConfig.sharedInstance().getNameOfCurrentUser().equals(AppConfig.GUEST)) {
            fragment = GuestHomeFragment.newInstance(mDiscoveredDevice);
        } else {
            fragment = UserHomeFragment.newInstance();
        }
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
