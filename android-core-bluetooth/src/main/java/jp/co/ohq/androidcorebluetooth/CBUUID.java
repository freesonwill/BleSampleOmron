package jp.co.ohq.androidcorebluetooth;

import android.support.annotation.NonNull;
import android.util.AndroidRuntimeException;

import java.util.UUID;
import java.util.regex.Pattern;

public class CBUUID {

    private static final Pattern UUID_PATTERN_16 = Pattern.compile("([0-9a-fA-F]{4})");
    private static final Pattern UUID_PATTERN_32 = Pattern.compile("([0-9a-fA-F]{8})");
    private static final Pattern UUID_PATTERN_128 = Pattern.compile("([0-9a-fA-F]{8})[-]?([0-9a-fA-F]{4})[-]?([0-9a-fA-F]{4})[-]?([0-9a-fA-F]{4})[-]?([0-9a-fA-F]{12})");

    private static final String BT_UUID_PREFIX = "0000";
    private static final String BT_UUID_SUFFIX = "-0000-1000-8000-00805f9b34fb";
    private static final Pattern BT_UUID_PATTERN_32 = Pattern.compile(BT_UUID_PREFIX + "([0-9a-fA-F]{4})");
    private static final Pattern BT_UUID_PATTERN_128 = Pattern.compile(BT_UUID_PREFIX + "([0-9a-fA-F]{4})" + BT_UUID_SUFFIX);

    @NonNull
    private final String mUuidString;

    public CBUUID(@NonNull String uuidString) {
        // 16-bit (e.g. "1811")
        if (4 == uuidString.length()) {
            if (!UUID_PATTERN_16.matcher(uuidString).matches()) {
                throw new AndroidRuntimeException("Invalid uuid format. " + uuidString);
            }
        }
        // 32-bit (e.g. "00001811")
        else if (8 == uuidString.length()) {
            if (!UUID_PATTERN_32.matcher(uuidString).matches()) {
                throw new AndroidRuntimeException("Invalid uuid format. " + uuidString);
            }
            if (BT_UUID_PATTERN_32.matcher(uuidString).matches()) {
                uuidString = uuidString.substring(4);
            }
        }
        // 128-bit (e.g. "00001811-0000-1000-8000-00805f9b34fb")
        else if (36 == uuidString.length()) {
            if (!UUID_PATTERN_128.matcher(uuidString).matches()) {
                throw new AndroidRuntimeException("Invalid uuid format. " + uuidString);
            }
            if (BT_UUID_PATTERN_128.matcher(uuidString).matches()) {
                uuidString = uuidString.substring(4, 8);
            }
        }
        // fail
        else {
            throw new AndroidRuntimeException("Invalid length.");
        }
        mUuidString = uuidString.toUpperCase();
    }

    public CBUUID(@NonNull UUID androidUUID) {
        this(androidUUID.toString());
    }

    @NonNull
    public static CBUUID fromString(@NonNull String uuidString) {
        return new CBUUID(uuidString);
    }

    @NonNull
    public String uuidString() {
        return mUuidString;
    }

    @NonNull
    String uuidStringOfLongType() {
        return generateUuidOfLongType(mUuidString);
    }

    @NonNull
    UUID androidUUID() {
        return UUID.fromString(generateUuidOfLongType(mUuidString));
    }

    @NonNull
    private String generateUuidOfLongType(@NonNull String uuidString) {

        final String ret;

        // 16-bit (e.g. "1811")
        if (4 == uuidString.length()) {
            ret = BT_UUID_PREFIX + uuidString + BT_UUID_SUFFIX;
        }
        // 32-bit (e.g. "00001811")
        else if (8 == uuidString.length()) {
            ret = uuidString + BT_UUID_SUFFIX;
        }
        // 128-bit (e.g. "00001811-0000-1000-8000-00805f9b34fb")
        else if (36 == uuidString.length()) {
            ret = uuidString;
        }
        // fail
        else {
            throw new AndroidRuntimeException("Invalid length.");
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof CBUUID)) {
            return false;
        }
        CBUUID src = (CBUUID) obj;
        return this.uuidString().equalsIgnoreCase(src.uuidString());
    }

    @Override
    public String toString() {
        return "CBUUID{" + CBUUIDNameResolver.getName(mUuidString) + '}';
    }
}
