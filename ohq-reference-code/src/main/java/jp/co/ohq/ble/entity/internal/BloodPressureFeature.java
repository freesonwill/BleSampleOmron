package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;

import java.util.EnumSet;

import jp.co.ohq.utility.Bytes;

public class BloodPressureFeature {

    @NonNull
    private final EnumSet<SupportedFlag> mSupportedFlags;

    public BloodPressureFeature(@NonNull byte[] data) {
        int feature = Bytes.parse2BytesAsInt(data, 0, true);
        mSupportedFlags = SupportedFlag.parse(feature);
    }

    @Override
    public String toString() {
        return "BloodPressureFeature{" +
                "mSupportedFlags=" + mSupportedFlags +
                '}';
    }

    @NonNull
    public EnumSet<SupportedFlag> getSupportedFlags() {
        return mSupportedFlags;
    }

    public enum SupportedFlag {
        BodyMovementDetection(1),
        CuffFitDetection(1 << 1),
        IrregularPulseDetection(1 << 2),
        PulseRateRangeDetection(1 << 3),
        MeasurementPositionDetection(1 << 4),
        MultipleBond(1 << 5),;
        private int mValue;

        SupportedFlag(int value) {
            mValue = value;
        }

        @NonNull
        static EnumSet<SupportedFlag> parse(int bits) {
            EnumSet<SupportedFlag> ret = EnumSet.noneOf(SupportedFlag.class);
            for (SupportedFlag type : values()) {
                if (type.contains(bits)) {
                    ret.add(type);
                }
            }
            return ret;
        }

        private boolean contains(int bits) {
            return mValue == (bits & mValue);
        }
    }
}
