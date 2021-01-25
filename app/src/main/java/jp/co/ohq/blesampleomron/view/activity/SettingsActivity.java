package jp.co.ohq.blesampleomron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import jp.co.ohq.blesampleomron.view.fragment.SettingsFragment;


public class SettingsActivity extends BaseActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        replaceFragment(android.R.id.content, SettingsFragment.newInstance());
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
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {

        final Fragment fragment = Fragment.instantiate(getBaseContext(), pref.getFragment(), pref.getExtras());
        String key = pref.getKey();
        if ("Gson".equals(key) || "nv_bluetooth".equals(key)) {
            Bundle bundle = new Bundle();
            bundle.putString("key", key);
            fragment.setArguments(bundle);
        }
        replaceFragmentWithAddingToBackStack(android.R.id.content, fragment, pref.getFragment().getClass().getName());
        return true;
    }
}
