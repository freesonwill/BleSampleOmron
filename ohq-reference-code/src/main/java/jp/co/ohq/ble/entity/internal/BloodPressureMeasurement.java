package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

import jp.co.ohq.ble.enumerate.OHQBloodPressureMeasurementStatus;
import jp.co.ohq.utility.Bytes;

public class BloodPressureMeasurement {

    private static final String UNIT_MMHG = "mmHg";
    private static final String UNIT_KPA = "kPa";

    private final static int SCALE = 3;
    @NonNull
    private String mUnit;
    @NonNull
    private BigDecimal mSystolic;
    @NonNull
    private BigDecimal mDiastolic;
    @NonNull
    private BigDecimal mMeanArterialPressure;
    @Nullable
    private String mTimeStamp;
    @Nullable
    private BigDecimal mPulseRate;
    @Nullable
    private BigDecimal mUserID;
    @NonNull
    private EnumSet<OHQBloodPressureMeasurementStatus> mMeasurementStatus;
    public BloodPressureMeasurement(@NonNull byte[] data) {
        int offset = 0;

        EnumSet<Flag> flags = Flag.parse((int) data[offset]);
        offset += 1;

        if (flags.contains(Flag.KpaUnit)) {
            mUnit = UNIT_KPA;
        } else {
            mUnit = UNIT_MMHG;
        }

        mSystolic = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
        offset += 2;

        mDiastolic = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
        offset += 2;

        mMeanArterialPressure = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
        offset += 2;

        if (flags.contains(Flag.TimeStampPresent)) {
            mTimeStamp = Bytes.parse7BytesAsDateString(data, offset, true);
            offset += 7;
        } else {
            mTimeStamp = null;
        }

        if (flags.contains(Flag.PulseRatePresent)) {
            mPulseRate = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
            offset += 2;
        } else {
            mPulseRate = null;
        }

        if (flags.contains(Flag.UserIDPresent)) {
            mUserID = new BigDecimal(data[offset] & 0xff);
            offset += 1;
        } else {
            mUserID = null;
        }

        if (flags.contains(Flag.MeasurementStatusPresent)) {
            mMeasurementStatus = OHQBloodPressureMeasurementStatus.parse((int) data[offset]);
            offset += 1;
        } else {
            mMeasurementStatus = EnumSet.noneOf(OHQBloodPressureMeasurementStatus.class);
        }
    }

    @NonNull
    public String getUnit() {
        return mUnit;
    }

    @NonNull
    public BigDecimal getSystolic() {
        return mSystolic;
    }

    @NonNull
    public BigDecimal getDiastolic() {
        return mDiastolic;
    }

    @NonNull
    public BigDecimal getMeanArterialPressure() {
        return mMeanArterialPressure;
    }

    @Nullable
    public String getTimeStamp() {
        return mTimeStamp;
    }

    @Nullable
    public BigDecimal getPulseRate() {
        return mPulseRate;
    }

    @Nullable
    public BigDecimal getUserID() {
        return mUserID;
    }

    @NonNull
    public EnumSet<OHQBloodPressureMeasurementStatus> getMeasurementStatus() {
        return mMeasurementStatus;
    }

    @Override
    public String toString() {
        return "BloodPressureMeasurement{" +
                "mUnit='" + mUnit + '\'' +
                ", mSystolic=" + mSystolic +
                ", mDiastolic=" + mDiastolic +
                ", mMeanArterialPressure=" + mMeanArterialPressure +
                ", mTimeStamp='" + mTimeStamp + '\'' +
                ", mPulseRate=" + mPulseRate +
                ", mUserID=" + mUserID +
                ", mMeasurementStatus=" + mMeasurementStatus +
                '}';
    }

    public enum Flag {
        KpaUnit(1),
        TimeStampPresent(1 << 1),
        PulseRatePresent(1 << 2),
        UserIDPresent(1 << 3),
        MeasurementStatusPresent(1 << 4),;
        private int mValue;

        Flag(int value) {
            mValue = value;
        }

        @NonNull
        public static EnumSet<Flag> parse(int bits) {
            EnumSet<Flag> ret = EnumSet.noneOf(Flag.class);
            for (Flag type : values()) {
                if (type.contains(bits)) {
                    ret.add(type);
                }
            }
            return ret;
        }

        public boolean contains(int bits) {
            return mValue == (bits & mValue);
        }
    }
}
