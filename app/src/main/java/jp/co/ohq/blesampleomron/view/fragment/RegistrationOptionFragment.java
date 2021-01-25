package jp.co.ohq.blesampleomron.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Locale;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;
import jp.co.ohq.utility.Bundler;

public class RegistrationOptionFragment extends BaseFragment {

    private static final String ARG_NUMBER_OF_USER = "ARG_NUMBER_OF_USER";
    private static final String ARG_DEVICE_ADDRESS = "ARG_DEVICE_ADDRESS";
    private static final String ARG_DEVICE_LOCAL_NAME = "ARG_DEVICE_LOCAL_NAME";
    private EventListener mListener;

    public static RegistrationOptionFragment newInstance(int numberOfUser, DiscoveredDevice device) {
        RegistrationOptionFragment fragment = new RegistrationOptionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NUMBER_OF_USER, numberOfUser);
        args.putString(ARG_DEVICE_ADDRESS, device.getAddress());
        args.putString(ARG_DEVICE_LOCAL_NAME, device.getLocalName());
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
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();

        final View rootView = inflater.inflate(R.layout.fragment_registration_option, container, false);

        Bundle args = getArguments();
        int numberOfUser = args.getInt(ARG_NUMBER_OF_USER);

        String address = args.getString(ARG_DEVICE_ADDRESS);
        TextView addressText = (TextView) rootView.findViewById(R.id.device_address);
        addressText.setText(address);

        String localName = args.getString(ARG_DEVICE_LOCAL_NAME);
        TextView localNameText = (TextView) rootView.findViewById(R.id.device_local_name);
        localNameText.setText(localName);

        final RadioGroup userIndexGroup;
        final RadioGroup protocolGroup = (RadioGroup) rootView.findViewById(R.id.protocol_group);
        final RadioButton omronExtensionBtn = (RadioButton) rootView.findViewById(R.id.omron_extension);

        if (1 <= numberOfUser) {

            userIndexGroup = (RadioGroup) rootView.findViewById(R.id.user_index_group);
            for (int i = 0; numberOfUser >= i; i++) {
                RadioButton rb = new RadioButton(getContext());
                rb.setId(i);
                if (0 == i) {
                    rb.setText(R.string.user_index_auto);
                } else {
                    rb.setText(String.format(Locale.US, "%d", i));
                }
                userIndexGroup.addView(rb);
                if (1 == i) {
                    userIndexGroup.check(rb.getId());
                }
            }
            protocolGroup.check(R.id.omron_extension);
            rootView.findViewById(R.id.user_index_group_title).setVisibility(View.VISIBLE);
            userIndexGroup.setVisibility(View.VISIBLE);

            protocolGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    AppLog.vMethodIn();
                    if (R.id.bluetooth_sig == checkedId) {
                        AppLog.i("bluetooth_sig");
                        rootView.findViewById(R.id.user_index_group_title).setVisibility(View.GONE);
                        userIndexGroup.setVisibility(View.GONE);
                    } else if (R.id.omron_extension == checkedId) {
                        AppLog.i("omron_extension");
                        rootView.findViewById(R.id.user_index_group_title).setVisibility(View.VISIBLE);
                        userIndexGroup.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            omronExtensionBtn.setVisibility(View.GONE);
            protocolGroup.check(R.id.bluetooth_sig);
            userIndexGroup = null;
        }

        rootView.findViewById(R.id.startRegistrationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Protocol protocol = protocolGroup.getCheckedRadioButtonId() == R.id.bluetooth_sig ? Protocol.BluetoothStandard : Protocol.OmronExtension;
                Bundle args = Bundler.bundle(Arg.Protocol.name(), protocol);
                if (Protocol.OmronExtension == protocol && userIndexGroup != null && 0 < userIndexGroup.getCheckedRadioButtonId()) {
                    args.putInt(Arg.UserIndex.name(), userIndexGroup.getCheckedRadioButtonId());
                }
                mListener.onFragmentEvent(Event.StartRegistration, args);
            }
        });

        return rootView;
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.registration_options).toUpperCase();
    }

    public enum Event {
        StartRegistration,
    }

    public enum Arg {
        Protocol, UserIndex
    }

    public interface EventListener {
        void onFragmentEvent(@NonNull Event event, Bundle args);
    }
}
