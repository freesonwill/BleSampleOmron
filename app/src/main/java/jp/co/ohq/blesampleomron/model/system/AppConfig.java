package jp.co.ohq.blesampleomron.model.system;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public final class AppConfig {

    public static final String GUEST = "Guest";

    @Nullable
    private static AppConfig sInstance;

    @NonNull
    public final String mPackageName;

    @NonNull
    private String mCurrentUserName;

    private AppConfig(@NonNull Context context) {
        mPackageName = context.getPackageName();
        mCurrentUserName = "";
    }

    public static AppConfig init(@NonNull Context context) {
        if (null != sInstance) {
            throw new IllegalStateException("An instance has already been created.");
        }
        return sInstance = new AppConfig(context);
    }

    public static AppConfig sharedInstance() {
        if (null == sInstance) {
            throw new IllegalStateException("Instance has not been created.");
        }
        return sInstance;
    }

    @NonNull
    public String getNameOfCurrentUser() {
        return mCurrentUserName;
    }

    public void setNameOfCurrentUser(@NonNull String userName) {
        mCurrentUserName = userName;
    }

    @NonNull
    public String getApplicationVersionName(@NonNull Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);

            if (packageInfo != null) {
                return packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    @NonNull
    public String getExternalApplicationDirectoryPath(@NonNull Context context) {
        File dir = new File(getDocumentsDirectoryPath() + "/" + mPackageName);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalAccessError("Failed to make directory '" + dir + "'");
            }
        }
        return dir.getAbsolutePath();
    }

    private String getDocumentsDirectoryPath() {
        String ret;
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            ret = Environment.getExternalStorageDirectory().getPath() + "/Documents";
        } else {
            ret = getExternalStorageDocumentsDirectory();
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getExternalStorageDocumentsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
    }
}
