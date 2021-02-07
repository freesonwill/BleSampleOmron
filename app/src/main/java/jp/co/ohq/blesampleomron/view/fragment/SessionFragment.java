package jp.co.ohq.blesampleomron.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import jp.co.ohq.androidcorebluetooth.CBConfig;
import jp.co.ohq.androidcorebluetooth.CBPeripheral;
import jp.co.ohq.ble.OHQConfig;
import jp.co.ohq.ble.OHQDeviceManager;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDetailedState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.BluetoothPowerController;
import jp.co.ohq.blesampleomron.controller.SessionController;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.controller.util.Common;
import jp.co.ohq.blesampleomron.model.entity.DeviceInfo;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.model.entity.SessionData;
import jp.co.ohq.blesampleomron.model.entity.UserInfo;
import jp.co.ohq.blesampleomron.model.enumerate.ComType;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;
import jp.co.ohq.blesampleomron.model.enumerate.ResultType;
import jp.co.ohq.blesampleomron.model.enumerate.SettingKey;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.model.system.HistoryManager;
import jp.co.ohq.blesampleomron.model.system.LoggingManager;
import jp.co.ohq.utility.Bundler;
import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.Types;

public class SessionFragment extends BaseFragment implements
        BluetoothPowerController.Listener,
        SessionController.Listener,
        OHQDeviceManager.DebugMonitor {

    private static final int CONSENT_CODE_OHQ = 0x020E;
    private static final int CONSENT_CODE_UNREGISTERED_USER = 0x0000;

    private static final int USER_INDEX_UNREGISTERED_USER = 0xFF;

    private static final long CONNECTION_WAIT_TIME = 60000;
    private static final String ARG_MODE = "ARG_MODE";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";
    private static final String ARG_OPTION = "ARG_OPTION";
    private static final String ARG_PARTIAL_HISTORY_DATA = "ARG_PARTIAL_HISTORY_DATA";
    @NonNull
    private final String mCurrentUserName = AppConfig.sharedInstance().getNameOfCurrentUser();
    @NonNull
    private Mode mMode = Mode.Normal;
    @NonNull
    private String mAddress = "";
    @NonNull
    private Map<OHQSessionOptionKey, Object> mOption = new HashMap<>();
    @NonNull
    private HistoryData mHistoryData = new HistoryData();
    @NonNull
    private LoggingManager mLoggingManager = new LoggingManager();
    private Realm mRealm;
    private EventListener mListener;
    private View mView;
    private TextView mAclStateView;
    private TextView mGattStateView;
    private TextView mBasicStateView;
    private TextView mDetailStateView;
    private Button mCancelButton;
    private BluetoothPowerController mBluetoothPowerController;
    private SessionController mSessionController;
    private Boolean mIsFirstSession;

    @NonNull
    public static SessionFragment newInstanceForRegister(
            @NonNull DiscoveredDevice discoverDevice,
            @NonNull Protocol protocol,
            @Nullable Map<OHQUserDataKey, Object> userData,
            @Nullable Integer userIndex) {
        final Map<OHQSessionOptionKey, Object> option = new HashMap<>();
        boolean specifiedUserControl = false;
        if (OHQDeviceCategory.WeightScale == discoverDevice.getDeviceCategory()) {
            specifiedUserControl = true;
        }
        if (OHQDeviceCategory.BodyCompositionMonitor == discoverDevice.getDeviceCategory()) {
            specifiedUserControl = true;
        }
        if (Protocol.OmronExtension == protocol) {
            specifiedUserControl = true;
        }
        if (specifiedUserControl) {
            option.put(OHQSessionOptionKey.RegisterNewUserKey, true);
            option.put(OHQSessionOptionKey.ConsentCodeKey, CONSENT_CODE_OHQ);
            if (null != userIndex) {
                option.put(OHQSessionOptionKey.UserIndexKey, userIndex);
            }
            if (null != userData) {
                option.put(OHQSessionOptionKey.UserDataKey, userData);
            }
            option.put(OHQSessionOptionKey.DatabaseChangeIncrementValueKey, (long) 0);
            option.put(OHQSessionOptionKey.UserDataUpdateFlagKey, true);
        }
        if (Protocol.OmronExtension == protocol) {
            option.put(OHQSessionOptionKey.AllowAccessToOmronExtendedMeasurementRecordsKey, true);
            option.put(OHQSessionOptionKey.AllowControlOfReadingPositionToMeasurementRecordsKey, true);
        }
        option.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);
        final HistoryData historyData = new HistoryData();
        historyData.setComType(ComType.Register);
        historyData.setAddress(discoverDevice.getAddress());
        historyData.setLocalName(discoverDevice.getLocalName());
        historyData.setCompleteLocalName(discoverDevice.getCompleteLocalName());
        historyData.setDeviceCategory(discoverDevice.getDeviceCategory());
        historyData.setProtocol(protocol);

        return newInstance(Mode.Normal, discoverDevice.getAddress(),
                option, historyData);
    }

    @NonNull
    public static SessionFragment newInstanceForDelete(
            @NonNull DeviceInfo deviceInfo) {
        if (null == deviceInfo.getAddress()) {
            throw new IllegalArgumentException("null == deviceInfo.getAddress()");
        }
        if (null == deviceInfo.getProtocol()) {
            throw new IllegalArgumentException("null == deviceInfo.getProtocol()");
        }
        if (null == deviceInfo.getUserIndex()) {
            throw new IllegalArgumentException("null == deviceInfo.getUserIndex()");
        }
        final Map<OHQSessionOptionKey, Object> option = new HashMap<>();
        option.put(OHQSessionOptionKey.DeleteUserDataKey, true);
        option.put(OHQSessionOptionKey.ConsentCodeKey, CONSENT_CODE_OHQ);
        option.put(OHQSessionOptionKey.UserIndexKey, deviceInfo.getUserIndex());
        option.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);

        final HistoryData historyData = new HistoryData();
        historyData.setComType(ComType.Delete);
        historyData.setAddress(deviceInfo.getAddress());
        historyData.setLocalName(deviceInfo.getLocalName());
        historyData.setCompleteLocalName(deviceInfo.getCompleteLocalName());
        historyData.setDeviceCategory(deviceInfo.getDeviceCategory());
        historyData.setProtocol(deviceInfo.getProtocol());

        return newInstance(Mode.Normal, deviceInfo.getAddress(),
                option, historyData);
    }

    @NonNull
    public static SessionFragment newInstanceForTransfer(
            @NonNull DeviceInfo deviceInfo,
            @Nullable Map<OHQUserDataKey, Object> userData) {
        if (null == deviceInfo.getAddress()) {
            throw new IllegalArgumentException("null == deviceInfo.getAddress()");
        }
        if (null == deviceInfo.getProtocol()) {
            throw new IllegalArgumentException("null == deviceInfo.getProtocol()");
        }
        final Map<OHQSessionOptionKey, Object> option = new HashMap<>();
        if (null != deviceInfo.getUserIndex()) {
            option.put(OHQSessionOptionKey.UserIndexKey, deviceInfo.getUserIndex());
            option.put(OHQSessionOptionKey.ConsentCodeKey, CONSENT_CODE_OHQ);
        }
        if (null != deviceInfo.getDatabaseChangeIncrement()) {
            option.put(OHQSessionOptionKey.DatabaseChangeIncrementValueKey, deviceInfo.getDatabaseChangeIncrement());
        }
        if (null != userData) {
            option.put(OHQSessionOptionKey.UserDataKey, userData);
            option.put(OHQSessionOptionKey.UserDataUpdateFlagKey, deviceInfo.isUserDataUpdateFlag());
        }
        if (Protocol.OmronExtension == deviceInfo.getProtocol()) {
            option.put(OHQSessionOptionKey.AllowAccessToOmronExtendedMeasurementRecordsKey, true);
            option.put(OHQSessionOptionKey.AllowControlOfReadingPositionToMeasurementRecordsKey, true);
            if (null != deviceInfo.getSequenceNumberOfLatestRecord()) {
                option.put(OHQSessionOptionKey.SequenceNumberOfFirstRecordToReadKey, deviceInfo.getSequenceNumberOfLatestRecord() + 1);
            }
        }
        option.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);

        final HistoryData historyData = new HistoryData();
        historyData.setComType(ComType.Transfer);
        historyData.setAddress(deviceInfo.getAddress());
        historyData.setLocalName(deviceInfo.getLocalName());
        historyData.setCompleteLocalName(deviceInfo.getCompleteLocalName());
        historyData.setDeviceCategory(deviceInfo.getDeviceCategory());
        historyData.setProtocol(deviceInfo.getProtocol());

        return newInstance(Mode.Normal, deviceInfo.getAddress(),
                option, historyData);
    }

    @NonNull
    public static SessionFragment newInstanceForChangeToUnregisteredUserMode(
            @NonNull DiscoveredDevice discoverDevice) {
        final Map<OHQSessionOptionKey, Object> option = new HashMap<>();
        option.put(OHQSessionOptionKey.RegisterNewUserKey, true);
        option.put(OHQSessionOptionKey.UserIndexKey, USER_INDEX_UNREGISTERED_USER);
        option.put(OHQSessionOptionKey.ConsentCodeKey, CONSENT_CODE_UNREGISTERED_USER);
        option.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);

        final HistoryData historyData = new HistoryData();
        historyData.setComType(ComType.Register);
        historyData.setAddress(discoverDevice.getAddress());
        historyData.setLocalName(discoverDevice.getLocalName());
        historyData.setCompleteLocalName(discoverDevice.getCompleteLocalName());
        historyData.setDeviceCategory(discoverDevice.getDeviceCategory());
        historyData.setProtocol(Protocol.OmronExtension);

        return newInstance(Mode.UnregisteredUser, discoverDevice.getAddress(),
                option, historyData);
    }

    @NonNull
    public static SessionFragment newInstanceForChangeToNormalMode(
            @NonNull DiscoveredDevice discoverDevice) {
        final Map<OHQSessionOptionKey, Object> option = new HashMap<>();
        option.put(OHQSessionOptionKey.DeleteUserDataKey, true);
        option.put(OHQSessionOptionKey.UserIndexKey, USER_INDEX_UNREGISTERED_USER);
        option.put(OHQSessionOptionKey.ConsentCodeKey, CONSENT_CODE_UNREGISTERED_USER);
        option.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);

        final HistoryData historyData = new HistoryData();
        historyData.setComType(ComType.Delete);
        historyData.setAddress(discoverDevice.getAddress());
        historyData.setLocalName(discoverDevice.getLocalName());
        historyData.setCompleteLocalName(discoverDevice.getCompleteLocalName());
        historyData.setDeviceCategory(discoverDevice.getDeviceCategory());
        historyData.setProtocol(Protocol.OmronExtension);

        return newInstance(Mode.UnregisteredUser, discoverDevice.getAddress(),
                option, historyData);
    }

    @NonNull
    public static SessionFragment newInstanceForUnregisteredUserModeTransfer(
            @NonNull DiscoveredDevice discoverDevice,
            @NonNull Map<OHQUserDataKey, Object> userData) {
        final Map<OHQSessionOptionKey, Object> option = new HashMap<>();
        option.put(OHQSessionOptionKey.UserIndexKey, USER_INDEX_UNREGISTERED_USER);
        option.put(OHQSessionOptionKey.ConsentCodeKey, CONSENT_CODE_UNREGISTERED_USER);
        option.put(OHQSessionOptionKey.UserDataKey, userData);
        option.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);
        option.put(OHQSessionOptionKey.AllowAccessToOmronExtendedMeasurementRecordsKey, true);

        final HistoryData historyData = new HistoryData();
        historyData.setComType(ComType.Transfer);
        historyData.setAddress(discoverDevice.getAddress());
        historyData.setLocalName(discoverDevice.getLocalName());
        historyData.setCompleteLocalName(discoverDevice.getCompleteLocalName());
        historyData.setDeviceCategory(discoverDevice.getDeviceCategory());
        historyData.setProtocol(Protocol.OmronExtension);

        return newInstance(Mode.UnregisteredUser, discoverDevice.getAddress(),
                option, historyData);
    }

    private static SessionFragment newInstance(
            @NonNull final Mode mode,
            @NonNull final String address,
            @NonNull final Map<OHQSessionOptionKey, Object> option,
            @NonNull final HistoryData partialHistoryData) {
        //option.remove(OHQSessionOptionKey.UserDataKey);
        //option.remove(OHQSessionOptionKey.UserDataUpdateFlagKey);
        //option.remove(OHQSessionOptionKey.ReadMeasurementRecordsKey);
        AppLog.vMethodIn(mode.name() + " " + address + " " + option + " " + partialHistoryData.toString());
        SessionFragment fragment = new SessionFragment();
        fragment.setArguments(Bundler.bundle(
                ARG_MODE, mode,
                ARG_ADDRESS, address,
                ARG_OPTION, option,
                ARG_PARTIAL_HISTORY_DATA, partialHistoryData));
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AppLog.vMethodIn();
        super.onAttach(context);
        if (null != getParentFragment() && getParentFragment() instanceof EventListener) {
            mListener = Types.autoCast(getParentFragment());
        } else if (context instanceof EventListener) {
            mListener = Types.autoCast(context);
        } else {
            throw new AndroidRuntimeException("Parent is must be implement 'EventListener'");
        }
        Bundle args = getArguments();
        final Mode mode = Types.autoCast(args.getSerializable(ARG_MODE));
        if (null == mode) {
            throw new IllegalArgumentException("Argument '" + ARG_MODE + "' must not be null.");
        }
        mMode = mode;
        final String address = args.getString(ARG_ADDRESS);
        if (null == address) {
            throw new IllegalArgumentException("Argument '" + ARG_ADDRESS + "' must not be null.");
        }
        mAddress = address;
        final Map<OHQSessionOptionKey, Object> option = Types.autoCast(args.getSerializable(ARG_OPTION));
        if (null == option) {
            throw new IllegalArgumentException("Argument '" + ARG_OPTION + "' must not be null.");
        }
        Toast.makeText(this.getContext(), "sessionCtx:" + option.keySet().toString(), Toast.LENGTH_SHORT).show();

        mOption = option;
        final HistoryData partialHistoryData = args.getParcelable(ARG_PARTIAL_HISTORY_DATA);
        if (null == partialHistoryData) {
            throw new IllegalArgumentException("Argument '" + ARG_PARTIAL_HISTORY_DATA + "' must not be null.");
        }
        mHistoryData = partialHistoryData;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);
        mBluetoothPowerController = new BluetoothPowerController(this);
        mSessionController = new SessionController(this, this);
        mRealm = Realm.getDefaultInstance();
        mIsFirstSession = true;
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
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mView = view;
        mAclStateView = (TextView) view.findViewById(R.id.aclState);
        mGattStateView = (TextView) view.findViewById(R.id.gattState);
        mBasicStateView = (TextView) view.findViewById(R.id.connectionState);
        mDetailStateView = (TextView) view.findViewById(R.id.detailState);
        mCancelButton = (Button) view.findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessionController.cancel();
            }
        });
        Button cancelTurnOnButton = (Button) view.findViewById(R.id.cancelTurnOnButton);
        cancelTurnOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentEvent(Event.Canceled, null);
            }
        });
    }

    @Override
    public void onResume() {
        AppLog.vMethodIn();
        super.onResume();
        mBluetoothPowerController.onResume();
        mSessionController.onResume();
        if (mBluetoothPowerController.state()) {
            mView.findViewById(R.id.turnOnMessageLayout).setVisibility(View.GONE);
            startSession();
        } else {
            mView.findViewById(R.id.turnOnMessageLayout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        AppLog.vMethodIn();
        super.onPause();
        mSessionController.onPause();
        mBluetoothPowerController.onPause();
    }

    @Override
    public void onBluetoothStateChanged(boolean enable) {
        if (enable) {
            mView.findViewById(R.id.turnOnMessageLayout).setVisibility(View.GONE);
            startSession();
        }
    }

    @Override
    public void onConnectionStateChanged(@NonNull OHQConnectionState connectionState) {
        AppLog.vMethodIn(connectionState.name());
        mBasicStateView.setText(connectionState.name());
        if (OHQConnectionState.Connected == connectionState) {
            mCancelButton.setEnabled(false);
            getActivity().setTitle(getString(R.string.processing).toUpperCase());
        }
    }

    @Override
    public void onSessionComplete(@NonNull SessionData sessionData) {
        AppLog.vMethodIn(sessionData.toString());
        if (OHQCompletionReason.Canceled == sessionData.getCompletionReason()) {
            mListener.onFragmentEvent(Event.Canceled, null);
            return;
        }
        ResultType sessionResult = validateSessionWithData(mHistoryData.getProtocol(), sessionData);
        mHistoryData.setResultType(sessionResult);
        mHistoryData.setReceivedDate(System.currentTimeMillis());
        mHistoryData.setUserName(mCurrentUserName);
        mHistoryData.setDeviceCategory(sessionData.getDeviceCategory());
        mHistoryData.setModelName(sessionData.getModelName());
        mHistoryData.setCurrentTime(sessionData.getCurrentTime());
        mHistoryData.setBatteryLevel(sessionData.getBatteryLevel());
        mHistoryData.setUserIndex(sessionData.getUserIndex());
        mHistoryData.setUserData(sessionData.getUserData());
        mHistoryData.setDatabaseChangeIncrement(sessionData.getDatabaseChangeIncrement());
        mHistoryData.setSequenceNumberOfLatestRecord(sessionData.getSequenceNumberOfLatestRecord());
        mHistoryData.setMeasurementRecords(sessionData.getMeasurementRecords());
        mHistoryData.setCompletionReason(sessionData.getCompletionReason());
        if (null != mHistoryData.getUserIndex()) {
            Integer consentCode = Types.autoCast(mOption.get(OHQSessionOptionKey.ConsentCodeKey));
            mHistoryData.setConsentCode(consentCode);
        }
        AppLog.i(mHistoryData.toString());

        if (Mode.Normal == mMode && ResultType.Success == sessionResult) {
            updateUserInfo(mHistoryData);
        }

        final Handler handler = new Handler();
        mLoggingManager.stop(new LoggingManager.ActionListener() {
            @Override
            public void onSuccess() {
                onFinished();
            }

            @Override
            public void onFailure() {
                AppLog.vMethodIn();
                onFinished();
            }

            private void onFinished() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        HistoryManager.sharedInstance().add(getContext(), mHistoryData, mLoggingManager.getLastLog());
                        mListener.onFragmentEvent(Event.Finished, Bundler.bundle(EventArg.ResultData.name(), mHistoryData));
                    }
                });
            }
        });
    }

    @Override
    public void onDetailedStateChanged(@NonNull OHQDetailedState newState) {
        AppLog.vMethodIn(newState.name());
        mDetailStateView.setText(newState.name());
    }

    @Override
    public void onPairingRequest() {
        AppLog.vMethodIn();
    }

    @Override
    public void onBondStateChanged(@NonNull CBPeripheral.BondState bondState) {
        AppLog.vMethodIn(bondState.name());
    }

    @Override
    public void onAclConnectionStateChanged(@NonNull CBPeripheral.AclConnectionState aclConnectionState) {
        AppLog.vMethodIn(aclConnectionState.name());
        mAclStateView.setText(aclConnectionState.name());
    }

    @Override
    public void onGattConnectionStateChanged(@NonNull CBPeripheral.GattConnectionState gattConnectionState) {
        AppLog.vMethodIn(gattConnectionState.name());
        mGattStateView.setText(gattConnectionState.name());
    }

    @Override
    @NonNull
    protected String onGetTitle() {
        return getString(R.string.connecting).toUpperCase();
    }

    @NonNull
    private Bundle getConfig(@NonNull Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String s;
        CBConfig.CreateBondOption cOption;
        s = pref.getString(SettingKey.create_bond_option.name(), null);
        if (getString(R.string.create_bond_before_catt_connection).equals(s)) {
            cOption = CBConfig.CreateBondOption.UsedBeforeGattConnection;
        } else if (getString(R.string.create_bond_after_services_discovered).equals(s)) {
            cOption = CBConfig.CreateBondOption.UsedAfterServicesDiscovered;
        } else {
            cOption = CBConfig.CreateBondOption.NotUse;
        }
        CBConfig.RemoveBondOption rOption;
        s = pref.getString(SettingKey.remove_bond_option.name(), null);
        if (getString(R.string.remove_bond_use).equals(s)) {
            rOption = CBConfig.RemoveBondOption.UsedBeforeConnectionProcessEveryTime;
        } else {
            rOption = CBConfig.RemoveBondOption.NotUse;
        }
        return Bundler.bundle(
                OHQConfig.Key.CreateBondOption.name(), cOption,
                OHQConfig.Key.RemoveBondOption.name(), rOption,
                OHQConfig.Key.AssistPairingDialogEnabled.name(), pref.getBoolean(SettingKey.assist_pairing_dialog.name(), false),
                OHQConfig.Key.AutoPairingEnabled.name(), pref.getBoolean(SettingKey.auto_pairing.name(), false),
                OHQConfig.Key.AutoEnterThePinCodeEnabled.name(), pref.getBoolean(SettingKey.auto_enter_the_pin_code.name(), false),
                OHQConfig.Key.PinCode.name(), pref.getString(SettingKey.pin_code.name(), "123456"),
                OHQConfig.Key.StableConnectionEnabled.name(), pref.getBoolean(SettingKey.stable_connection.name(), false),
                OHQConfig.Key.StableConnectionWaitTime.name(), Long.valueOf(pref.getString(SettingKey.stable_connection_wait_time.name(), "123456")),
                OHQConfig.Key.ConnectionRetryEnabled.name(), pref.getBoolean(SettingKey.connection_retry.name(), false),
                OHQConfig.Key.ConnectionRetryDelayTime.name(), Long.valueOf(pref.getString(SettingKey.connection_retry_delay_time.name(), "123456")),
                OHQConfig.Key.ConnectionRetryCount.name(), Integer.valueOf(pref.getString(SettingKey.connection_retry_count.name(), "123456")),
                OHQConfig.Key.UseRefreshWhenDisconnect.name(), pref.getBoolean(SettingKey.refresh_use.name(), false)
        );
    }

    private void startSession() {
        //startSession () will be called only once after SessionFragment is generated.
        //Make sure that startSession () will not be called again after the session is completed.
        if (mSessionController.isInSession() || !mIsFirstSession) {
            AppLog.i("Already started session.");
            return;
        }
        mIsFirstSession = false;
        final Handler handler = new Handler();
        mLoggingManager.start(new LoggingManager.ActionListener() {
            @Override
            public void onSuccess() {
                onStarted();
            }

            @Override
            public void onFailure() {
                onStarted();
            }

            private void onStarted() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Common.outputDeviceInfo(getContext());
                        mSessionController.setConfig(getConfig(getContext()));
                        mOption.put(OHQSessionOptionKey.ConnectionWaitTimeKey, CONNECTION_WAIT_TIME);
                        mSessionController.startSession(mAddress, mOption);
                    }
                });
            }
        });
    }

    private void updateUserInfo(@NonNull HistoryData historyData) {
        final UserInfo userInfo = mRealm.where(UserInfo.class).equalTo(
                "name", mCurrentUserName).findFirst();

        final Map<OHQUserDataKey, Object> userData = historyData.getUserData();
        if (!userData.isEmpty()) {
            boolean updated = false;
            mRealm.beginTransaction();
            String dateOfBirth = Types.autoCast(userData.get(OHQUserDataKey.DateOfBirthKey));
            if (null != dateOfBirth && !userInfo.getDateOfBirth().equals(dateOfBirth)) {
                userInfo.setDateOfBirth(dateOfBirth);
                updated = true;
            }
            BigDecimal height = Types.autoCast(userData.get(OHQUserDataKey.HeightKey));
            if (null != height && 0 != userInfo.getHeight().compareTo(height)) {
                userInfo.setHeight(height);
                updated = true;
            }
            OHQGender gender = Types.autoCast(userData.get(OHQUserDataKey.GenderKey));
            if (null != gender && userInfo.getGender() != gender) {
                userInfo.setGender(gender);
                updated = true;
            }
            if (updated) {
                for (DeviceInfo d : mRealm.where(DeviceInfo.class).equalTo("users.name", mCurrentUserName).findAll()) {
                    d.setUserDataUpdateFlag(true);
                }
            }
            mRealm.commitTransaction();
        }

        final DeviceInfo deviceInfo;
        switch (historyData.getComType()) {
            case Register:
                deviceInfo = new DeviceInfo();
                deviceInfo.setAddress(historyData.getAddress());
                deviceInfo.setLocalName(historyData.getLocalName());
                deviceInfo.setCompleteLocalName(historyData.getCompleteLocalName());
                deviceInfo.setModelName(historyData.getModelName());
                deviceInfo.setDeviceCategory(historyData.getDeviceCategory());
                deviceInfo.setProtocol(historyData.getProtocol());
                deviceInfo.setUserIndex(historyData.getUserIndex());
                deviceInfo.setConsentCode(historyData.getConsentCode());
                deviceInfo.setSequenceNumberOfLatestRecord(historyData.getSequenceNumberOfLatestRecord());
                deviceInfo.setDatabaseChangeIncrement(historyData.getDatabaseChangeIncrement());
                deviceInfo.setUserDataUpdateFlag(false);
                AppLog.d(deviceInfo.toString());
                mRealm.beginTransaction();
                userInfo.getRegisteredDevices().add(deviceInfo);
                mRealm.commitTransaction();
                break;
            case Delete:
                deviceInfo = mRealm.where(DeviceInfo.class).equalTo("users.name", mCurrentUserName)
                        .equalTo("address", historyData.getAddress()).findFirst();
                mRealm.beginTransaction();
                userInfo.getRegisteredDevices().remove(deviceInfo);
                mRealm.commitTransaction();
                break;
            case Transfer:
                deviceInfo = mRealm.where(DeviceInfo.class).equalTo("users.name", mCurrentUserName)
                        .equalTo("address", historyData.getAddress()).findFirst();
                mRealm.beginTransaction();
                deviceInfo.setSequenceNumberOfLatestRecord(historyData.getSequenceNumberOfLatestRecord());
                deviceInfo.setDatabaseChangeIncrement(historyData.getDatabaseChangeIncrement());
                deviceInfo.setUserDataUpdateFlag(false);
                mRealm.commitTransaction();
                break;
            default:
                throw new IllegalArgumentException("Illegal com type.");
        }
    }

    @NonNull
    private ResultType validateSessionWithData(@NonNull Protocol protocol, @NonNull SessionData sessionData) {
        if (OHQCompletionReason.Disconnected != sessionData.getCompletionReason()) {
            AppLog.e("OHQCompletionReason.Disconnected != sessionData.getCompletionReason()");
            return ResultType.Failure;
        }
        if (Protocol.OmronExtension == protocol && null == sessionData.getUserIndex()) {
            AppLog.e("Protocol.OmronExtension == protocol && null == sessionData.getUserIndex()");
            return ResultType.Failure;
        }
        if (null == sessionData.getBatteryLevel()) {
            AppLog.e("null == sessionData.getBatteryLevel()");
            return ResultType.Failure;
        }
        if (null == sessionData.getCurrentTime()) {
            AppLog.e("null == sessionData.getCurrentTime()");
            return ResultType.Failure;
        }
        Map<OHQSessionOptionKey, Object> option = sessionData.getOption();
        if (null == option) {
            AppLog.e("null == option");
            return ResultType.Failure;
        }
        if (option.containsKey(OHQSessionOptionKey.UserDataKey)
                && option.containsKey(OHQSessionOptionKey.DatabaseChangeIncrementValueKey)) {
            if (null != sessionData.getUserIndex() && null == sessionData.getDatabaseChangeIncrement()) {
                AppLog.e("null != sessionData.getUserIndex() && null == sessionData.getDatabaseChangeIncrement()");
                return ResultType.Failure;
            }
        }
        return ResultType.Success;
    }

    public enum Event {
        Finished,
        Canceled,
    }

    public enum EventArg {
        ResultData,
    }

    private enum Mode {
        Normal, UnregisteredUser
    }

    public interface EventListener {
        void onFragmentEvent(Event event, Bundle args);
    }
}

