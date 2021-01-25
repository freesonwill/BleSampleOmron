package jp.co.ohq.blesampleomron.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import jp.co.ohq.blesampleomron.BuildConfig;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.enumerate.SettingKey;

public class SettingsFragment extends BasePreferenceFragment {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        AppLog.vMethodIn();
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);

        Preference version = findPreference(SettingKey.version.name());
        String versionName = BuildConfig.VERSION_NAME;
        if (!TextUtils.isEmpty(versionName)) {
            version.setSummary(getString(R.string.version_label, versionName));
        } else {
            version.setSummary(getString(R.string.version_label, getString(R.string.unknown_version)));
        }
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.settings);
    }
}
