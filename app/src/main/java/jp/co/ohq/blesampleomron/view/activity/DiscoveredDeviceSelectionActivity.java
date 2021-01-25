package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.util.AndroidRuntimeException;
import android.view.MenuItem;

import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.view.fragment.DiscoveredDevicesFragment;
import jp.co.ohq.blesampleomron.view.fragment.controller.DiscoveredDeviceSelectionFragmentController;

public class DiscoveredDeviceSelectionActivity extends BaseActivity implements
        DiscoveredDeviceSelectionFragmentController.EventListener {

    private static final String ARG_ONLY_VIEW = "ARG_ONLY_VIEW";
    private static final String ARG_ONLY_PAIRING_MODE = "ARG_ONLY_PAIRING_MODE";
    private static final String ARG_COMPARE_REGISTERED_DEVICES = "ARG_COMPARE_REGISTERED_DEVICES";

    public static Intent newIntent(@NonNull Context context, boolean onlyView, boolean onlyPairingMode, boolean compareRegisteredDevices) {
        Intent intent = new Intent(context, DiscoveredDeviceSelectionActivity.class);
        intent.putExtra(ARG_ONLY_VIEW, onlyView);
        intent.putExtra(ARG_ONLY_PAIRING_MODE, onlyPairingMode);
        intent.putExtra(ARG_COMPARE_REGISTERED_DEVICES, compareRegisteredDevices);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent args = getIntent();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, DiscoveredDeviceSelectionFragmentController.newInstance(
                        args.getBooleanExtra(ARG_ONLY_VIEW, false),
                        args.getBooleanExtra(ARG_ONLY_PAIRING_MODE, false),
                        args.getBooleanExtra(ARG_COMPARE_REGISTERED_DEVICES, false)  // ToDo
                ))
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentEvent(@NonNull DiscoveredDeviceSelectionFragmentController.Event event, Bundle args) {
        switch (event) {
            case Selected: {
                DiscoveredDevice discoveredDevice = args.getParcelable(DiscoveredDevicesFragment.EventArg.DiscoveredDevice.name());
                if (null == discoveredDevice) {
                    throw new NullPointerException("null == discoveredDevice");
                }
                Intent intent = new Intent();
                intent.putExtra(Result.SelectedDevice.name(), discoveredDevice);
                setResult(RESULT_OK, intent);
                finish();
                break;
            }
            default:
                throw new AndroidRuntimeException("Illegal event.");
        }
    }

    public enum Result {
        SelectedDevice,
    }
}
