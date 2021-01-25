package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.view.fragment.ResultFragment;

public class ResultActivity extends BaseActivity {

    private static final String EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA";

    public static Intent newIntent(@NonNull Context context, @NonNull HistoryData historyData) {
        AppLog.vMethodIn();
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_RESULT_DATA, historyData);
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

        HistoryData historyData = getIntent().getParcelableExtra(EXTRA_RESULT_DATA);

        replaceFragment(
                android.R.id.content,
                ResultFragment.newInstance(historyData));
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
}
