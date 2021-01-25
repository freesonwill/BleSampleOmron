package jp.co.ohq.blesampleomron.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;

import jp.co.ohq.blesampleomron.R;

abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(onGetTitle().toUpperCase());
    }

    @NonNull
    protected String onGetTitle() {
        return getString(R.string.app_name);
    }
}
