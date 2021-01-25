package jp.co.ohq.ble;

import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.co.ohq.androidcorebluetooth.CBCharacteristic;
import jp.co.ohq.androidcorebluetooth.CBCharacteristicWriteType;
import jp.co.ohq.androidcorebluetooth.CBDescriptor;
import jp.co.ohq.androidcorebluetooth.CBPeripheral;
import jp.co.ohq.androidcorebluetooth.CBPeripheralDelegate;
import jp.co.ohq.androidcorebluetooth.CBService;
import jp.co.ohq.androidcorebluetooth.CBStatusCode;
import jp.co.ohq.androidcorebluetooth.CBUUID;
import jp.co.ohq.ble.entity.internal.BloodPressureFeature;
import jp.co.ohq.ble.entity.internal.BloodPressureMeasurement;
import jp.co.ohq.ble.entity.internal.BodyCompositionFeature;
import jp.co.ohq.ble.entity.internal.BodyCompositionMeasurement;
import jp.co.ohq.ble.entity.internal.CharacteristicPresentationFormat;
import jp.co.ohq.ble.entity.internal.OmronMeasurementBP;
import jp.co.ohq.ble.entity.internal.OmronMeasurementWS;
import jp.co.ohq.ble.entity.internal.RecordAccessControlPoint;
import jp.co.ohq.ble.entity.internal.UserControlPoint;
import jp.co.ohq.ble.entity.internal.WeightMeasurement;
import jp.co.ohq.ble.entity.internal.WeightScaleFeature;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQDataType;
import jp.co.ohq.ble.enumerate.OHQDetailedState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.utility.Bytes;
import jp.co.ohq.utility.SynchronizeCallback;
import jp.co.ohq.utility.Types;
import jp.co.ohq.utility.sm.State;
import jp.co.ohq.utility.sm.StateMachine;

import static java.lang.String.format;
import static jp.co.ohq.ble.enumerate.OHQSessionOptionKey.AllowControlOfReadingPositionToMeasurementRecordsKey;
import static jp.co.ohq.ble.enumerate.OHQSessionOptionKey.DatabaseChangeIncrementValueKey;
import static jp.co.ohq.ble.enumerate.OHQSessionOptionKey.ReadMeasurementRecordsKey;
import static jp.co.ohq.ble.enumerate.OHQSessionOptionKey.RegisterNewUserKey;

final class OHQDevice extends StateMachine implements CBPeripheralDelegate {

    private static final int INVALID_SEQUENCE_NUMBER = 0;

    private static final int EVT_BASE = 0x00000000;

    private static final int EVT_START_TRANSFER = EVT_BASE + 0x0001;
    private static final int EVT_CANCEL_TRANSFER = EVT_BASE + 0x0002;
    private static final int EVT_UPDATE_CHAR_SUCCESS = EVT_BASE + 0x0003;
    private static final int EVT_UPDATE_CHAR_FAILURE = EVT_BASE + 0x0004;
    private static final int EVT_UPDATE_DESC_SUCCESS = EVT_BASE + 0x0005;
    private static final int EVT_UPDATE_DESC_FAILURE = EVT_BASE + 0x0006;
    private static final int EVT_WRITE_CHAR_SUCCESS = EVT_BASE + 0x0007;
    private static final int EVT_WRITE_CHAR_FAILURE = EVT_BASE + 0x0008;
    private static final int EVT_WRITE_DESC_SUCCESS = EVT_BASE + 0x0009;
    private static final int EVT_WRITE_DESC_FAILURE = EVT_BASE + 0x000a;
    private static final int EVT_UPDATE_NOTIFY_SUCCESS = EVT_BASE + 0x000b;
    private static final int EVT_UPDATE_NOTIFY_FAILURE = EVT_BASE + 0x000c;

    private final State mInactiveState = new InactiveState();
    private final State mDescValueReadingState = new DescValueReadingState();
    private final State mCharValueReadingState = new CharValueReadingState();
    private final State mNotificationEnablingState = new NotificationEnablingState();
    private final State mUserRegisteringState = new UserRegisteringState();
    private final State mUserAuthenticatingState = new UserAuthenticatingState();
    private final State mUserDataDeletingState = new UserDataDeletingState();
    private final State mDatabaseChangeIncrementNotificationWaitingState = new DatabaseChangeIncrementNotificationWaitingState();
    private final State mUserDataReadingState = new UserDataReadingState();
    private final State mUserDataWritingState = new UserDataWritingState();
    private final State mMeasurementRecordAccessControllingState = new MeasurementRecordAccessControllingState();
    private final State mIdleState = new IdleState();

    @NonNull
    private final CBPeripheral mPeripheral;
    @NonNull
    private final Delegate mDelegate;
    @NonNull
    private final Map<OHQSessionOptionKey, Object> mOptions;
    @NonNull
    private final OHQDeviceCategory mDeviceCategory;
    @NonNull
    private final Map<OHQUserDataKey, CBCharacteristic> mSupportedUserDataCharacteristics;
    @NonNull
    private final List<CBUUID> mNotificationEnabledCharacteristicUUIDs = new LinkedList<>();
    @NonNull
    private final Map<OHQUserDataKey, Object> mLatestUserData = new HashMap<>();
    @NonNull
    private final LinkedList<Map<OHQMeasurementRecordKey, Object>> mMeasurementRecords = new LinkedList<>();
    @Nullable
    private CharacteristicPresentationFormat mHeightCharacteristicPresentationFormat;
    @Nullable
    private Integer mAuthenticateUserIndex;
    @Nullable
    private Long mLatestDatabaseChangeIncrement;
    private boolean mWroteCurrentTime;
    @Nullable
    private byte[] mMultiplePacketMeasurementData;
    @Nullable
    private Map<OHQMeasurementRecordKey, Object> mPartialMeasurementRecord;

    OHQDevice(
            @Nullable Looper looper,
            @NonNull CBPeripheral peripheral,
            @NonNull Delegate delegate,
            @NonNull Map<OHQSessionOptionKey, Object> options) {
        super(OHQDevice.class.getSimpleName(), looper);

        mPeripheral = peripheral;
        mDelegate = delegate;
        mOptions = options;
        OHQLog.d("options:" + mOptions.toString());

        mPeripheral.delegate(this);

        if (null != _getService(OHQUUIDDefines.Service.BodyComposition.uuid())) {
            mDeviceCategory = OHQDeviceCategory.BodyCompositionMonitor;
        } else if (null != _getService(OHQUUIDDefines.Service.BloodPressure.uuid())) {
            mDeviceCategory = OHQDeviceCategory.BloodPressureMonitor;
        } else if (null != _getService(OHQUUIDDefines.Service.WeightScale.uuid())) {
            mDeviceCategory = OHQDeviceCategory.WeightScale;
        } else {
            mDeviceCategory = OHQDeviceCategory.Unknown;
        }
        mDelegate.dataObserver(OHQDataType.DeviceCategory, mDeviceCategory);

        mSupportedUserDataCharacteristics = new HashMap<>();
        mSupportedUserDataCharacteristics.put(OHQUserDataKey.DateOfBirthKey, _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.DateofBirth.uuid()));
        mSupportedUserDataCharacteristics.put(OHQUserDataKey.GenderKey, _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.Gender.uuid()));
        mSupportedUserDataCharacteristics.put(OHQUserDataKey.HeightKey, _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.Height.uuid()));
        OHQLog.d("SupportedUserDataKeys:" + mSupportedUserDataCharacteristics.toString());

        final State defaultState = new DefaultState();
        final State activeState = new ActiveState();
        final State notificationEnabledState = new NotificationEnabledState();

        addState(defaultState);
        addState(mInactiveState, defaultState);
        addState(activeState, defaultState);
        addState(mDescValueReadingState, activeState);
        addState(mCharValueReadingState, activeState);
        addState(mNotificationEnablingState, activeState);
        addState(notificationEnabledState, activeState);
        addState(mUserRegisteringState, notificationEnabledState);
        addState(mUserAuthenticatingState, notificationEnabledState);
        addState(mUserDataDeletingState, notificationEnabledState);
        addState(mDatabaseChangeIncrementNotificationWaitingState, notificationEnabledState);
        addState(mUserDataReadingState, notificationEnabledState);
        addState(mUserDataWritingState, notificationEnabledState);
        addState(mMeasurementRecordAccessControllingState, notificationEnabledState);
        addState(mIdleState, notificationEnabledState);

        setInitialState(mInactiveState);

        setTag(OHQLog.TAG);
        setDbg(OHQLog.OUTPUT_LOG_ENABLED);

        start();
    }

    public void startTransfer() {
        sendMessage(EVT_START_TRANSFER);
    }

    public void cancelTransfer() {
        sendMessage(EVT_CANCEL_TRANSFER);
    }

    @NonNull
    public LinkedList<Map<OHQMeasurementRecordKey, Object>> getMeasurementRecords() {
        final LinkedList<Map<OHQMeasurementRecordKey, Object>> ret;
        if (getHandler().isCurrentThread()) {
            ret = mMeasurementRecords;
        } else {
            final SynchronizeCallback callback = new SynchronizeCallback();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.setResult(mMeasurementRecords);
                    callback.unlock();
                }
            });
            callback.lock();
            ret = Types.autoCast(callback.getResult());
        }
        return ret;
    }

    @Override
    public void didDiscoverServices(@NonNull CBPeripheral peripheral/*, Error error*/) {

    }

    @Override
    public void didDiscoverIncludedServicesFor(@NonNull CBPeripheral peripheral, CBService service/*, Error error*/) {

    }

    @Override
    public void didDiscoverCharacteristicsFor(@NonNull CBPeripheral peripheral, CBService service/*, Error error*/) {

    }

    @Override
    public void didDiscoverDescriptorsFor(@NonNull CBPeripheral peripheral, CBCharacteristic characteristic/*, Error error*/) {

    }

    @Override
    public void didUpdateValueFor(@NonNull CBPeripheral peripheral, @NonNull CBCharacteristic characteristic, @NonNull byte[] value, int status) {
        if (CBStatusCode.GATT_SUCCESS == status) {
            Object[] objects = {characteristic, value};
            sendMessage(EVT_UPDATE_CHAR_SUCCESS, objects);
        } else {
            sendMessage(EVT_UPDATE_CHAR_FAILURE, status, 0, characteristic);
        }
    }

    @Override
    public void didUpdateValueFor(@NonNull CBPeripheral peripheral, @NonNull CBDescriptor descriptor, int status) {
        if (CBStatusCode.GATT_SUCCESS == status) {
            sendMessage(EVT_UPDATE_DESC_SUCCESS, descriptor);
        } else {
            sendMessage(EVT_UPDATE_DESC_FAILURE, status, 0, descriptor);
        }
    }

    @Override
    public void didWriteValueFor(@NonNull CBPeripheral peripheral, @NonNull CBCharacteristic characteristic, int status) {
        if (CBStatusCode.GATT_SUCCESS == status) {
            sendMessage(EVT_WRITE_CHAR_SUCCESS, characteristic);
        } else {
            sendMessage(EVT_WRITE_CHAR_FAILURE, status, 0, characteristic);
        }
    }

    @Override
    public void didWriteValueFor(@NonNull CBPeripheral peripheral, @NonNull CBDescriptor descriptor, int status) {
        if (CBStatusCode.GATT_SUCCESS == status) {
            sendMessage(EVT_WRITE_DESC_SUCCESS, descriptor);
        } else {
            sendMessage(EVT_WRITE_DESC_FAILURE, status, 0, descriptor);
        }
    }

    @Override
    public void didUpdateNotificationStateFor(@NonNull CBPeripheral peripheral, @NonNull CBCharacteristic characteristic, int status) {
        if (CBStatusCode.GATT_SUCCESS == status) {
            sendMessage(EVT_UPDATE_NOTIFY_SUCCESS, characteristic);
        } else {
            sendMessage(EVT_UPDATE_NOTIFY_FAILURE, status, 0, characteristic);
        }
    }

    @Override
    public void didReadRSSI(@NonNull CBPeripheral peripheral, int rssi, int status/*, Error error*/) {

    }

    @Nullable
    private CBService _getService(@NonNull CBUUID serviceUuid) {
        for (CBService service : mPeripheral.services()) {
            if (serviceUuid.equals(service.uuid())) {
                return service;
            }
        }
        return null;
    }

    @Nullable
    private CBCharacteristic _getCharacteristic(
            @NonNull CBUUID serviceUuid,
            @NonNull CBUUID characteristicUuid) {
        OHQLog.vMethodIn(characteristicUuid.uuidString());
        CBService service = _getService(serviceUuid);
        if (null == service) {
            return null;
        }
        for (CBCharacteristic characteristic : service.characteristics()) {
            if (characteristicUuid.equals(characteristic.uuid())) {
                return characteristic;
            }
        }
        return null;
    }

    @Nullable
    private CBDescriptor _getDescriptor(
            @Nullable CBCharacteristic characteristic,
            @NonNull CBUUID descriptorUuid) {
        if (null == characteristic) {
            return null;
        }
        for (CBDescriptor descriptor : characteristic.descriptors()) {
            if (descriptorUuid.equals(descriptor.uuid())) {
                return descriptor;
            }
        }
        return null;
    }

    private void _abort(@NonNull final OHQCompletionReason reason) {
        OHQLog.vMethodIn(reason.name());
        mDelegate.didAbortTransferWithReason(reason);
        transitionTo(mInactiveState);
    }

    public interface Delegate {
        void dataObserver(@NonNull OHQDataType aDataType, @NonNull Object aData);

        void didAbortTransferWithReason(@NonNull OHQCompletionReason aReason);

        void onStateChanged(@NonNull OHQDetailedState newState);
    }

    private class DefaultState extends State {
        @Override
        public boolean processMessage(@NonNull Message msg) {
            OHQLog.e("Illegal event. msg.what:" + String.format(Locale.US, "0x%08x", msg.what));
            return StateMachine.HANDLED;
        }
    }

    private class InactiveState extends State {
        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_START_TRANSFER:
                    handle = StateMachine.HANDLED;
                    mNotificationEnabledCharacteristicUUIDs.clear();
                    mLatestUserData.clear();
                    mMeasurementRecords.clear();
                    mHeightCharacteristicPresentationFormat = null;
                    mAuthenticateUserIndex = null;
                    mLatestDatabaseChangeIncrement = null;
                    transitionTo(mDescValueReadingState);
                    break;
                default:
                    break;
            }
            return handle;
        }
    }

    private class ActiveState extends State {
        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_CANCEL_TRANSFER:
                    handle = StateMachine.HANDLED;
                    transitionTo(mInactiveState);
                    break;
                case EVT_UPDATE_CHAR_FAILURE:
                case EVT_UPDATE_DESC_FAILURE:
                case EVT_WRITE_CHAR_FAILURE:
                case EVT_WRITE_DESC_FAILURE:
                case EVT_UPDATE_NOTIFY_FAILURE:
                    handle = StateMachine.HANDLED;
                    OHQLog.e("Failed to transfer. gatt status:" + msg.arg1);
                    _abort(OHQCompletionReason.FailedToTransfer);
                    break;
                default:
                    break;
            }
            return handle;
        }
    }

    private class DescValueReadingState extends State {

        private final List<CBDescriptor> descriptors = new LinkedList<CBDescriptor>() {
            @Override
            public boolean add(CBDescriptor descriptor) {
                return null != descriptor && super.add(descriptor);
            }
        };

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.DescValueReading);
            descriptors.add(_getDescriptor(
                    _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.Height.uuid()),
                    OHQUUIDDefines.Descriptor.CharacteristicPresentationFormat.uuid()));
            if (descriptors.isEmpty()) {
                transitionTo(mCharValueReadingState);
            } else {
                for (CBDescriptor descriptor : descriptors) {
                    mPeripheral.readValue(descriptor);
                }
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_UPDATE_DESC_SUCCESS: {
                    CBDescriptor descriptor = (CBDescriptor) msg.obj;
                    if (descriptors.contains(descriptor)) {
                        handle = StateMachine.HANDLED;
                        if (OHQUUIDDefines.Descriptor.CharacteristicPresentationFormat.uuid().equals(descriptor.uuid())) {
                            _didUpdateValueForCharacteristicPresentationFormatDescriptor(descriptor);
                        }
                        descriptors.remove(descriptor);
                        if (descriptors.isEmpty()) {
                            transitionTo(mCharValueReadingState);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForCharacteristicPresentationFormatDescriptor(@NonNull CBDescriptor descriptor) {
            mHeightCharacteristicPresentationFormat = new CharacteristicPresentationFormat(descriptor.value());
            OHQLog.i(mHeightCharacteristicPresentationFormat.toString());
        }
    }

    private class CharValueReadingState extends State {

        private final List<CBCharacteristic> characteristics = new LinkedList<CBCharacteristic>() {
            @Override
            public boolean add(CBCharacteristic characteristic) {
                return null != characteristic && super.add(characteristic);
            }
        };

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.CharValueReading);
            characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.DeviceInformation.uuid(), OHQUUIDDefines.Characteristic.ModelNumberString.uuid()));
            characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.BatteryService.uuid(), OHQUUIDDefines.Characteristic.BatteryLevel.uuid()));
            characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.BloodPressure.uuid(), OHQUUIDDefines.Characteristic.BloodPressureFeature.uuid()));
            characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.BodyComposition.uuid(), OHQUUIDDefines.Characteristic.BodyCompositionFeature.uuid()));
            characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.WeightScale.uuid(), OHQUUIDDefines.Characteristic.WeightScaleFeature.uuid()));
            if (characteristics.isEmpty()) {
                transitionTo(mNotificationEnablingState);
            } else {
                for (CBCharacteristic characteristic : characteristics) {
                    mPeripheral.readValue(characteristic);
                }
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_UPDATE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) ((Object[])msg.obj)[0];
                    if (characteristics.contains(characteristic)) {
                        handle = StateMachine.HANDLED;
                        if (OHQUUIDDefines.Characteristic.ModelNumberString.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForModelNumberStringCharacteristic(characteristic);
                        } else if (OHQUUIDDefines.Characteristic.BatteryLevel.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForBatteryLevelCharacteristic(characteristic);
                        } else if (OHQUUIDDefines.Characteristic.BloodPressureFeature.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForBloodPressureFeatureCharacteristic(characteristic);
                        } else if (OHQUUIDDefines.Characteristic.WeightScaleFeature.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForWeightScaleFeatureCharacteristic(characteristic);
                        } else if (OHQUUIDDefines.Characteristic.BodyCompositionFeature.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForBodyCompositionFeatureCharacteristic(characteristic);
                        }
                        characteristics.remove(characteristic);
                        if (characteristics.isEmpty()) {
                            transitionTo(mNotificationEnablingState);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForModelNumberStringCharacteristic(@NonNull CBCharacteristic characteristic) {
            String modelName = characteristic.stringValue();
            OHQLog.i(modelName);
            mDelegate.dataObserver(OHQDataType.ModelName, modelName);
        }

        private void _didUpdateValueForBatteryLevelCharacteristic(@NonNull CBCharacteristic characteristic) {
            int batteryLevel = characteristic.value()[0];
            OHQLog.i("Battery Level " + batteryLevel);
            mDelegate.dataObserver(OHQDataType.BatteryLevel, batteryLevel);
        }

        private void _didUpdateValueForBloodPressureFeatureCharacteristic(@NonNull CBCharacteristic characteristic) {
            BloodPressureFeature data = new BloodPressureFeature(characteristic.value());
            OHQLog.i(data.toString());
        }

        private void _didUpdateValueForWeightScaleFeatureCharacteristic(@NonNull CBCharacteristic characteristic) {
            WeightScaleFeature data = new WeightScaleFeature(characteristic.value());
            OHQLog.i(data.toString());
        }

        private void _didUpdateValueForBodyCompositionFeatureCharacteristic(@NonNull CBCharacteristic characteristic) {
            BodyCompositionFeature data = new BodyCompositionFeature(characteristic.value());
            OHQLog.i(data.toString());
        }
    }

    private class NotificationEnablingState extends State {

        private final List<CBCharacteristic> characteristics = new LinkedList<CBCharacteristic>() {
            @Override
            public boolean add(CBCharacteristic characteristic) {
                return null != characteristic && super.add(characteristic);
            }
        };

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.NotificationEnabling);
            characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.CurrentTimeService.uuid(), OHQUUIDDefines.Characteristic.CurrentTime.uuid()));
            characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.BatteryService.uuid(), OHQUUIDDefines.Characteristic.BatteryLevel.uuid()));
            if (mOptions.containsKey(RegisterNewUserKey) || mOptions.containsKey(OHQSessionOptionKey.UserIndexKey)) {
                characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.UserControlPoint.uuid()));
                characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.DatabaseChangeIncrement.uuid()));
            }
            CBCharacteristic omronBloodPressureMeasurementCharacteristic = _getCharacteristic(OHQUUIDDefines.Service.OmronOptionalService.uuid(), OHQUUIDDefines.Characteristic.OmronMeasurementBP.uuid());
            CBCharacteristic omronBodyCompositionMeasurementCharacteristic = _getCharacteristic(OHQUUIDDefines.Service.OmronOptionalService.uuid(), OHQUUIDDefines.Characteristic.OmronMeasurementWS.uuid());
            if (mOptions.containsKey(ReadMeasurementRecordsKey)) {
                if (mOptions.containsKey(AllowControlOfReadingPositionToMeasurementRecordsKey)) {
                    characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.OmronOptionalService.uuid(), OHQUUIDDefines.Characteristic.RecordAccessControlPoint.uuid()));
                }
                if (mOptions.containsKey(OHQSessionOptionKey.AllowAccessToOmronExtendedMeasurementRecordsKey) &&
                        ((null != omronBloodPressureMeasurementCharacteristic) || (null != omronBodyCompositionMeasurementCharacteristic))) {
                    characteristics.add(omronBloodPressureMeasurementCharacteristic);
                    characteristics.add(omronBodyCompositionMeasurementCharacteristic);
                } else {
                    characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.BloodPressure.uuid(), OHQUUIDDefines.Characteristic.BloodPressureMeasurement.uuid()));
                    characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.BodyComposition.uuid(), OHQUUIDDefines.Characteristic.BodyCompositionMeasurement.uuid()));
                    characteristics.add(_getCharacteristic(OHQUUIDDefines.Service.WeightScale.uuid(), OHQUUIDDefines.Characteristic.WeightMeasurement.uuid()));
                }
            }
            if (characteristics.isEmpty()) {
                OHQLog.e("characteristics.isEmpty()");
                _abort(OHQCompletionReason.OperationNotSupported);
                return;
            }
            for (CBCharacteristic characteristic : characteristics) {
                mPeripheral.setNotifyValue(true, characteristic);
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_UPDATE_NOTIFY_SUCCESS: {
                    handle = StateMachine.HANDLED;
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    mNotificationEnabledCharacteristicUUIDs.add(characteristic.uuid());
                    characteristics.remove(characteristic);
                    if (characteristics.isEmpty()) {
                        State transitionState = null;
                        if (mNotificationEnabledCharacteristicUUIDs.contains(OHQUUIDDefines.Characteristic.UserControlPoint.uuid())) {
                            if (mOptions.containsKey(RegisterNewUserKey)) {
                                transitionState = mUserRegisteringState;
                            } else if (null != mOptions.get(OHQSessionOptionKey.UserIndexKey)) {
                                mAuthenticateUserIndex = Types.autoCast(mOptions.get(OHQSessionOptionKey.UserIndexKey));
                                transitionState = mUserAuthenticatingState;
                            }
                        }
                        if (null == transitionState) {
                            if (mOptions.containsKey(ReadMeasurementRecordsKey) &&
                                    mNotificationEnabledCharacteristicUUIDs.contains(OHQUUIDDefines.Characteristic.RecordAccessControlPoint.uuid())) {
                                transitionState = mMeasurementRecordAccessControllingState;
                            } else {
                                transitionState = mIdleState;
                            }
                        }
                        transitionTo(transitionState);
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }
    }

    private class NotificationEnabledState extends State {

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mMeasurementRecords.clear();
            mWroteCurrentTime = false;
            mMultiplePacketMeasurementData = null;
            mPartialMeasurementRecord = null;
            OHQLog.d(mNotificationEnabledCharacteristicUUIDs.toString());
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_UPDATE_CHAR_SUCCESS: {
                    Object[] objects = (Object[]) msg.obj;
                    CBCharacteristic characteristic = (CBCharacteristic) objects[0];
                    byte[] value = (byte[]) objects[1];
                    if (OHQUUIDDefines.Characteristic.BatteryLevel.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForBatteryLevelCharacteristic(characteristic);
                    } else if (OHQUUIDDefines.Characteristic.CurrentTime.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForCurrentTimeCharacteristic(characteristic);
                    } else if (OHQUUIDDefines.Characteristic.BloodPressureMeasurement.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForBloodPressureMeasurementCharacteristic(characteristic);
                    } else if (OHQUUIDDefines.Characteristic.WeightMeasurement.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForWeightMeasurementCharacteristic(characteristic);
                    } else if (OHQUUIDDefines.Characteristic.BodyCompositionMeasurement.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForBodyCompositionMeasurementCharacteristic(value);
                    } else if (OHQUUIDDefines.Characteristic.OmronMeasurementBP.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForOmronMeasurementBPCharacteristic(value);
                    } else if (OHQUUIDDefines.Characteristic.OmronMeasurementWS.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForOmronMeasurementWSCharacteristic(value);
                    }
                    break;
                }
                case EVT_WRITE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    if (OHQUUIDDefines.Characteristic.CurrentTime.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        OHQLog.d("CTS WriteCharacteristic Success.");
                    }
                    break;
                }
                case EVT_WRITE_CHAR_FAILURE: {
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    if (OHQUUIDDefines.Characteristic.CurrentTime.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        int status = msg.arg1;
                        if (CBStatusCode.GATT_NO_RESOURCES == status) {   // 0x80: WriteCharacteristic Request Rejected
                            // If the slave sends error response in CTS,
                            // you don't retry and should send next request.
                            OHQLog.i("CTS WriteCharacteristic Request Rejected. (0x80)");
                        } else if (CBStatusCode.GATT_ERROR == status) {   // 0x85: WriteCharacteristic Request Rejected
                            // The status, 0x80 (Data filed ignored) will be notified same status to the application
                            // but there are cases when notified other status, 0x85 to the application in some smartphones.
                            // So the application need to regard as 0x80 only for Current Time Characteristic.
                            OHQLog.w("CTS WriteCharacteristic Request Rejected. (0x85)");
                        } else {
                            OHQLog.e("CTS WriteCharacteristic Failure. (" + format(Locale.US, "0x%x", status) + ")");
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForBatteryLevelCharacteristic(@NonNull CBCharacteristic characteristic) {
            int batteryLevel = characteristic.value()[0];
            OHQLog.i("Battery Level " + batteryLevel);
            mDelegate.dataObserver(OHQDataType.BatteryLevel, batteryLevel);
        }

        private void _didUpdateValueForCurrentTimeCharacteristic(@NonNull CBCharacteristic characteristic) {
            byte[] data = characteristic.value();
            String date = Bytes.parse7BytesAsDateString(data, 0, true);
            int weekOfDay = data[7];
            int fractions = data[8];
            int adjustReason = data[9];

            OHQLog.i(format(Locale.US, "%s WeekOfDay:%d Fractions256:%d AdjustReason:0x%02x",
                    date, weekOfDay, fractions, adjustReason));

            mDelegate.dataObserver(OHQDataType.CurrentTime, date);

            if (!mWroteCurrentTime) {
                _writeOfCurrentTimeCharacteristicsForPeripheral(characteristic);
                mWroteCurrentTime = true;
            }
        }

        private void _didUpdateValueForBloodPressureMeasurementCharacteristic(@NonNull CBCharacteristic characteristic) {
            OHQLog.vMethodIn();
            BloodPressureMeasurement data = new BloodPressureMeasurement(characteristic.value());
            OHQLog.i(data.toString());

            Map<OHQMeasurementRecordKey, Object> measurementRecord = new HashMap<OHQMeasurementRecordKey, Object>() {
                @Override
                public Object put(OHQMeasurementRecordKey key, Object value) {
                    return null != value ? super.put(key, value) : null;
                }
            };
            measurementRecord.put(OHQMeasurementRecordKey.BloodPressureUnitKey, data.getUnit());
            measurementRecord.put(OHQMeasurementRecordKey.SystolicKey, data.getSystolic());
            measurementRecord.put(OHQMeasurementRecordKey.DiastolicKey, data.getDiastolic());
            measurementRecord.put(OHQMeasurementRecordKey.MeanArterialPressureKey, data.getMeanArterialPressure());
            measurementRecord.put(OHQMeasurementRecordKey.TimeStampKey, data.getTimeStamp());
            measurementRecord.put(OHQMeasurementRecordKey.PulseRateKey, data.getPulseRate());
            measurementRecord.put(OHQMeasurementRecordKey.UserIndexKey, data.getUserID());
            measurementRecord.put(OHQMeasurementRecordKey.BloodPressureMeasurementStatusKey, data.getMeasurementStatus());
            mMeasurementRecords.add(measurementRecord);
        }

        private void _didUpdateValueForWeightMeasurementCharacteristic(@NonNull CBCharacteristic characteristic) {
            OHQLog.vMethodIn();
            WeightMeasurement data = new WeightMeasurement(characteristic.value());
            OHQLog.i(data.toString());

            Map<OHQMeasurementRecordKey, Object> measurementRecord = new HashMap<OHQMeasurementRecordKey, Object>() {
                @Override
                public Object put(OHQMeasurementRecordKey key, Object value) {
                    return null != value ? super.put(key, value) : null;
                }
            };
            measurementRecord.put(OHQMeasurementRecordKey.WeightUnitKey, data.getWeightUnit());
            measurementRecord.put(OHQMeasurementRecordKey.WeightKey, data.getWeight());
            measurementRecord.put(OHQMeasurementRecordKey.TimeStampKey, data.getTimeStamp());
            measurementRecord.put(OHQMeasurementRecordKey.UserIndexKey, data.getUserID());
            measurementRecord.put(OHQMeasurementRecordKey.BMIKey, data.getBMI());
            measurementRecord.put(OHQMeasurementRecordKey.HeightUnitKey, "cm");
            BigDecimal height = null != data.getHeight() ? data.getHeight().multiply(new BigDecimal("100.0")).setScale(1, RoundingMode.HALF_UP) : null;
            measurementRecord.put(OHQMeasurementRecordKey.HeightKey, height);

            if (OHQDeviceCategory.BodyCompositionMonitor == mDeviceCategory) {
                if (null != mPartialMeasurementRecord) {
                    throw new IllegalStateException("BodyCompositionMeasurement has not been received.");
                }
                mPartialMeasurementRecord = measurementRecord;
            } else {
                mMeasurementRecords.add(measurementRecord);
            }
        }

        private void _didUpdateValueForBodyCompositionMeasurementCharacteristic(@NonNull byte[] value) {
            OHQLog.vMethodIn();
            BodyCompositionMeasurement data;
            EnumSet<BodyCompositionMeasurement.Flag> flags = BodyCompositionMeasurement.Flag.parse(Bytes.parse2BytesAsInt(value, 0, true));
            OHQLog.d(flags.toString());
            if (null == mMultiplePacketMeasurementData) {
                if (flags.contains(BodyCompositionMeasurement.Flag.MultiplePacketMeasurement)) {
                    mMultiplePacketMeasurementData = value;
                    OHQLog.d("MultiplePacketMeasurement partial. value:" + Bytes.toHexString(mMultiplePacketMeasurementData));
                    data = null;
                } else {
                    OHQLog.d("SinglePacketMeasurement");
                    data = new BodyCompositionMeasurement(value);
                }
            } else {
                OHQLog.d("MultiplePacketMeasurement complete");
                data = new BodyCompositionMeasurement(mMultiplePacketMeasurementData, value);
                mMultiplePacketMeasurementData = null;
            }
            if (null == data) {
                OHQLog.d("Second data waiting ");
                return;
            }
            OHQLog.i(data.toString());

            if (OHQDeviceCategory.BodyCompositionMonitor != mDeviceCategory) {
                throw new IllegalStateException("OHQDeviceCategory.BodyCompositionMonitor != mDeviceCategory");
            }
            if (null == mPartialMeasurementRecord) {
                throw new IllegalStateException("WeightMeasurement has not been received.");
            }

            Map<OHQMeasurementRecordKey, Object> measurementRecord = new HashMap<OHQMeasurementRecordKey, Object>() {
                @Override
                public Object put(OHQMeasurementRecordKey key, Object value) {
                    return null != value ? super.put(key, value) : null;
                }
            };
            measurementRecord.putAll(mPartialMeasurementRecord);
            mPartialMeasurementRecord = null;
            measurementRecord.put(OHQMeasurementRecordKey.WeightUnitKey, data.getWeightUnit());
            measurementRecord.put(OHQMeasurementRecordKey.BodyFatPercentageKey, data.getBodyFatPercentage());
            measurementRecord.put(OHQMeasurementRecordKey.TimeStampKey, data.getTimeStamp());
            measurementRecord.put(OHQMeasurementRecordKey.UserIndexKey, data.getUserID());
            measurementRecord.put(OHQMeasurementRecordKey.BasalMetabolismKey, data.getBasalMetabolism());
            measurementRecord.put(OHQMeasurementRecordKey.MusclePercentageKey, data.getMusclePercentage());
            measurementRecord.put(OHQMeasurementRecordKey.MuscleMassKey, data.getMuscleMass());
            measurementRecord.put(OHQMeasurementRecordKey.FatFreeMassKey, data.getFatFreeMass());
            measurementRecord.put(OHQMeasurementRecordKey.SoftLeanMassKey, data.getSoftLeanMass());
            measurementRecord.put(OHQMeasurementRecordKey.BodyWaterMassKey, data.getBodyWaterMass());
            measurementRecord.put(OHQMeasurementRecordKey.ImpedanceKey, data.getImpedance());
            measurementRecord.put(OHQMeasurementRecordKey.WeightKey, data.getWeight());
            measurementRecord.put(OHQMeasurementRecordKey.HeightUnitKey, "cm");
            BigDecimal height = null != data.getHeight() ? data.getHeight().multiply(new BigDecimal("100.0")).setScale(1, RoundingMode.HALF_UP) : null;
            measurementRecord.put(OHQMeasurementRecordKey.HeightKey, height);
            mMeasurementRecords.add(measurementRecord);
        }

        private void _didUpdateValueForOmronMeasurementBPCharacteristic(@NonNull byte[] value) {
            OHQLog.vMethodIn();
            OmronMeasurementBP data;
            EnumSet<OmronMeasurementBP.Flag> flags = OmronMeasurementBP.Flag.parse(Bytes.parse2BytesAsInt(value, 0, true));
            OHQLog.d(flags.toString());
            if (null == mMultiplePacketMeasurementData) {
                if (flags.contains(OmronMeasurementBP.Flag.MultiplePacketMeasurement)) {
                    mMultiplePacketMeasurementData = value;
                    OHQLog.d("MultiplePacketMeasurement partial. value:" + Bytes.toHexString(mMultiplePacketMeasurementData));
                    data = null;
                } else {
                    OHQLog.d("SinglePacketMeasurement");
                    data = new OmronMeasurementBP(value);
                }
            } else {
                OHQLog.d("MultiplePacketMeasurement complete");
                data = new OmronMeasurementBP(mMultiplePacketMeasurementData, value);
                mMultiplePacketMeasurementData = null;
            }
            if (null == data) {
                OHQLog.d("Second data waiting ");
                return;
            }
            OHQLog.i(data.toString());

            Map<OHQMeasurementRecordKey, Object> measurementRecord = new HashMap<OHQMeasurementRecordKey, Object>() {
                @Override
                public Object put(OHQMeasurementRecordKey key, Object value) {
                    return null != value ? super.put(key, value) : null;
                }
            };
            measurementRecord.put(OHQMeasurementRecordKey.BloodPressureUnitKey, data.getUnit());
            measurementRecord.put(OHQMeasurementRecordKey.SequenceNumberKey, data.getSequenceNumber());
            measurementRecord.put(OHQMeasurementRecordKey.SystolicKey, data.getSystolic());
            measurementRecord.put(OHQMeasurementRecordKey.DiastolicKey, data.getDiastolic());
            measurementRecord.put(OHQMeasurementRecordKey.MeanArterialPressureKey, data.getMeanArterialPressure());
            measurementRecord.put(OHQMeasurementRecordKey.TimeStampKey, data.getTimeStamp());
            measurementRecord.put(OHQMeasurementRecordKey.PulseRateKey, data.getPulseRate());
            measurementRecord.put(OHQMeasurementRecordKey.UserIndexKey, data.getUserID());
            measurementRecord.put(OHQMeasurementRecordKey.BloodPressureMeasurementStatusKey, data.getMeasurementStatus());
            measurementRecord.put(OHQMeasurementRecordKey.ContinuousNumberOfMeasurementsKey, data.getContinuousNumberOfMeasurements());
            measurementRecord.put(OHQMeasurementRecordKey.ArtifactDetectionCountKey, data.getArtifactDetectionCount());
            measurementRecord.put(OHQMeasurementRecordKey.ArrhythmiaDetectionCountKey, data.getArrhythmiaDetectionCount());
            measurementRecord.put(OHQMeasurementRecordKey.RoomTemperatureKey, data.getRoomTemperature());
            mMeasurementRecords.add(measurementRecord);
        }

        private void _didUpdateValueForOmronMeasurementWSCharacteristic(@NonNull byte[] value) {
            OHQLog.vMethodIn();
            OmronMeasurementWS data;
            EnumSet<OmronMeasurementWS.Flag> flags = OmronMeasurementWS.Flag.parse((Bytes.parse4BytesAsInt(value, 0, true) & 0x00ffffff));
            OHQLog.d(flags.toString());
            if (null == mMultiplePacketMeasurementData) {
                if (flags.contains(OmronMeasurementWS.Flag.MultiplePacketMeasurement)) {
                    mMultiplePacketMeasurementData = value;
                    OHQLog.d("MultiplePacketMeasurement partial. value:" + Bytes.toHexString(mMultiplePacketMeasurementData));
                    data = null;
                } else {
                    OHQLog.d("SinglePacketMeasurement");
                    data = new OmronMeasurementWS(value);
                }
            } else {
                OHQLog.d("MultiplePacketMeasurement complete");
                data = new OmronMeasurementWS(mMultiplePacketMeasurementData, value);
                mMultiplePacketMeasurementData = null;
            }
            if (null == data) {
                OHQLog.d("Second data waiting ");
                return;
            }
            OHQLog.i(data.toString());

            Map<OHQMeasurementRecordKey, Object> measurementRecord = new HashMap<OHQMeasurementRecordKey, Object>() {
                @Override
                public Object put(OHQMeasurementRecordKey key, Object value) {
                    return null != value ? super.put(key, value) : null;
                }
            };
            measurementRecord.put(OHQMeasurementRecordKey.WeightUnitKey, data.getWeightUnit());
            measurementRecord.put(OHQMeasurementRecordKey.SequenceNumberKey, data.getSequenceNumber());
            measurementRecord.put(OHQMeasurementRecordKey.WeightKey, data.getWeight());
            measurementRecord.put(OHQMeasurementRecordKey.TimeStampKey, data.getTimeStamp());
            measurementRecord.put(OHQMeasurementRecordKey.UserIndexKey, data.getUserID());
            measurementRecord.put(OHQMeasurementRecordKey.BMIKey, data.getBMI());
            measurementRecord.put(OHQMeasurementRecordKey.HeightUnitKey, "cm");
            BigDecimal height = null != data.getHeight() ? data.getHeight().multiply(new BigDecimal("100.0")).setScale(1, RoundingMode.HALF_UP) : null;
            measurementRecord.put(OHQMeasurementRecordKey.HeightKey, height);
            measurementRecord.put(OHQMeasurementRecordKey.BodyFatPercentageKey, data.getBodyFatPercentage());
            measurementRecord.put(OHQMeasurementRecordKey.BasalMetabolismKey, data.getBasalMetabolism());
            measurementRecord.put(OHQMeasurementRecordKey.MusclePercentageKey, data.getMusclePercentage());
            measurementRecord.put(OHQMeasurementRecordKey.MuscleMassKey, data.getMuscleMass());
            measurementRecord.put(OHQMeasurementRecordKey.FatFreeMassKey, data.getFatFreeMass());
            measurementRecord.put(OHQMeasurementRecordKey.SoftLeanMassKey, data.getSoftLeanMass());
            measurementRecord.put(OHQMeasurementRecordKey.BodyWaterMassKey, data.getBodyWaterMass());
            measurementRecord.put(OHQMeasurementRecordKey.ImpedanceKey, data.getImpedance());
            measurementRecord.put(OHQMeasurementRecordKey.SkeletalMusclePercentageKey, data.getSkeletalMusclePercentage());
            measurementRecord.put(OHQMeasurementRecordKey.VisceralFatLevelKey, data.getVisceralFatLevel());
            measurementRecord.put(OHQMeasurementRecordKey.BodyAgeKey, data.getBodyAge());
            measurementRecord.put(OHQMeasurementRecordKey.BodyFatPercentageStageEvaluationKey, data.getBodyFatPercentageStageEvaluation());
            measurementRecord.put(OHQMeasurementRecordKey.SkeletalMusclePercentageStageEvaluationKey, data.getSkeletalMusclePercentageStageEvaluation());
            measurementRecord.put(OHQMeasurementRecordKey.VisceralFatLevelStageEvaluationKey, data.getVisceralFatLevelStageEvaluation());
            mMeasurementRecords.add(measurementRecord);
        }

        private void _writeOfCurrentTimeCharacteristicsForPeripheral(@NonNull CBCharacteristic characteristic) {
            byte[] data = new byte[10];
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            data[0] = (byte) year;
            data[1] = (byte) ((year >> 8) & 0xFF);
            data[2] = (byte) (cal.get(Calendar.MONTH) + 1);
            data[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
            data[4] = (byte) cal.get(Calendar.HOUR_OF_DAY);
            data[5] = (byte) cal.get(Calendar.MINUTE);
            data[6] = (byte) cal.get(Calendar.SECOND);
            data[7] = (byte) ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1); // Rotate
            data[8] = (byte) (cal.get(Calendar.MILLISECOND) * 256 / 1000); // Fractions256
            data[9] = 0x01; // Adjust Reason: Manual time update
            String date = format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d WeekOfDay:%d Fractions256:%d AdjustReason:0x%02x",
                    year, data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
            StringBuilder sb = new StringBuilder("");
            for (byte b : data) {
                sb.append(format(Locale.US, "%02x ", b));
            }
            OHQLog.i("CTS Tx Time:" + date);
            OHQLog.i("CTS Tx Data:" + sb.toString());

            mPeripheral.writeValue(data, characteristic, CBCharacteristicWriteType.WithResponse);
        }
    }

    private class UserRegisteringState extends State {
        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.UserRegistering);
            CBCharacteristic userControlPointCharacteristic = _getCharacteristic(
                    OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.UserControlPoint.uuid());
            if (null == userControlPointCharacteristic) {
                OHQLog.e("null == userControlPointCharacteristic");
                _abort(OHQCompletionReason.FailedToRegisterUser);
                return;
            }
            Integer userIndex = Types.autoCast(mOptions.get(OHQSessionOptionKey.UserIndexKey));
            int consentCode = OHQDeviceManager.DEFAULT_CONSENT_CODE;
            if (mOptions.containsKey(OHQSessionOptionKey.ConsentCodeKey)) {
                consentCode = Types.autoCast(mOptions.get(OHQSessionOptionKey.ConsentCodeKey));
            }
            UserControlPoint.Request request;
            if (null == userIndex) {
                request = UserControlPoint.newRegisterNewUser(consentCode);
            } else {
                request = UserControlPoint.newRegisterNewUserWithUserIndex(userIndex, consentCode);
            }
            OHQLog.d(request.toString());
            mPeripheral.writeValue(request.getPacket(), userControlPointCharacteristic, CBCharacteristicWriteType.WithResponse);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_WRITE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    if (OHQUUIDDefines.Characteristic.UserControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                    }
                    break;
                }
                case EVT_UPDATE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) ((Object[])msg.obj)[0];
                    if (OHQUUIDDefines.Characteristic.UserControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForUserControlPointCharacteristic(characteristic);
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForUserControlPointCharacteristic(@NonNull CBCharacteristic characteristic) {
            OHQLog.vMethodIn();
            UserControlPoint.Response response = UserControlPoint.parseResponse(characteristic.value());
            OHQLog.d(response.toString());
            if (UserControlPoint.ResponseValue.Success != response.responseValue) {
                OHQLog.e("UserControlPoint.ResponseValue.Success != response.responseValue");
                _abort(OHQCompletionReason.FailedToRegisterUser);
                return;
            }
            final Integer registeredUserIndex;
            switch (response.requestOpCode) {
                case RegisterNewUser: {
                    registeredUserIndex = response.userIndex;
                    break;
                }
                case RegisterNewUserWithUserIndex: {
                    registeredUserIndex = Types.autoCast(mOptions.get(OHQSessionOptionKey.UserIndexKey));
                    break;
                }
                default:
                    OHQLog.e("Illegal request operation code. " + response.requestOpCode.name());
                    _abort(OHQCompletionReason.FailedToRegisterUser);
                    return;
            }
            mDelegate.dataObserver(OHQDataType.RegisteredUserIndex, registeredUserIndex);
            if (null != mOptions.get(OHQSessionOptionKey.UserDataKey)) {
                mAuthenticateUserIndex = registeredUserIndex;
                transitionTo(mUserAuthenticatingState);
            } else {
                transitionTo(mIdleState);
            }
        }
    }

    private class UserAuthenticatingState extends State {
        int authenticateUserIndex;

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.UserAuthenticating);
            CBCharacteristic userControlPointCharacteristic = _getCharacteristic(
                    OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.UserControlPoint.uuid());
            if (null == userControlPointCharacteristic) {
                OHQLog.e("null == userControlPointCharacteristic");
                _abort(OHQCompletionReason.FailedToAuthenticateUser);
                return;
            }

            if (null == mAuthenticateUserIndex) {
                OHQLog.e("null == mAuthenticateUserIndex");
                _abort(OHQCompletionReason.FailedToAuthenticateUser);
                return;
            }
            authenticateUserIndex = mAuthenticateUserIndex;

            int consentCode = OHQDeviceManager.DEFAULT_CONSENT_CODE;
            if (mOptions.containsKey(OHQSessionOptionKey.ConsentCodeKey)) {
                consentCode = Types.autoCast(mOptions.get(OHQSessionOptionKey.ConsentCodeKey));
            }

            UserControlPoint.Request request = UserControlPoint.newConsent(authenticateUserIndex, consentCode);
            OHQLog.d(request.toString());
            mPeripheral.writeValue(request.getPacket(), userControlPointCharacteristic, CBCharacteristicWriteType.WithResponse);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_WRITE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    if (OHQUUIDDefines.Characteristic.UserControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                    }
                    break;
                }
                case EVT_UPDATE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) ((Object[])msg.obj)[0];
                    if (OHQUUIDDefines.Characteristic.UserControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForUserControlPointCharacteristic(characteristic);
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForUserControlPointCharacteristic(@NonNull CBCharacteristic characteristic) {
            OHQLog.vMethodIn();
            UserControlPoint.Response response = UserControlPoint.parseResponse(characteristic.value());
            OHQLog.d(response.toString());
            if (UserControlPoint.OpCode.Consent != response.requestOpCode) {
                OHQLog.e("Illegal request operation code. " + response.requestOpCode.name());
                _abort(OHQCompletionReason.FailedToAuthenticateUser);
                return;
            }
            if (UserControlPoint.ResponseValue.Success != response.responseValue) {
                OHQLog.e("UserControlPoint.ResponseValue.Success != response.responseValue");
                _abort(OHQCompletionReason.FailedToRegisterUser);
                return;
            }
            mDelegate.dataObserver(OHQDataType.AuthenticatedUserIndex, authenticateUserIndex);
            if (mOptions.containsKey(OHQSessionOptionKey.DeleteUserDataKey)) {
                transitionTo(mUserDataDeletingState);
            } else if (mOptions.containsKey(DatabaseChangeIncrementValueKey)) {
                transitionTo(mDatabaseChangeIncrementNotificationWaitingState);
            } else if (mOptions.containsKey(OHQSessionOptionKey.UserDataKey)) {
                final Map<OHQUserDataKey, Object> appUserData;
                if (!mOptions.containsKey(OHQSessionOptionKey.UserDataKey)) {
                    _abort(OHQCompletionReason.FailedToSetUserData);
                    return;
                }
                appUserData = Types.autoCast(mOptions.get(OHQSessionOptionKey.UserDataKey));
                final List<OHQUserDataKey> missingUserDataKeys = new LinkedList<>();
                for (OHQUserDataKey key : mSupportedUserDataCharacteristics.keySet()) {
                    if (!appUserData.containsKey(key)) {
                        missingUserDataKeys.add(key);
                    }
                }
                if (0 < missingUserDataKeys.size()) {
                    OHQLog.e("User data setting failed because incomplete user data was specified. missing:" + missingUserDataKeys.toString());
                    _abort(OHQCompletionReason.FailedToSetUserData);
                    return;
                }
                mLatestUserData.putAll(appUserData);
                transitionTo(mUserDataWritingState);
            } else if (mOptions.containsKey(ReadMeasurementRecordsKey) &&
                    mNotificationEnabledCharacteristicUUIDs.contains(OHQUUIDDefines.Characteristic.RecordAccessControlPoint.uuid())) {
                transitionTo(mMeasurementRecordAccessControllingState);
            } else {
                transitionTo(mIdleState);
            }
        }
    }

    private class UserDataDeletingState extends State {
        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.UserDataDeleting);
            CBCharacteristic userControlPointCharacteristic = _getCharacteristic(
                    OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.UserControlPoint.uuid());
            if (null == userControlPointCharacteristic) {
                OHQLog.e("null == userControlPointCharacteristic");
                _abort(OHQCompletionReason.FailedToDeleteUser);
                return;
            }
            UserControlPoint.Request request = UserControlPoint.newDeleteUserData();
            OHQLog.d(request.toString());
            mPeripheral.writeValue(request.getPacket(), userControlPointCharacteristic, CBCharacteristicWriteType.WithResponse);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_WRITE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    if (OHQUUIDDefines.Characteristic.UserControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                    }
                    break;
                }
                case EVT_UPDATE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) ((Object[])msg.obj)[0];
                    if (OHQUUIDDefines.Characteristic.UserControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        UserControlPoint.Response response = UserControlPoint.parseResponse(characteristic.value());
                        OHQLog.d(response.toString());
                        if (UserControlPoint.OpCode.DeleteUserData != response.requestOpCode) {
                            OHQLog.e("Illegal request operation code. " + response.requestOpCode.name());
                            _abort(OHQCompletionReason.FailedToDeleteUser);
                            break;
                        }
                        if (UserControlPoint.ResponseValue.Success != response.responseValue) {
                            OHQLog.e("UserControlPoint.ResponseValue.Success != response.responseValue");
                            _abort(OHQCompletionReason.FailedToDeleteUser);
                            break;
                        }
                        int userIndex = Types.autoCast(mOptions.get(OHQSessionOptionKey.UserIndexKey));
                        mDelegate.dataObserver(OHQDataType.DeletedUserIndex, userIndex);
                        transitionTo(mIdleState);
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }
    }

    private class DatabaseChangeIncrementNotificationWaitingState extends State {
        @NonNull
        private Long appDatabaseChangeIncrementValue = Long.valueOf("0");
        @Nullable
        private Boolean updateFlag;

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.WaitingForUpdateOfDatabaseChangeIncrement);
            Long appDatabaseChangeIncrementValue = Types.autoCast(mOptions.get(OHQSessionOptionKey.DatabaseChangeIncrementValueKey));
            if (null == appDatabaseChangeIncrementValue) {
                _abort(OHQCompletionReason.OperationNotSupported);
                return;
            }
            this.appDatabaseChangeIncrementValue = appDatabaseChangeIncrementValue;
            this.updateFlag = Types.autoCast(mOptions.get(OHQSessionOptionKey.UserDataUpdateFlagKey));
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_UPDATE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) ((Object[])msg.obj)[0];
                    if (OHQUUIDDefines.Characteristic.DatabaseChangeIncrement.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForDatabaseChangeIncrementCharacteristic(characteristic);
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForDatabaseChangeIncrementCharacteristic(@NonNull CBCharacteristic characteristic) {
            long deviceDatabaseChangeIncrementValue = Bytes.parse4BytesAsLong(characteristic.value(), 0, true);
            if (deviceDatabaseChangeIncrementValue > appDatabaseChangeIncrementValue) {
                OHQLog.i(String.format(Locale.US, "User Data Synchronization case a : Server(%d) > Client(%d)",
                        deviceDatabaseChangeIncrementValue, appDatabaseChangeIncrementValue));
                mLatestDatabaseChangeIncrement = deviceDatabaseChangeIncrementValue;
                transitionTo(mUserDataReadingState);
            } else {
                final Map<OHQUserDataKey, Object> appUserData;
                if (mOptions.containsKey(OHQSessionOptionKey.UserDataKey)) {
                    appUserData = Types.autoCast(mOptions.get(OHQSessionOptionKey.UserDataKey));
                } else {
                    appUserData = new HashMap<>();
                }
                final List<OHQUserDataKey> missingUserDataKeys = new LinkedList<>();
                for (OHQUserDataKey key : mSupportedUserDataCharacteristics.keySet()) {
                    if (!appUserData.containsKey(key)) {
                        missingUserDataKeys.add(key);
                    }
                }
                if (0 == deviceDatabaseChangeIncrementValue) {
                    if (0 < missingUserDataKeys.size()) {
                        OHQLog.e("User data setting failed because incomplete user data was specified. missing:" + missingUserDataKeys.toString());
                        _abort(OHQCompletionReason.FailedToSetUserData);
                        return;
                    }
                    updateFlag = true;
                }
                mLatestUserData.putAll(appUserData);
                if (deviceDatabaseChangeIncrementValue < appDatabaseChangeIncrementValue) {
                    OHQLog.i(String.format(Locale.US, "User Data Synchronization case b : Server(%d) < Client(%d)",
                            deviceDatabaseChangeIncrementValue, appDatabaseChangeIncrementValue));
                    mLatestDatabaseChangeIncrement = appDatabaseChangeIncrementValue;
                } else if (null != updateFlag && updateFlag) {
                    OHQLog.i(String.format(Locale.US, "User Data Synchronization case c : Server(%d) == Client(%d) (Updated)",
                            deviceDatabaseChangeIncrementValue, appDatabaseChangeIncrementValue));
                    mLatestDatabaseChangeIncrement = deviceDatabaseChangeIncrementValue + 1L;
                } else {
                    OHQLog.i(String.format(Locale.US, "User Data Synchronization case d : Server(%d) == Client(%d)",
                            deviceDatabaseChangeIncrementValue, appDatabaseChangeIncrementValue));
                    mLatestDatabaseChangeIncrement = deviceDatabaseChangeIncrementValue;
                }
                if (0 < missingUserDataKeys.size()) {
                    transitionTo(mUserDataReadingState);
                } else {
                    transitionTo(mUserDataWritingState);
                }
            }
        }
    }

    private class UserDataReadingState extends State {
        @NonNull
        private final List<CBCharacteristic> characteristics = new LinkedList<CBCharacteristic>() {
            @Override
            public boolean add(CBCharacteristic characteristic) {
                return null != characteristic && super.add(characteristic);
            }
        };

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.WriteUserDataPreparing);
            for (Map.Entry<OHQUserDataKey, CBCharacteristic> e : mSupportedUserDataCharacteristics.entrySet()) {
                if (!mLatestUserData.keySet().contains(e.getKey())) {
                    characteristics.add(e.getValue());
                }
            }
            if (characteristics.isEmpty()) {
                transitionTo(mUserDataWritingState);
            } else {
                for (CBCharacteristic characteristic : characteristics) {
                    mPeripheral.readValue(characteristic);
                }
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_UPDATE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) ((Object[])msg.obj)[0];
                    if (characteristics.contains(characteristic)) {
                        handle = StateMachine.HANDLED;
                        if (OHQUUIDDefines.Characteristic.DateofBirth.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForDateOfBirthCharacteristic(characteristic);
                        } else if (OHQUUIDDefines.Characteristic.Gender.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForGenderCharacteristic(characteristic);
                        } else if (OHQUUIDDefines.Characteristic.Height.uuid().equals(characteristic.uuid())) {
                            _didUpdateValueForHeightCharacteristic(characteristic);
                        }
                        characteristics.remove(characteristic);
                        if (characteristics.isEmpty()) {
                            transitionTo(mUserDataWritingState);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForDateOfBirthCharacteristic(@NonNull CBCharacteristic characteristic) {
            byte[] data = characteristic.value();
            int year = Bytes.parse2BytesAsShort(data, 0, true);
            int month = data[2];
            int day = data[3];
            String dateOfBirthString = String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
            mLatestUserData.put(OHQUserDataKey.DateOfBirthKey, dateOfBirthString);
            OHQLog.i("dateOfBirth:" + dateOfBirthString);
        }

        private void _didUpdateValueForGenderCharacteristic(@NonNull CBCharacteristic characteristic) {
            byte genderValue = characteristic.value()[0];
            OHQGender gender = genderValue == 0 ? OHQGender.Male : OHQGender.Female;
            mLatestUserData.put(OHQUserDataKey.GenderKey, gender);
            OHQLog.i("gender:" + gender.name());
        }

        private void _didUpdateValueForHeightCharacteristic(@NonNull CBCharacteristic characteristic) {
            CBDescriptor characteristicPresentationFormatDescriptor = _getDescriptor(characteristic, OHQUUIDDefines.Descriptor.CharacteristicPresentationFormat.uuid());
            if (null == characteristicPresentationFormatDescriptor) {
                OHQLog.e("null == characteristicPresentationFormatDescriptor");
                return;
            }
            CharacteristicPresentationFormat characteristicPresentationFormat = new CharacteristicPresentationFormat(characteristicPresentationFormatDescriptor.value());
            BigDecimal heightValue = new BigDecimal(Bytes.parse2BytesAsInt(characteristic.value(), 0, true));
            BigDecimal heightInMeters = heightValue.multiply(new BigDecimal(Math.pow(10, characteristicPresentationFormat.exponent)));
            BigDecimal height = heightInMeters.multiply(new BigDecimal("100.0"));
            mLatestUserData.put(OHQUserDataKey.HeightKey, height);
            OHQLog.i("height:" + height + " cm");
        }
    }

    private class UserDataWritingState extends State {
        @NonNull
        private final Map<CBCharacteristic, byte[]> characteristics = new LinkedHashMap<CBCharacteristic, byte[]>() {
            @Override
            public byte[] put(CBCharacteristic key, byte[] value) {
                return null != key ? super.put(key, value) : null;
            }
        };
        @NonNull
        private final Map<OHQUserDataKey, Object> updatedUserData = new HashMap<>();
        @Nullable
        private Long updatedDatabaseChangeIncrement;

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.UserDataWriting);
            OHQLog.i(mLatestUserData.toString());
            CBCharacteristic dateOfBirthCharacteristic = _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.DateofBirth.uuid());
            if (null != dateOfBirthCharacteristic && null != mLatestUserData.get(OHQUserDataKey.DateOfBirthKey)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                try {
                    String dateOfBirthString = Types.autoCast(mLatestUserData.get(OHQUserDataKey.DateOfBirthKey));
                    Date date = sdf.parse(dateOfBirthString);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    byte[] data = new byte[4];
                    data[0] = (byte) (year & 0x000000ff);
                    data[1] = (byte) ((year >> 8) & 0x000000ff);
                    data[2] = (byte) (cal.get(Calendar.MONTH) + 1);
                    data[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
                    characteristics.put(dateOfBirthCharacteristic, data);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            CBCharacteristic genderCharacteristic = _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.Gender.uuid());
            if (null != genderCharacteristic && null != mLatestUserData.get(OHQUserDataKey.GenderKey)) {
                OHQGender gender = Types.autoCast(mLatestUserData.get(OHQUserDataKey.GenderKey));
                byte[] data = {OHQGender.Male == gender ? (byte) 0 : (byte) 1};
                characteristics.put(genderCharacteristic, data);
            }

            CBCharacteristic heightCharacteristic = _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.Height.uuid());
            if (null != heightCharacteristic && null != mLatestUserData.get(OHQUserDataKey.HeightKey) && null != mHeightCharacteristicPresentationFormat) {
                BigDecimal height = Types.autoCast(mLatestUserData.get(OHQUserDataKey.HeightKey));
                BigDecimal heightInMeters = height.multiply(new BigDecimal("0.01")); // in meters e.g.1.705m
                BigDecimal heightValue = heightInMeters.multiply(new BigDecimal(Math.pow(10, -mHeightCharacteristicPresentationFormat.exponent))); // e.g. 1705
                byte[] data = new byte[2];
                data[0] = (byte) (heightValue.intValue() & 0x000000ff);
                data[1] = (byte) ((heightValue.intValue() >> 8) & 0x000000ff);
                characteristics.put(heightCharacteristic, data);
            }


            CBCharacteristic databaseChangeIncrementCharacteristic = _getCharacteristic(OHQUUIDDefines.Service.UserData.uuid(), OHQUUIDDefines.Characteristic.DatabaseChangeIncrement.uuid());
            if (null != databaseChangeIncrementCharacteristic && null != mLatestDatabaseChangeIncrement) {
                byte[] data = new byte[4];
                data[0] = (byte) (mLatestDatabaseChangeIncrement & 0x000000ff);
                data[1] = (byte) ((mLatestDatabaseChangeIncrement >> 8) & 0x000000ff);
                data[2] = (byte) ((mLatestDatabaseChangeIncrement >> 16) & 0x000000ff);
                data[3] = (byte) ((mLatestDatabaseChangeIncrement >> 24) & 0x000000ff);
                characteristics.put(databaseChangeIncrementCharacteristic, data);
            }

            if (characteristics.isEmpty()) {
                _writeCompleted();
            } else {
                for (Map.Entry<CBCharacteristic, byte[]> e : characteristics.entrySet()) {
                    mPeripheral.writeValue(e.getValue(), e.getKey(), CBCharacteristicWriteType.WithResponse);
                }
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_WRITE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    if (characteristics.containsKey(characteristic)) {
                        handle = StateMachine.HANDLED;
                        if (OHQUUIDDefines.Characteristic.DateofBirth.uuid().equals(characteristic.uuid())) {
                            updatedUserData.put(OHQUserDataKey.DateOfBirthKey, mLatestUserData.get(OHQUserDataKey.DateOfBirthKey));
                        } else if (OHQUUIDDefines.Characteristic.Gender.uuid().equals(characteristic.uuid())) {
                            updatedUserData.put(OHQUserDataKey.GenderKey, mLatestUserData.get(OHQUserDataKey.GenderKey));
                        } else if (OHQUUIDDefines.Characteristic.Height.uuid().equals(characteristic.uuid())) {
                            updatedUserData.put(OHQUserDataKey.HeightKey, mLatestUserData.get(OHQUserDataKey.HeightKey));
                        } else if (OHQUUIDDefines.Characteristic.DatabaseChangeIncrement.uuid().equals(characteristic.uuid())) {
                            updatedDatabaseChangeIncrement = mLatestDatabaseChangeIncrement;
                        }
                        characteristics.remove(characteristic);
                        if (characteristics.isEmpty()) {
                            _writeCompleted();
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _writeCompleted() {
            OHQLog.i(updatedUserData.toString());
            if (!updatedUserData.isEmpty()) {
                mDelegate.dataObserver(OHQDataType.UserData, updatedUserData);
            }
            if (null != updatedDatabaseChangeIncrement) {
                mDelegate.dataObserver(OHQDataType.DatabaseChangeIncrement, updatedDatabaseChangeIncrement);
            }
            if (mOptions.containsKey(ReadMeasurementRecordsKey) &&
                    mNotificationEnabledCharacteristicUUIDs.contains(OHQUUIDDefines.Characteristic.RecordAccessControlPoint.uuid())) {
                transitionTo(mMeasurementRecordAccessControllingState);
            } else {
                transitionTo(mIdleState);
            }
        }
    }

    private class MeasurementRecordAccessControllingState extends State {

        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.MeasurementRecordAccessControlling);
            if (!mOptions.containsKey(AllowControlOfReadingPositionToMeasurementRecordsKey)) {
                OHQLog.d("!mOptions.containsKey(AllowControlOfReadingPositionToMeasurementRecordsKey)");
                return;
            }

            CBCharacteristic recordAccessControlPointCharacteristic = _getCharacteristic(
                    OHQUUIDDefines.Service.OmronOptionalService.uuid(), OHQUUIDDefines.Characteristic.RecordAccessControlPoint.uuid());
            if (null == recordAccessControlPointCharacteristic) {
                OHQLog.d("null == recordAccessControlPointCharacteristic");
                return;
            }

            Integer firstRecordSequenceNumber = Types.autoCast(mOptions.get(OHQSessionOptionKey.SequenceNumberOfFirstRecordToReadKey));
            final RecordAccessControlPoint.Request request;
            if (null == firstRecordSequenceNumber) {
                request = RecordAccessControlPoint.newReportNumberOfStoredRecordsOfAllRecords();
            } else {
                request = RecordAccessControlPoint.newReportNumberOfStoredRecordsOfGreaterThanOrEqualTo(firstRecordSequenceNumber);
            }
            OHQLog.d(request.toString());
            mPeripheral.writeValue(request.getPacket(), recordAccessControlPointCharacteristic, CBCharacteristicWriteType.WithResponse);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            boolean handle = StateMachine.NOT_HANDLED;
            switch (msg.what) {
                case EVT_WRITE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) msg.obj;
                    if (OHQUUIDDefines.Characteristic.RecordAccessControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                    }
                    break;
                }
                case EVT_UPDATE_CHAR_SUCCESS: {
                    CBCharacteristic characteristic = (CBCharacteristic) ((Object[])msg.obj)[0];
                    if (OHQUUIDDefines.Characteristic.RecordAccessControlPoint.uuid().equals(characteristic.uuid())) {
                        handle = StateMachine.HANDLED;
                        _didUpdateValueForRecordAccessControlPointCharacteristic(characteristic);
                    }
                    break;
                }
                default:
                    break;
            }
            return handle;
        }

        private void _didUpdateValueForRecordAccessControlPointCharacteristic(@NonNull CBCharacteristic characteristic) {
            OHQLog.vMethodIn();

            RecordAccessControlPoint.Response response = RecordAccessControlPoint.parseResponse(characteristic.value());
            OHQLog.d(response.toString());
            switch (response.opCode) {
                case NumberOfStoredRecordsResponse: {
                    Integer firstRecordSequenceNumber = Types.autoCast(mOptions.get(OHQSessionOptionKey.SequenceNumberOfFirstRecordToReadKey));
                    final RecordAccessControlPoint.Request request;
                    if (0 < response.numberOfRecords) {
                        if (null == firstRecordSequenceNumber) {
                            request = RecordAccessControlPoint.newReportStoredRecordsOfAllRecords();
                        } else {
                            request = RecordAccessControlPoint.newReportStoredRecordsOfGreaterThanOrEqualTo(firstRecordSequenceNumber);
                        }
                    } else {
                        request = RecordAccessControlPoint.newReportSequenceNumberOfLatestRecord();
                    }
                    OHQLog.d(request.toString());
                    mPeripheral.writeValue(request.getPacket(), characteristic, CBCharacteristicWriteType.WithResponse);
                    break;
                }
                case ResponseCode: {
                    if (response.requestOpCode == RecordAccessControlPoint.OpCode.ReportStoredRecords && response.responseValue == RecordAccessControlPoint.ResponseValue.Success) {
                        final RecordAccessControlPoint.Request request = RecordAccessControlPoint.newReportSequenceNumberOfLatestRecord();
                        OHQLog.d(request.toString());
                        mPeripheral.writeValue(request.getPacket(), characteristic, CBCharacteristicWriteType.WithResponse);
                    }
                    break;
                }
                case SequenceNumberOfLatestRecordResponse: {
                    if (INVALID_SEQUENCE_NUMBER != response.sequenceNumber) {
                        mDelegate.dataObserver(OHQDataType.SequenceNumberOfLatestRecord, response.sequenceNumber);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private class IdleState extends State {
        @Override
        public void enter(@Nullable Object[] transferObjects) {
            mDelegate.onStateChanged(OHQDetailedState.Idle);
        }
    }
}
