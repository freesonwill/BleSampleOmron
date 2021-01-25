package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import jp.co.ohq.ble.advertising.EachUserData;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DiscoveredDevice;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.model.enumerate.ActivityRequestCode;
import jp.co.ohq.blesampleomron.model.enumerate.Protocol;
import jp.co.ohq.blesampleomron.view.fragment.RegistrationOptionFragment;
import jp.co.ohq.blesampleomron.view.fragment.controller.DiscoveredDeviceSelectionFragmentController;

public class RegisterDeviceActivity extends BaseActivity implements
        DiscoveredDeviceSelectionFragmentController.EventListener,
        RegistrationOptionFragment.EventListener {

    private boolean mCanceled;
    private DiscoveredDevice mDiscoverDevice;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, RegisterDeviceActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, DiscoveredDeviceSelectionFragmentController.newInstance(true, true))
                .commit();
    }

    @Override
    protected void onResume() {
        AppLog.vMethodIn("mCanceled:" + mCanceled);
        super.onResume();
        if (mCanceled) {
            getSupportFragmentManager().popBackStack();
            mCanceled = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        AppLog.vMethodIn();
        super.onSaveInstanceState(outState);
        outState.putBoolean("Canceled", mCanceled);
        outState.putParcelable(DiscoveredDevice.class.getSimpleName(), mDiscoverDevice);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onRestoreInstanceState(savedInstanceState);
        mCanceled = savedInstanceState.getBoolean("Canceled");
        mDiscoverDevice = savedInstanceState.getParcelable(DiscoveredDevice.class.getSimpleName());
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
        AppLog.vMethodIn(event.name());
        switch (event) {
            case Selected: {
                mDiscoverDevice = args.getParcelable(DiscoveredDeviceSelectionFragmentController.EventArg.DiscoveredDevice.name());
                if (null == mDiscoverDevice) {
                    throw new NullPointerException("null == mDiscoverDevice");
                }
                EachUserData eachUserData = mDiscoverDevice.getEachUserData();
                if (null != eachUserData) {
                    replaceFragmentWithAddingToBackStack(android.R.id.content,
                            RegistrationOptionFragment.newInstance(eachUserData.getNumberOfUser(), mDiscoverDevice));
                } else {
                    replaceFragmentWithAddingToBackStack(android.R.id.content,
                            RegistrationOptionFragment.newInstance(0, mDiscoverDevice));
                }
                break;
            }
        }
    }

    @Override
    public void onFragmentEvent(@NonNull RegistrationOptionFragment.Event event, Bundle args) {
        AppLog.vMethodIn(event.name());
        switch (event) {
            case StartRegistration: {
                if (null == mDiscoverDevice) {
                    throw new IllegalStateException("null == mDiscoverDevice");
                }
                final Protocol selectedProtocol = (Protocol) args.getSerializable(RegistrationOptionFragment.Arg.Protocol.name());
                if (null == selectedProtocol) {
                    throw new NullPointerException("null == mSelectedProtocol");
                }
                final Integer selectedUserIndex;
                if (args.containsKey(RegistrationOptionFragment.Arg.UserIndex.name())) {
                    selectedUserIndex = args.getInt(RegistrationOptionFragment.Arg.UserIndex.name());
                } else {
                    selectedUserIndex = null;
                }
                startActivityForResult(SessionActivity.newIntentForRegister(
                        this, mDiscoverDevice, selectedProtocol, selectedUserIndex), ActivityRequestCode.Session.hashCode16());
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.vMethodIn();

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
        }
    }

    private void onSessionActivityResult(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                HistoryData historyData = data.getParcelableExtra(SessionActivity.Result.ResultData.name());
                if (null == historyData) {
                    throw new NullPointerException("null == resultData");
                }
                startActivity(ResultActivity.newIntent(this, historyData));
                finish();
                break;
            case RESULT_CANCELED:
                mCanceled = true;
                break;
            default:
                finish();
                break;
        }
    }
}
