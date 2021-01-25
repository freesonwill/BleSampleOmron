package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import io.realm.Realm;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.DeviceInfo;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.model.entity.UserInfo;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.fragment.DeviceDetailFragment;
import jp.co.ohq.blesampleomron.view.fragment.SessionFragment;

public class DeviceDetailActivity extends BaseActivity implements
        DeviceDetailFragment.EventListener {

    private static final String ARG_OPENING_FRAGMENT = "ARG_OPENING_FRAGMENT";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";

    private static final int ACTIVITY_REQ_CODE_SESSION = 0;
    private Realm mRealm;
    private String mAddress;

    public static Intent newIntent(@NonNull Context context, @NonNull String address) {
        Intent intent = new Intent(context, DeviceDetailActivity.class);
        intent.putExtra(ARG_OPENING_FRAGMENT, OpeningFragmentType.DeviceDetail);
        intent.putExtra(ARG_ADDRESS, address);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();

        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        OpeningFragmentType openingFragmentType = (OpeningFragmentType) getIntent().getSerializableExtra(ARG_OPENING_FRAGMENT);
        mAddress = getIntent().getStringExtra(ARG_ADDRESS);

        Fragment fragment;
        switch (openingFragmentType) {
            case DeviceDetail:
                fragment = DeviceDetailFragment.newInstance(mAddress);
                break;
            default:
                throw new IllegalArgumentException("Invalid openingFragmentType.");
        }
        replaceFragment(android.R.id.content, fragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.vMethodIn();
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onFragmentEvent(DeviceDetailFragment.Event event, Bundle args) {
        AppLog.vMethodIn(event.name());
        switch (event) {
            case DeleteUser: {
                deleteUser(mAddress);
                break;
            }
            case ForgetDevice: {
                forgetDevice(mAddress);
                break;
            }
        }
    }

    private void deleteUser(@NonNull String address) {
        DeviceInfo deviceInfo = mRealm.where(DeviceInfo.class).equalTo("users.name",
                AppConfig.sharedInstance().getNameOfCurrentUser()).equalTo("address", address).findFirst();
        if (null != deviceInfo.getUserIndex()) {
            startActivityForResult(SessionActivity.newIntentForDelete(this, address), ACTIVITY_REQ_CODE_SESSION);
        } else {
            forgetDevice(address);
        }
    }

    private void forgetDevice(@NonNull String address) {
        UserInfo userInfo = mRealm.where(UserInfo.class).equalTo("name",
                AppConfig.sharedInstance().getNameOfCurrentUser()).findAll().first();
        DeviceInfo deviceInfo = mRealm.where(DeviceInfo.class).equalTo("users.name",
                AppConfig.sharedInstance().getNameOfCurrentUser()).equalTo("address", address).findFirst();
        mRealm.beginTransaction();
        userInfo.getRegisteredDevices().remove(deviceInfo);
        mRealm.commitTransaction();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.vMethodIn();

        if (ACTIVITY_REQ_CODE_SESSION != requestCode) {
            throw new IllegalArgumentException("ACTIVITY_REQ_CODE_SESSION != requestCode");
        }

        switch (resultCode) {
            case RESULT_OK:
                HistoryData historyData = data.getParcelableExtra(SessionFragment.EventArg.ResultData.name());
                if (null == historyData) {
                    throw new NullPointerException("null == resultData");
                }

                startActivity(ResultActivity.newIntent(this, historyData));
                break;
            case RESULT_CANCELED:
            default:
                break;
        }
        finish();
    }

    private enum OpeningFragmentType {
        DeviceDetail,
    }
}
