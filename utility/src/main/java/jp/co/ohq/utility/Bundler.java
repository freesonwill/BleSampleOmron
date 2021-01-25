package jp.co.ohq.utility;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public class Bundler {

    @NonNull
    public static Bundle bundle(@NonNull final Object... args) {
        return bundle(null, args);
    }

    @NonNull
    public static Bundle bundle(@Nullable Bundle bundle, @NonNull final Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("The number of arguments is not an even number.");
        }
        if (null == bundle) {
            bundle = new Bundle();
        }
        for (int i = 0, size = args.length; i < size; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("Key type is not String.");
            }
            final String key = (String) args[i];
            final Object value = args[i + 1];
            if (value == null) {
                bundle.putString(key, null);
            }
            if (value instanceof Boolean) {
                bundle.putBoolean(key, (Boolean) value);
            } else if (value instanceof boolean[]) {
                bundle.putBooleanArray(key, (boolean[]) value);
            } else if (value instanceof Bundle) {
                bundle.putBundle(key, (Bundle) value);
            } else if (value instanceof Byte) {
                bundle.putByte(key, (Byte) value);
            } else if (value instanceof byte[]) {
                bundle.putByteArray(key, (byte[]) value);
            } else if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof String[]) {
                bundle.putStringArray(key, (String[]) value);
            } else if (value instanceof Character) {
                bundle.putChar(key, (Character) value);
            } else if (value instanceof char[]) {
                bundle.putCharArray(key, (char[]) value);
            } else if (value instanceof CharSequence) {
                bundle.putCharSequence(key, (CharSequence) value);
            } else if (value instanceof CharSequence[]) {
                bundle.putCharSequenceArray(key, (CharSequence[]) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double) value);
            } else if (value instanceof double[]) {
                bundle.putDoubleArray(key, (double[]) value);
            } else if (value instanceof Float) {
                bundle.putFloat(key, (Float) value);
            } else if (value instanceof float[]) {
                bundle.putFloatArray(key, (float[]) value);
            } else if (value instanceof Short) {
                bundle.putShort(key, (Short) value);
            } else if (value instanceof short[]) {
                bundle.putShortArray(key, (short[]) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof int[]) {
                bundle.putIntArray(key, (int[]) value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (Long) value);
            } else if (value instanceof long[]) {
                bundle.putLongArray(key, (long[]) value);
            } else if (value instanceof Parcelable) {
                bundle.putParcelable(key, (Parcelable) value);
            } else if (value instanceof Parcelable[]) {
                bundle.putParcelableArray(key, (Parcelable[]) value);
            } else if (value instanceof Serializable) {
                bundle.putSerializable(key, (Serializable) value);
            } else {
                throw new IllegalArgumentException("Illegal key. " + key);
            }
        }
        return bundle;
    }
}
