package jp.co.ohq.blesampleomron.view.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.DateUtil;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.view.dialog.EditTextDialog;
import jp.co.ohq.blesampleomron.view.dialog.SimpleDialog;
import jp.co.ohq.blesampleomron.view.widget.TwoLineTextViewWithoutImage;


public class GuestHomeFragment extends BaseFragment implements
        View.OnClickListener,
        DatePickerDialog.OnDateSetListener,
        EditTextDialog.Callback,
        SimpleDialog.Callback {

    private static final String ARG_TARGET_DEVICE = "ARG_TARGET_DEVICE";
    private static final int DIALOG_HEIGHT = 0;
    private static final int DIALOG_GENDER_LIST = 1;
    private final Map<OHQUserDataKey, Object> mUserData = new HashMap<>();
    private EventListener mListener;
    private DiscoveredDevice mTargetDevice;
    private TwoLineTextViewWithoutImage mDateOfBirthView;
    private TwoLineTextViewWithoutImage mHeightView;
    private TwoLineTextViewWithoutImage mGenderView;

    public static GuestHomeFragment newInstance(@Nullable DiscoveredDevice discoverDevice) {
        GuestHomeFragment fragment = new GuestHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TARGET_DEVICE, discoverDevice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AppLog.vMethodIn();
        super.onAttach(context);
        if (null != getParentFragment() && getParentFragment() instanceof GuestHomeFragment.EventListener) {
            mListener = (GuestHomeFragment.EventListener) getParentFragment();
        } else if (context instanceof GuestHomeFragment.EventListener) {
            mListener = (GuestHomeFragment.EventListener) context;
        } else {
            throw new AndroidRuntimeException("Parent is must be implement 'EventListener'");
        }
        Bundle args = getArguments();
        mTargetDevice = args.getParcelable(ARG_TARGET_DEVICE);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean guestCtrlFlg;
        View rootView = inflater.inflate(R.layout.fragment_guest_home, container, false);
        TwoLineTextViewWithoutImage deviceText = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.device_text);
        if (mTargetDevice == null) {
            guestCtrlFlg = false;
        } else {
            guestCtrlFlg = true;
            deviceText.setTitle(mTargetDevice.getCompleteLocalName());
            deviceText.setSummary(mTargetDevice.getAddress());
        }
        deviceText.setOnClickListener(this);

        TwoLineTextViewWithoutImage changeNormalText = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.change_normal_text);
        changeNormalText.setOnClickListener(this);
        changeNormalText.setEnabled(guestCtrlFlg);

        TwoLineTextViewWithoutImage changeUnregisteredText = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.change_unregistered_user_text);
        changeUnregisteredText.setOnClickListener(this);
        changeUnregisteredText.setEnabled(guestCtrlFlg);

        TwoLineTextViewWithoutImage receiveText = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.receive_measurement_records);
        receiveText.setOnClickListener(this);
        receiveText.setEnabled(guestCtrlFlg);

        mUserData.put(OHQUserDataKey.DateOfBirthKey, "2001-01-01");
        mUserData.put(OHQUserDataKey.HeightKey, new BigDecimal("170.5"));
        mUserData.put(OHQUserDataKey.GenderKey, OHQGender.Male);

        mDateOfBirthView = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.date_of_birth_text);
        mDateOfBirthView.setOnClickListener(this);
        mDateOfBirthView.setSummary((String) mUserData.get(OHQUserDataKey.DateOfBirthKey));

        mHeightView = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.height_text);
        mHeightView.setOnClickListener(this);
        mHeightView.setSummary(getString(R.string.height_label, mUserData.get(OHQUserDataKey.HeightKey)));

        mGenderView = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.gender_text);
        mGenderView.setOnClickListener(this);
        mGenderView.setSummary(((OHQGender) mUserData.get(OHQUserDataKey.GenderKey)).name());

        return rootView;
    }

    @Override
    public void onClick(View v) {
        AppLog.vMethodIn();
        Bundle bundle = new Bundle();

        switch (v.getId()) {
            case R.id.device_text:
                mListener.onFragmentEvent(Event.AddDevice, bundle);
                break;
            case R.id.change_normal_text:
                bundle.putParcelable(EventArg.DiscoveredDevice.name(), mTargetDevice);
                mListener.onFragmentEvent(Event.ChangeToNormalMode, bundle);
                break;
            case R.id.change_unregistered_user_text:
                bundle.putParcelable(EventArg.DiscoveredDevice.name(), mTargetDevice);
                mListener.onFragmentEvent(Event.ChangeToUnregisteredUserMode, bundle);
                break;
            case R.id.receive_measurement_records:
                bundle.putParcelable(EventArg.DiscoveredDevice.name(), mTargetDevice);
                bundle.putSerializable(EventArg.UserData.name(), new HashMap<>(mUserData));
                mListener.onFragmentEvent(Event.ReceiveMeasurementRecords, bundle);
                break;
            case R.id.date_of_birth_text:
                showDatePickerDialog((String) mUserData.get(OHQUserDataKey.DateOfBirthKey));
                break;
            case R.id.height_text:
                showHeightEditDialog((BigDecimal) mUserData.get(OHQUserDataKey.HeightKey));
                break;
            case R.id.gender_text:
                showGenderSelectDialog();
                break;
            default:
                break;
        }
    }

    private void showDatePickerDialog(@NonNull String initialDateOfBirth) {
        Calendar cal = DateUtil.toDate(initialDateOfBirth, DateUtil.FormatType.Form2);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
        dpd.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String ymd = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
        mUserData.put(OHQUserDataKey.DateOfBirthKey, ymd);
        mDateOfBirthView.setSummary(ymd);
    }

    private void showHeightEditDialog(@NonNull BigDecimal initialHeight) {
        EditTextDialog.newInstance(getString(R.string.height), initialHeight.toString()).show(getChildFragmentManager(), "");
    }

    @Override
    public void onEditTextDialogEdited(String resultCode) {
        AppLog.d(resultCode);
        try {
            onHeightEdited(new BigDecimal(resultCode));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEditTextDialogCanceled() {

    }

    private void onHeightEdited(@NonNull BigDecimal height) {
        mUserData.put(OHQUserDataKey.HeightKey, height);
        mHeightView.setSummary(getString(R.string.height_label, height.toString()));
    }

    private void showGenderSelectDialog() {
        new SimpleDialog.Builder(GuestHomeFragment.this)
                .title(R.string.gender)
                .items(
                        getString(R.string.male),
                        getString(R.string.female)
                )
                .negative(getString(R.string.cancel))
                .requestCode(DIALOG_GENDER_LIST)
                .show();
    }

    private void onGenderSelected(OHQGender gender) {
        mUserData.put(OHQUserDataKey.GenderKey, gender);
        mGenderView.setSummary(gender.name());
    }

    @Override
    public void onSimpleDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        switch (requestCode) {
            case DIALOG_GENDER_LIST:
                OHQGender gender;
                switch (resultCode) {
                    case 0: // Male
                        gender = OHQGender.Male;
                        break;
                    case 1: // Female
                        gender = OHQGender.Female;
                        break;
                    default:
                        gender = OHQGender.Male;
                        break;
                }
                onGenderSelected(gender);
                break;
            default:
                throw new AndroidRuntimeException("Illegal request code.");
        }
    }

    @Override
    public void onSimpleDialogCancelled(int requestCode, Bundle params) {

    }

    public enum Event {
        DeviceInfo, AddDevice, ChangeToNormalMode, ChangeToUnregisteredUserMode, ReceiveMeasurementRecords
    }

    public enum EventArg {
        DeviceAddress, DiscoveredDevice, UserData
    }

    public interface EventListener {
        void onFragmentEvent(@NonNull Event event, Bundle args);
    }
}
