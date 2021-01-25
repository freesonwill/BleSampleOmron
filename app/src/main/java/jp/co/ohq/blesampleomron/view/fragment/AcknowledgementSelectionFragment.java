package jp.co.ohq.blesampleomron.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import jp.co.ohq.blesampleomron.R;

public class AcknowledgementSelectionFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setHasOptionsMenu(true);
        setPreferencesFromResource(R.xml.acknowledgements_settings, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.acknowledgements).toUpperCase();
    }
}

