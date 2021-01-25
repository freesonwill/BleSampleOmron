package jp.co.ohq.ble.enumerate;

import android.support.annotation.NonNull;

import java.util.EnumSet;

public enum OHQBloodPressureMeasurementStatus {
    BodyMovementDetected(1, "Body Movement Detected"),
    CuffTooLoose(1 << 1, "Cuff Too Loose"),
    IrregularPulseDetected(1 << 2, "Irregular Pulse Detected"),
    PulseRateTooHigher(1 << 3, "Pulse Rate Too Higher"),
    PulseRateTooLower(1 << 4, "Pulse Rate Too Lower"),
    ImproperMeasurementPosition(1 << 5, "Improper Measurement Position"),;
    private int mValue;
    @NonNull
    private String mDescription;

    OHQBloodPressureMeasurementStatus(int value, @NonNull String description) {
        mValue = value;
        mDescription = description;
    }

    @NonNull
    public static EnumSet<OHQBloodPressureMeasurementStatus> parse(int bits) {
        EnumSet<OHQBloodPressureMeasurementStatus> ret = EnumSet.noneOf(OHQBloodPressureMeasurementStatus.class);
        for (OHQBloodPressureMeasurementStatus type : values()) {
            if (type.contains(bits)) {
                ret.add(type);
            }
        }
        return ret;
    }

    private boolean contains(int bits) {
        return mValue == (bits & mValue);
    }

    @NonNull
    public String description() {
        return mDescription;
    }
}
