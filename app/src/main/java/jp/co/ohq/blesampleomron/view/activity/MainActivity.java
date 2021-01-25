package jp.co.ohq.blesampleomron.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.Realm;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.model.entity.UserInfo;
import jp.co.ohq.blesampleomron.model.enumerate.ActivityRequestCode;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.fragment.HistoryFragment;
import jp.co.ohq.blesampleomron.view.fragment.UserProfileFragment;
import jp.co.ohq.blesampleomron.view.fragment.controller.HomeFragmentController;

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        HistoryFragment.EventListener,
        UserProfileFragment.EventListener {

    private Realm mRealm;

    private ImageView mUserImageView;
    private TextView mUserNameView;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0).findViewById(R.id.nav_header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLog.vMethodIn();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                startActivityForResult(UserSelectionActivity.newIntent(MainActivity.this), ActivityRequestCode.UserSelection.hashCode16());
            }
        });

        mUserImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.user_image);
        mUserNameView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);

        Menu menuNav = navigationView.getMenu();
        MenuItem itemUserProfile = menuNav.findItem(R.id.nav_user_profile);

        if (AppConfig.sharedInstance().getNameOfCurrentUser().equals(AppConfig.GUEST)) {
            itemUserProfile.setEnabled(false);
        } else {
            itemUserProfile.setEnabled(true);
        }
        replaceFragment(R.id.container, HomeFragmentController.newInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppConfig.sharedInstance().getNameOfCurrentUser().equals(AppConfig.GUEST)) {
            mUserImageView.setImageResource(R.drawable.img_anonymous_black_100dp);
            mUserNameView.setText(AppConfig.GUEST);
        } else {
            UserInfo userInfo = mRealm.where(UserInfo.class).equalTo(
                    "name", AppConfig.sharedInstance().getNameOfCurrentUser()).findFirst();
            mUserImageView.setImageResource(OHQGender.Male == userInfo.getGender() ? R.drawable.img_male_black_100dp : R.drawable.img_female_black_100dp);
            mUserNameView.setText(userInfo.getName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            replaceFragment(
                    R.id.container,
                    HomeFragmentController.newInstance());
        } else if (id == R.id.nav_user_profile) {
            replaceFragment(
                    R.id.container,
                    UserProfileFragment.newInstance(AppConfig.sharedInstance().getNameOfCurrentUser()));
        } else if (id == R.id.nav_history) {
            replaceFragment(
                    R.id.container,
                    HistoryFragment.newInstance());
        } else if (id == R.id.nav_settings) {
            startActivity(SettingsActivity.newIntent(this));
        } else if (id == R.id.nav_log) {
            startActivity(LogViewActivity.newIntent(this));
        } else if (id == R.id.nav_scanner) {
            startActivityForResult(DiscoveredDeviceSelectionActivity.newIntent(
                    this,
                    true,
                    false,
                    false
            ), ActivityRequestCode.DiscoveredDeviceSelection.hashCode16());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentEvent(@NonNull UserProfileFragment.Event event, Bundle args) {
        UserInfo userInfo = mRealm.where(UserInfo.class).equalTo(
                "name", AppConfig.sharedInstance().getNameOfCurrentUser()).findFirst();
        mUserImageView.setImageResource(OHQGender.Male == userInfo.getGender() ? R.drawable.img_male_black_100dp : R.drawable.img_female_black_100dp);
    }

    @Override
    public void onFragmentEvent(@NonNull HistoryFragment.Event event, Bundle args) {
        switch (event) {
            case Selected:
                HistoryData historyData = args.getParcelable(HistoryFragment.EventArg.ResultData.name());
                if (null == historyData) {
                    throw new IllegalArgumentException("null == resultData");
                }
                startActivity(ResultActivity.newIntent(this, historyData));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.vMethodIn();
        super.onActivityResult(requestCode, resultCode, data);
        final ActivityRequestCode requestCodeEnum;
        try {
            requestCodeEnum = ActivityRequestCode.valueOf(requestCode);
        } catch (IllegalArgumentException e) {
            return;
        }
        switch (requestCodeEnum) {
            case UserSelection:
                onUserSelectionActivityResult(resultCode, data);
                break;
        }
    }

    private void onUserSelectionActivityResult(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                recreate();
                break;
            case RESULT_CANCELED:
            default:
                break;
        }
    }
}
