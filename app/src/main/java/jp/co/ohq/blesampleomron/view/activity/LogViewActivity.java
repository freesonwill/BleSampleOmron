package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.view.fragment.LogViewFragment;

public class LogViewActivity extends BaseActivity {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, LogViewActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        replaceFragment(android.R.id.content, LogViewFragment.newInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.vMethodIn();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
