package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.fragment.UserSelectionFragment;

public class UserSelectionActivity extends BaseActivity
        implements UserSelectionFragment.EventListener {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, UserSelectionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        replaceFragment(
                android.R.id.content,
                UserSelectionFragment.newInstance());
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
        if (0 == getSupportFragmentManager().getBackStackEntryCount()) {
            activityFinish(RESULT_CANCELED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUserSelectionFragmentEvent(@NonNull UserSelectionFragment.Event event, @NonNull Bundle args) {
        AppLog.vMethodIn(event.name());
        switch (event) {
            case UserSelected: {
                String userName = args.getString(UserSelectionFragment.Arg.UserName.name());
                if (null == userName) {
                    throw new IllegalArgumentException("null == userName");
                }
                AppConfig.sharedInstance().setNameOfCurrentUser(userName);
                activityFinish(RESULT_OK);
                break;
            }
        }
    }

    private void activityFinish(int responseCode) {
        Intent intent = new Intent();
        setResult(responseCode, intent);
        finish();
    }
}
