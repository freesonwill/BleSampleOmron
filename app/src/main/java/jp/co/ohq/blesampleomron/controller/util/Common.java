package jp.co.ohq.blesampleomron.controller.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.utility.StringEx;

@SuppressWarnings("unused")
public class Common {

    private final static int MAX_FRACTION_DIGITS = 3;

    private Common() {
        // must be not create instance.
    }

    @ColorRes
    public static int getDeviceCategoryColorResource(@Nullable OHQDeviceCategory deviceCategory) {
        @ColorRes
        int colorRes = android.R.color.white;
        if (null != deviceCategory) {
            switch (deviceCategory) {
                case BloodPressureMonitor:
                    colorRes = R.color.bloodPressureMonitor;
                    break;
                case BodyCompositionMonitor:
                    colorRes = R.color.bodyCompositionMonitor;
                    break;
                case WeightScale:
                    colorRes = R.color.weightScale;
                    break;
            }
        }
        return colorRes;
    }

    @NonNull
    public static String getNumberString(BigDecimal value) {
        return StringEx.toNumberString(value);
    }

    @NonNull
    public static String getNumberStringWithUnit(BigDecimal value, @NonNull String unit) {
        return getNumberString(value) + " " + unit;
    }

    @NonNull
    public static String getDecimalString(BigDecimal value, int minDigits) {
        return StringEx.toDecimalString(value, minDigits, MAX_FRACTION_DIGITS);
    }

    @NonNull
    public static String getDecimalStringWithUnit(BigDecimal value, int minDigits, @NonNull String unit) {
        return getDecimalString(value, minDigits) + " " + unit;
    }

    public static String getPercentString(BigDecimal value, int minDigits) {
        return getDecimalString(value.multiply(new BigDecimal("100.0")), minDigits);
    }

    public static String getPercentStringWithUnit(BigDecimal value, int minDigits) {
        return StringEx.toPercentString(value, minDigits, MAX_FRACTION_DIGITS);
    }

    public static void outputDeviceInfo(Context context) {
        if (null == context) {
            throw new IllegalArgumentException("null == context");
        }
        PackageManager pm = context.getPackageManager();
        if (null == pm) {
            throw new AndroidRuntimeException("null == pm");
        }
        String packageName = "Unknown";
        String versionName = "?.?.?";

        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            packageName = packageInfo.packageName;
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AppLog.i("Product: " + packageName + " v" + versionName);
        AppLog.i("DeviceType: Android " + Build.VERSION.RELEASE);
        AppLog.i("DeviceName: " + Build.MODEL);

        AppLog.i("Board: " + Build.BOARD);
        AppLog.i("Bootloader: " + Build.BOOTLOADER);
        AppLog.i("Brand: " + Build.BRAND);
        AppLog.i("Device: " + Build.DEVICE);
        AppLog.i("Display: " + Build.DISPLAY);
        AppLog.i("Fingerprint: " + Build.FINGERPRINT);
        AppLog.i("Hardware: " + Build.HARDWARE);
        AppLog.i("Host: " + Build.HOST);
        AppLog.i("ID: " + Build.ID);
        AppLog.i("KernelVersion: " + readKernelVersion());
        AppLog.i("Manufacturer: " + Build.MANUFACTURER);
        AppLog.i("Product: " + Build.PRODUCT);
        AppLog.i("Radio: " + Build.getRadioVersion());
        // LogUtil.i("Serial: " + Build.SERIAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StringBuilder supported_abis = new StringBuilder();
            for (String s : Build.SUPPORTED_ABIS) {
                supported_abis.append(" ").append(s);
            }
            AppLog.i("Supported ABIs:" + supported_abis);
        }
        AppLog.i("Tags: " + Build.TAGS);
        AppLog.i("Time: " + Build.TIME);
        AppLog.i("Type: " + Build.TYPE);
        AppLog.i("User: " + Build.USER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppLog.i("VersionBaseOS: " + Build.VERSION.BASE_OS);
        }
        AppLog.i("VersionCodeName: " + Build.VERSION.CODENAME);
        AppLog.i("VersionIncremental: " + Build.VERSION.INCREMENTAL);
        AppLog.i("VersionSDK: " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppLog.i("VersionSecurityPatch: " + Build.VERSION.SECURITY_PATCH);
        }
    }

    private static String readKernelVersion() {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "cat", "/proc/version"
            });

            if (process.waitFor() == 0) {
                inputStream = process.getInputStream();
            } else {
                inputStream = process.getErrorStream();
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1024);
            return bufferedReader.readLine();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
