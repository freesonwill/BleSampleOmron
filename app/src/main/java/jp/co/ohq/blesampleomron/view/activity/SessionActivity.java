package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DeviceInfo;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.model.entity.UserInfo;
import jp.co.ohq.blesampleomron.model.enumerate.ComType;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.fragment.SessionFragment;
import jp.co.ohq.utility.Types;

import static jp.co.ohq.blesampleomron.model.enumerate.ComType.Register;
import static jp.co.ohq.blesampleomron.model.enumerate.ComType.Transfer;

public class SessionActivity extends BaseActivity implements
        SessionFragment.EventListener {

    private static final String ARG_COM_TYPE = "ARG_COM_TYPE";
    private static final String ARG_MODE = "ARG_MODE";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";
    private static final String ARG_DISCOVER_DEVICE = "ARG_DISCOVER_DEVICE";
    private static final String ARG_PROTOCOL = "ARG_PROTOCOL";
    private static final String ARG_USER_INDEX = "ARG_USER_INDEX";
    private static final String ARG_USER_DATA = "ARG_USER_DATA";
    private final String mCurrentUserName = AppConfig.sharedInstance().getNameOfCurrentUser();
    private Realm mRealm;

    public static Intent newIntentForRegister(
            @NonNull Context context,
            @NonNull DiscoveredDevice discoverDevice,
            @NonNull Protocol protocol,
            @Nullable Integer userIndex) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(ARG_MODE, Mode.Normal);
        intent.putExtra(ARG_COM_TYPE, Register);
        intent.putExtra(ARG_DISCOVER_DEVICE, discoverDevice);
        intent.putExtra(ARG_PROTOCOL, protocol);
        if (null != userIndex) {
            intent.putExtra(ARG_USER_INDEX, userIndex);
        }
        return intent;
    }

    public static Intent newIntentForDelete(
            @NonNull Context context,
            @NonNull String address) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(ARG_MODE, Mode.Normal);
        intent.putExtra(ARG_COM_TYPE, ComType.Delete);
        intent.putExtra(ARG_ADDRESS, address);
        return intent;
    }

    public static Intent newIntentForTransfer(
            @NonNull Context context,
            @NonNull String address) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(ARG_MODE, Mode.Normal);
        intent.putExtra(ARG_COM_TYPE, Transfer);
        intent.putExtra(ARG_ADDRESS, address);
        return intent;
    }

    public static Intent newIntentForChangeToUnregisteredUserMode(
            @NonNull Context context,
            @NonNull DiscoveredDevice discoverDevice) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(ARG_MODE, Mode.UnregisteredUser);
        intent.putExtra(ARG_COM_TYPE, Register);
        intent.putExtra(ARG_DISCOVER_DEVICE, discoverDevice);
        return intent;
    }

    public static Intent newIntentForChangeToNormalMode(
            @NonNull Context context,
            @NonNull DiscoveredDevice discoverDevice) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(ARG_MODE, Mode.UnregisteredUser);
        intent.putExtra(ARG_COM_TYPE, ComType.Delete);
        intent.putExtra(ARG_DISCOVER_DEVICE, discoverDevice);
        return intent;
    }

    public static Intent newIntentForUnregisteredUserModeTransfer(
            @NonNull Context context,
            @NonNull DiscoveredDevice discoverDevice,
            @NonNull Map<OHQUserDataKey, Object> userData) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(ARG_MODE, Mode.UnregisteredUser);
        intent.putExtra(ARG_COM_TYPE, Transfer);
        intent.putExtra(ARG_DISCOVER_DEVICE, discoverDevice);
        intent.putExtra(ARG_USER_DATA, new HashMap<>(userData));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();

        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        Intent intent = getIntent();
        final Mode mode = (Mode) intent.getSerializableExtra(ARG_MODE);
        final ComType comType = (ComType) intent.getSerializableExtra(ARG_COM_TYPE);
        final Fragment fragment;
        if (Mode.Normal == mode) {
            if (ComType.Register == comType) {
                DiscoveredDevice discoverDevice = intent.getParcelableExtra(ARG_DISCOVER_DEVICE);
                Protocol protocol = (Protocol) intent.getSerializableExtra(ARG_PROTOCOL);
                Integer userIndex = intent.getIntExtra(ARG_USER_INDEX, -1);
                if (-1 == userIndex) {
                    userIndex = null;
                }
                UserInfo userInfo = mRealm.where(UserInfo.class).equalTo(
                        "name", mCurrentUserName).findFirst();
                Map<OHQUserDataKey, Object> userData = new HashMap<>();
                userData.put(OHQUserDataKey.DateOfBirthKey, userInfo.getDateOfBirth());
                userData.put(OHQUserDataKey.HeightKey, userInfo.getHeight());
                userData.put(OHQUserDataKey.GenderKey, userInfo.getGender());
                fragment = SessionFragment.newInstanceForRegister(discoverDevice, protocol, userData, userIndex);
            } else if (ComType.Delete == comType) {
                String address = intent.getStringExtra(ARG_ADDRESS);
                DeviceInfo deviceInfo = mRealm.where(DeviceInfo.class).equalTo(
                        "users.name", mCurrentUserName).equalTo("address", address).findFirst();
                fragment = SessionFragment.newInstanceForDelete(deviceInfo);
            } else {
                String address = intent.getStringExtra(ARG_ADDRESS);
                DeviceInfo deviceInfo = mRealm.where(DeviceInfo.class).equalTo(
                        "users.name", mCurrentUserName).equalTo("address", address).findFirst();
                UserInfo userInfo = mRealm.where(UserInfo.class).equalTo(
                        "name", mCurrentUserName).findFirst();
                Map<OHQUserDataKey, Object> userData = new HashMap<>();
                userData.put(OHQUserDataKey.DateOfBirthKey, userInfo.getDateOfBirth());
                userData.put(OHQUserDataKey.HeightKey, userInfo.getHeight());
                userData.put(OHQUserDataKey.GenderKey, userInfo.getGender());
                fragment = SessionFragment.newInstanceForTransfer(deviceInfo, userData);
            }
        } else {
            if (ComType.Register == comType) {
                DiscoveredDevice discoverDevice = intent.getParcelableExtra(ARG_DISCOVER_DEVICE);
                fragment = SessionFragment.newInstanceForChangeToUnregisteredUserMode(discoverDevice);
            } else if (ComType.Delete == comType) {
                DiscoveredDevice discoverDevice = intent.getParcelableExtra(ARG_DISCOVER_DEVICE);
                fragment = SessionFragment.newInstanceForChangeToNormalMode(discoverDevice);
            } else {
                DiscoveredDevice discoverDevice = intent.getParcelableExtra(ARG_DISCOVER_DEVICE);
                Map<OHQUserDataKey, Object> userData = Types.autoCast(intent.getSerializableExtra(ARG_USER_DATA));
                fragment = SessionFragment.newInstanceForUnregisteredUserModeTransfer(discoverDevice, userData);
            }
        }
        replaceFragment(android.R.id.content, fragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public void onBackPressed() {
        // Do not let it back.
    }

    @Override
    public void onFragmentEvent(@NonNull SessionFragment.Event event, Bundle args) {
        AppLog.vMethodIn();
        switch (event) {
            case Finished:
                HistoryData historyData = args.getParcelable(SessionFragment.EventArg.ResultData.name());
                if (null == historyData) {
                    throw new IllegalArgumentException("null == resultData");
                }
                Intent intent = new Intent();
                intent.putExtra(Result.ResultData.name(), historyData);
                setResult(RESULT_OK, intent);
                break;
            case Canceled:
                setResult(RESULT_CANCELED, null);
                break;
        }
        finish();
    }

    public enum Result {
        ResultData,
    }

    private enum Mode {
        Normal, UnregisteredUser
    }
}
