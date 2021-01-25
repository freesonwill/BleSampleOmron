package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

import jp.co.ohq.ble.enumerate.OHQBloodPressureMeasurementStatus;
import jp.co.ohq.utility.Bytes;

public class OmronMeasurementBP {

    private static final String UNIT_MMHG = "mmHg";
    private static final String UNIT_KPA = "kPa";

    private final static int SCALE = 3;
    @NonNull
    private String mUnit = "";
    @Nullable
    private BigDecimal mSequenceNumber;
    @Nullable
    private BigDecimal mSystolic;
    @Nullable
    private BigDecimal mDiastolic;
    @Nullable
    private BigDecimal mMeanArterialPressure;
    @Nullable
    private String mTimeStamp;
    @Nullable
    private BigDecimal mPulseRate;
    @Nullable
    private BigDecimal mUserID;
    @NonNull
    private EnumSet<OHQBloodPressureMeasurementStatus> mMeasurementStatus = EnumSet.noneOf(OHQBloodPressureMeasurementStatus.class);
    @Nullable
    private BigDecimal mContinuousNumberOfMeasurements;
    @Nullable
    private BigDecimal mArtifactDetectionCount;
    @Nullable
    private BigDecimal mArrhythmiaDetectionCount;
    @Nullable
    private BigDecimal mRoomTemperature;
    public OmronMeasurementBP(@NonNull byte[] data) {
        this(data, null, null);
    }

    public OmronMeasurementBP(@NonNull byte[] data, @Nullable BloodPressureFeature feature) {
        this(data, null, feature);
    }

    public OmronMeasurementBP(@NonNull byte[] data1, @Nullable byte[] data2) {
        this(data1, data2, null);
    }

    public OmronMeasurementBP(@NonNull byte[] data1, @Nullable byte[] data2, @Nullable BloodPressureFeature feature) {
        class DataParser {
            private void parse(@NonNull byte[] data, @Nullable BloodPressureFeature feature) {
                int offset = 0;

                EnumSet<Flag> flags = Flag.parse(Bytes.parse2BytesAsInt(data, offset, true));
                offset += 2;

                if (flags.contains(Flag.KpaUnit)) {
                    mUnit = UNIT_KPA;
                } else {
                    mUnit = UNIT_MMHG;
                }

                if (flags.contains(Flag.SequenceNumberPresent)) {
                    mSequenceNumber = new BigDecimal(Bytes.parse2BytesAsInt(data, offset, true));
                    offset += 2;
                }

                if (flags.contains(Flag.SystolicPresent)) {
                    mSystolic = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.DiastolicPresent)) {
                    mDiastolic = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.MeanArterialPressurePresent)) {
                    mMeanArterialPressure = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.TimeStampPresent)) {
                    mTimeStamp = Bytes.parse7BytesAsDateString(data, offset, true);
                    offset += 7;
                }

                if (flags.contains(Flag.PulseRatePresent)) {
                    mPulseRate = new BigDecimal(Bytes.parse2BytesAsSFloat(data, offset, true).floatValue()).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.UserIDPresent)) {
                    mUserID = new BigDecimal(data[offset] & 0xff);
                    offset += 1;
                }

                if (flags.contains(Flag.MeasurementStatusPresent)) {
                    mMeasurementStatus = OHQBloodPressureMeasurementStatus.parse(Bytes.parse2BytesAsInt(data, offset, true));
                    offset += 2;
                }

                if (flags.contains(Flag.ContinuousNumberPresent)) {
                    mContinuousNumberOfMeasurements = new BigDecimal(data[offset]);
                    offset += 1;
                }

                if (flags.contains(Flag.ArtifactDetectionCountPresent)) {
                    mArtifactDetectionCount = new BigDecimal(data[offset]);
                    offset += 1;
                }

                if (flags.contains(Flag.ArrhythmiaDetectionCountPresent)) {
                    mArrhythmiaDetectionCount = new BigDecimal(data[offset]);
                    offset += 1;
                }

                if (flags.contains(Flag.RoomTemperaturePresent)) {
                    mRoomTemperature = new BigDecimal(Bytes.parse2BytesAsInt(data, offset, true) * 0.1f).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }
            }
        }
        DataParser parser = new DataParser();
        parser.parse(data1, feature);
        if (null != data2) {
            parser.parse(data2, feature);
        }
    }

    @NonNull
    public String getUnit() {
        return mUnit;
    }

    @Nullable
    public BigDecimal getSequenceNumber() {
        return mSequenceNumber;
    }

    @Nullable
    public BigDecimal getSystolic() {
        return mSystolic;
    }

    @Nullable
    public BigDecimal getDiastolic() {
        return mDiastolic;
    }

    @Nullable
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

    @Nullable
    public BigDecimal getContinuousNumberOfMeasurements() {
        return mContinuousNumberOfMeasurements;
    }

    @Nullable
    public BigDecimal getArtifactDetectionCount() {
        return mArtifactDetectionCount;
    }

    @Nullable
    public BigDecimal getArrhythmiaDetectionCount() {
        return mArrhythmiaDetectionCount;
    }

    @Nullable
    public BigDecimal getRoomTemperature() {
        return mRoomTemperature;
    }

    @Override
    public String toString() {
        return "OmronMeasurementBP{" +
                "mUnit='" + mUnit + '\'' +
                ", mSequenceNumber=" + mSequenceNumber +
                ", mSystolic=" + mSystolic +
                ", mDiastolic=" + mDiastolic +
                ", mMeanArterialPressure=" + mMeanArterialPressure +
                ", mTimeStamp='" + mTimeStamp + '\'' +
                ", mPulseRate=" + mPulseRate +
                ", mUserID=" + mUserID +
                ", mMeasurementStatus=" + mMeasurementStatus +
                ", mContinuousNumberOfMeasurements=" + mContinuousNumberOfMeasurements +
                ", mArtifactDetectionCount=" + mArtifactDetectionCount +
                ", mArrhythmiaDetectionCount=" + mArrhythmiaDetectionCount +
                ", mRoomTemperature=" + mRoomTemperature +
                '}';
    }

    public enum Flag {
        KpaUnit(1),
        SequenceNumberPresent(1 << 1),
        SystolicPresent(1 << 2),
        DiastolicPresent(1 << 3),
        MeanArterialPressurePresent(1 << 4),
        TimeStampPresent(1 << 5),
        PulseRatePresent(1 << 6),
        UserIDPresent(1 << 7),
        MeasurementStatusPresent(1 << 8),
        ContinuousNumberPresent(1 << 9),
        ArtifactDetectionCountPresent(1 << 10),
        ArrhythmiaDetectionCountPresent(1 << 11),
        RoomTemperaturePresent(1 << 12),
        MultiplePacketMeasurement(1 << 13),;
        private final int mValue;

        Flag(int value) {
            this.mValue = value;
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
