package jp.co.ohq.blesampleomron.view.fragment.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;
import android.view.View;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.view.fragment.AdvertisementDataFragment;
import jp.co.ohq.blesampleomron.view.fragment.DiscoveredDevicesFragment;
import jp.co.ohq.utility.Bundler;

public class DiscoveredDeviceSelectionFragmentController extends BaseFragmentController implements
        DiscoveredDevicesFragment.EventListener {

    private static final String ARG_ONLY_VIEW = "ARG_ONLY_VIEW";
    private static final String ARG_ONLY_PAIRING_MODE = "ARG_ONLY_PAIRING_MODE";
    private static final String ARG_COMPARE_REGISTERED_DEVICES = "ARG_COMPARE_REGISTERED_DEVICES";
    private EventListener mListener;

    public static DiscoveredDeviceSelectionFragmentController newInstance(boolean onlyPairingMode, boolean compareRegisteredDevices) {
        DiscoveredDeviceSelectionFragmentController fragment = new DiscoveredDeviceSelectionFragmentController();
        fragment.setArguments(Bundler.bundle(
                ARG_ONLY_VIEW, false,
                ARG_ONLY_PAIRING_MODE, onlyPairingMode,
                ARG_COMPARE_REGISTERED_DEVICES, compareRegisteredDevices
        ));
        return fragment;
    }

    public static DiscoveredDeviceSelectionFragmentController newInstance(boolean onlyView, boolean onlyPairingMode, boolean compareRegisteredDevices) {
        DiscoveredDeviceSelectionFragmentController fragment = new DiscoveredDeviceSelectionFragmentController();
        fragment.setArguments(Bundler.bundle(
                ARG_ONLY_VIEW, onlyView,
                ARG_ONLY_PAIRING_MODE, onlyPairingMode,
                ARG_COMPARE_REGISTERED_DEVICES, compareRegisteredDevices
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.container, DiscoveredDevicesFragment.newInstance(
                        !getArguments().getBoolean(ARG_ONLY_VIEW),
                        getArguments().getBoolean(ARG_ONLY_PAIRING_MODE),
                        getArguments().getBoolean(ARG_COMPARE_REGISTERED_DEVICES)
                ))
                .commit();
    }

    @Override
    public void onFragmentEvent(@NonNull DiscoveredDevicesFragment.Event event, Bundle args) {
        switch (event) {
            case DetailInfoSelected: {
                DiscoveredDevice discoverDevice = args.getParcelable(DiscoveredDevicesFragment.EventArg.DiscoveredDevice.name());
                if (null == discoverDevice) {
                    throw new NullPointerException("null == mDiscoverDevice");
                }
                if (null == discoverDevice.getAdvertisementData()) {
                    throw new NullPointerException("null == discoverDevice.getAdvertisementData()");
                }
                getChildFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, AdvertisementDataFragment.newInstance(discoverDevice.getAdvertisementData()))
                        .commit();
                break;
            }
            case DeviceSelected: {
                DiscoveredDevice discoverDevice = args.getParcelable(DiscoveredDevicesFragment.EventArg.DiscoveredDevice.name());
                if (null == discoverDevice) {
                    throw new NullPointerException("null == mDiscoverDevice");
                }
                if (null == discoverDevice.getAdvertisementData()) {
                    throw new NullPointerException("null == discoverDevice.getAdvertisementData()");
                }
                if (getArguments().getBoolean(ARG_ONLY_VIEW)) {
                    getChildFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.container, AdvertisementDataFragment.newInstance(discoverDevice.getAdvertisementData()))
                            .commit();
                } else {
                    mListener.onFragmentEvent(Event.Selected, Bundler.bundle(
                            EventArg.DiscoveredDevice.name(), discoverDevice
                    ));
                }
                break;
            }
            default:
                throw new AndroidRuntimeException("Illegal event.");
        }
    }

    public enum Event {
        Selected
    }

    public enum EventArg {
        DiscoveredDevice,
    }

    public interface EventListener {
        void onFragmentEvent(Event event, Bundle args);
    }
}
