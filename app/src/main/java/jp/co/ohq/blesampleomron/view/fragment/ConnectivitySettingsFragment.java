package jp.co.ohq.blesampleomron.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.enumerate.SettingKey;


public class ConnectivitySettingsFragment extends BasePreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        AppLog.vMethodIn();
        setHasOptionsMenu(true);
        setPreferencesFromResource(R.xml.connectivity_settings, rootKey);
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.connectivity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        AppLog.vMethodIn();
        inflater.inflate(R.menu.fragment_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        AppLog.vMethodIn();
        super.onResume();
        CheckBoxPreference pinPref = (CheckBoxPreference) findPreference(SettingKey.auto_enter_the_pin_code.name());
        EditTextPreference pinEditPref = (EditTextPreference) findPreference(SettingKey.pin_code.name());
        pinEditPref.setEnabled(pinPref.isChecked());
        CheckBoxPreference stablePref = (CheckBoxPreference) findPreference(SettingKey.stable_connection.name());
        EditTextPreference stableEditPref = (EditTextPreference) findPreference(SettingKey.stable_connection_wait_time.name());
        stableEditPref.setEnabled(stablePref.isChecked());
        CheckBoxPreference retryPref = (CheckBoxPreference) findPreference(SettingKey.connection_retry.name());
        EditTextPreference retryTimeEditPref = (EditTextPreference) findPreference(SettingKey.connection_retry_delay_time.name());
        EditTextPreference retryCntEditPref = (EditTextPreference) findPreference(SettingKey.connection_retry_count.name());
        retryTimeEditPref.setEnabled(retryPref.isChecked());
        retryCntEditPref.setEnabled(retryPref.isChecked());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        AppLog.vMethodIn();
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.vMethodIn();
        switch (item.getItemId()) {
            case R.id.initialize_settings:
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().clear().apply();
                PreferenceManager.setDefaultValues(getContext(), R.xml.connectivity_settings, true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        AppLog.vMethodIn(key);

        Preference preference = findPreference(key);
        if (preference instanceof CheckBoxPreference) {
            ((CheckBoxPreference) preference).setChecked(pref.getBoolean(key, false));
        } else if (preference instanceof EditTextPreference) {
//            preference.setSummary(pref.getString(key, "Unknown"));
            ((EditTextPreference) preference).setText(pref.getString(key, "Unknown"));
        } else if (preference instanceof ListPreference) {
            preference.setSummary(pref.getString(key, "Unknown"));
            AppLog.i(pref.getString(key, "Unknown"));
            ((ListPreference) preference).setValue(pref.getString(key, "Unknown"));
        }

        if (SettingKey.auto_enter_the_pin_code.name().equals(key)) {
            findPreference(SettingKey.pin_code.name()).setEnabled(pref.getBoolean(key, false));
        } else if (SettingKey.stable_connection.name().equals(key)) {
            findPreference(SettingKey.stable_connection_wait_time.name()).setEnabled(pref.getBoolean(key, false));
        } else if (SettingKey.connection_retry.name().equals(key)) {
            findPreference(SettingKey.connection_retry_delay_time.name()).setEnabled(pref.getBoolean(key, false));
            findPreference(SettingKey.connection_retry_count.name()).setEnabled(pref.getBoolean(key, false));
        }
    }
}
