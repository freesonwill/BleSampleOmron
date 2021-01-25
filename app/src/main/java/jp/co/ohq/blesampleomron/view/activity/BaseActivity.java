package jp.co.ohq.blesampleomron.view.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.service.LogViewService;

abstract class BaseActivity extends AppCompatActivity {

    private static final String[] sRequiredPermissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @TargetApi(Build.VERSION_CODES.M)
    private static void requestPermissions(@NonNull Activity activity, @NonNull String[] permissions) {
        activity.requestPermissions(permissions, 0);
    }

    protected void replaceFragment(@IdRes int containerViewId, @NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }

    protected void replaceFragmentWithAddingToBackStack(@IdRes int containerViewId, @NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(containerViewId, fragment)
                .commit();
    }

    protected void replaceFragmentWithAddingToBackStack(@IdRes int containerViewId, @NonNull Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(tag)
                .replace(containerViewId, fragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(getApplicationContext(), LogViewService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPermitted(this, sRequiredPermissions)) {
            requestPermissions(this, sRequiredPermissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (PackageManager.PERMISSION_DENIED == result) {
                finish();
                return;
            }
        }
    }

    private boolean isPermitted(@NonNull Context context, final @NonNull String[] permissions) {
        final boolean ret;
        if (Build.VERSION_CODES.M > Build.VERSION.SDK_INT) {
            AppLog.i("Unsupported runtime permissions.");
            ret = true;
        } else {
            List<String> deniedPermission = new ArrayList<>();
            for (String permission : permissions) {
                if (PackageManager.PERMISSION_DENIED == context.checkSelfPermission(permission)) {
                    deniedPermission.add(permission);
                }
            }
            if (deniedPermission.isEmpty()) {
                AppLog.i("Runtime permissions are permitted.");
                ret = true;
            } else {
                ret = false;
            }
        }
        return ret;
    }

    @Override
    public void onBackPressed() {
        for (Fragment parent : getSupportFragmentManager().getFragments()) {
            if (null != parent) {
                if (parent.isVisible()) {
                    if (0 < parent.getChildFragmentManager().getBackStackEntryCount()) {
                        parent.getChildFragmentManager().popBackStack();
                        return;
                    }
                }
            }
        }
        super.onBackPressed();
    }
}
