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
import java.util.Locale;

import io.realm.Realm;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.DateUtil;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DeviceInfo;
import jp.co.ohq.blesampleomron.model.entity.UserInfo;
import jp.co.ohq.blesampleomron.view.dialog.EditTextDialog;
import jp.co.ohq.blesampleomron.view.dialog.SimpleDialog;
import jp.co.ohq.blesampleomron.view.widget.TwoLineTextViewWithoutImage;
import jp.co.ohq.utility.Bundler;

public class UserProfileFragment extends BaseFragment implements
        EditTextDialog.Callback,
        SimpleDialog.Callback,
        DatePickerDialog.OnDateSetListener {

    private static final String ARG_USER_NAME = "ARG_USER_NAME";

    private static final int DIALOG_GENDER_LIST = 1;

    private Realm mRealm;

    private UserInfo mUserInfo;

    private EventListener mListener;
    private TwoLineTextViewWithoutImage mDateOfBirthValue;
    private TwoLineTextViewWithoutImage mHeightValue;
    private TwoLineTextViewWithoutImage mGenderValue;

    public static UserProfileFragment newInstance(String userName) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AppLog.vMethodIn();
        super.onAttach(context);
        if ((context instanceof EventListener)) {
            mListener = (EventListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();

        Bundle args = getArguments();
        String userName = args.getString(ARG_USER_NAME);
        mUserInfo = mRealm.where(UserInfo.class).equalTo("name", userName).findFirst();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        TwoLineTextViewWithoutImage dateOfBirthText = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.date_of_birth_text);
        dateOfBirthText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        mDateOfBirthValue = dateOfBirthText;
        mDateOfBirthValue.setSummary(mUserInfo.getDateOfBirth());

        TwoLineTextViewWithoutImage heightText = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.height_text);
        heightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightEditDialog();
            }
        });
        mHeightValue = heightText;
        mHeightValue.setSummary(getString(R.string.height_label, String.valueOf(mUserInfo.getHeight())));

        TwoLineTextViewWithoutImage genderText = (TwoLineTextViewWithoutImage) rootView.findViewById(R.id.gender_text);
        genderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderSelectDialog();
            }
        });
        mGenderValue = genderText;
        mGenderValue.setSummary(mUserInfo.getGender().name());

        return rootView;
    }

    @Override
    public void onEditTextDialogEdited(String resultCode) {
        AppLog.d(resultCode);
        onHeightEdited(new BigDecimal(resultCode));
    }

    @Override
    public void onEditTextDialogCanceled() {

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
                        gender = mUserInfo.getGender();
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
        AppLog.vMethodIn();
    }

    private void showDatePickerDialog() {
        Calendar cal = DateUtil.toDate(mUserInfo.getDateOfBirth(), DateUtil.FormatType.Form2);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
        dpd.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String ymd = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
        if (mUserInfo.getDateOfBirth().equals(ymd)) {
            return;
        }
        mRealm.beginTransaction();
        mUserInfo.setDateOfBirth(ymd);
        updateDatabaseChangeIncrementIfNeeded(mUserInfo);
        mRealm.commitTransaction();
        mDateOfBirthValue.setSummary(mUserInfo.getDateOfBirth());
    }

    private void showHeightEditDialog() {
        EditTextDialog.newInstance(getString(R.string.height), String.valueOf(mUserInfo.getHeight())).show(getChildFragmentManager(), "");
    }

    private void onHeightEdited(@NonNull BigDecimal height) {
        if (height.equals(mUserInfo.getHeight())) {
            return;
        }
        mRealm.beginTransaction();
        mUserInfo.setHeight(height);
        updateDatabaseChangeIncrementIfNeeded(mUserInfo);
        mRealm.commitTransaction();
        mHeightValue.setSummary(getString(R.string.height_label, String.valueOf(mUserInfo.getHeight())));
    }

    private void showGenderSelectDialog() {
        new SimpleDialog.Builder(UserProfileFragment.this)
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
        if (mUserInfo.getGender() == gender) {
            return;
        }
        mRealm.beginTransaction();
        mUserInfo.setGender(gender);
        updateDatabaseChangeIncrementIfNeeded(mUserInfo);
        mRealm.commitTransaction();
        mGenderValue.setSummary(mUserInfo.getGender().name());
        if (null != mListener) {
            mListener.onFragmentEvent(Event.ChangedGender, Bundler.bundle(
                    EventArg.Gender.name(), gender));
        }
    }

    private void updateDatabaseChangeIncrementIfNeeded(@NonNull UserInfo userInfo) {
        for (DeviceInfo deviceInfo : userInfo.getRegisteredDevices()) {
            if (null == deviceInfo.getDatabaseChangeIncrement()) {
                continue;
            }
            if (deviceInfo.isUserDataUpdateFlag()) {
                continue;
            }
            deviceInfo.setUserDataUpdateFlag(true);
            deviceInfo.setDatabaseChangeIncrement(deviceInfo.getDatabaseChangeIncrement() + 1);
        }
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.profile).toUpperCase();
    }

    public enum Event {
        ChangedGender,
    }

    public enum EventArg {
        Gender,
    }

    public interface EventListener {
        void onFragmentEvent(@NonNull Event event, Bundle args);
    }
}
