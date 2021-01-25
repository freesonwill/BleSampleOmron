package jp.co.ohq.blesampleomron.view.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import jp.co.ohq.blesampleomron.R;

abstract class BaseFragment extends Fragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(onGetTitle().toUpperCase());
    }

    @NonNull
    protected String onGetTitle() {
        return getString(R.string.app_name);
    }

    protected void replaceFragment(@IdRes int containerViewId, @NonNull Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }

    protected void replaceFragmentWithAddingToBackStack(@IdRes int containerViewId, @NonNull Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(containerViewId, fragment)
                .commit();
    }
}
