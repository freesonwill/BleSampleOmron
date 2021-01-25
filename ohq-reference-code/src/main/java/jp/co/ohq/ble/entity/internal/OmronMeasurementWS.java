package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

import jp.co.ohq.utility.Bytes;

public class OmronMeasurementWS {

    public static final String WEIGHT_UNIT_KILOGRAM = "kg";
    public static final String WEIGHT_UNIT_POUND = "lb";
    public static final String HEIGHT_UNIT_METER = "m";
    public static final String HEIGHT_UNIT_INCH = "in";

    private final static int SCALE = 3;
    private final static float WEIGHT_RESOLUTION_KG_DEFAULT = 0.005f;
    private final static float WEIGHT_RESOLUTION_LB_DEFAULT = 0.01f;
    private final static float HEIGHT_RESOLUTION_M_DEFAULT = 0.001f;
    private final static float HEIGHT_RESOLUTION_IN_DEFAULT = 0.1f;
    @NonNull
    private String mWeightUnit = "";
    @NonNull
    private String mHeightUnit = "";
    @Nullable
    private BigDecimal mSequenceNumber;
    @Nullable
    private BigDecimal mWeight;
    @Nullable
    private String mTimeStamp;
    @Nullable
    private BigDecimal mUserID;
    @Nullable
    private BigDecimal mBMI;
    @Nullable
    private BigDecimal mHeight;
    @Nullable
    private BigDecimal mBodyFatPercentage;
    @Nullable
    private BigDecimal mBasalMetabolism;
    @Nullable
    private BigDecimal mMusclePercentage;
    @Nullable
    private BigDecimal mMuscleMass;
    @Nullable
    private BigDecimal mFatFreeMass;
    @Nullable
    private BigDecimal mSoftLeanMass;
    @Nullable
    private BigDecimal mBodyWaterMass;
    @Nullable
    private BigDecimal mImpedance;
    @Nullable
    private BigDecimal mSkeletalMusclePercentage;
    @Nullable
    private BigDecimal mVisceralFatLevel;
    @Nullable
    private BigDecimal mBodyAge;
    @Nullable
    private BigDecimal mBodyFatPercentageStageEvaluation;
    @Nullable
    private BigDecimal mSkeletalMusclePercentageStageEvaluation;
    @Nullable
    private BigDecimal mVisceralFatLevelStageEvaluation;
    public OmronMeasurementWS(@NonNull byte[] data) {
        this(data, null, null);
    }

    public OmronMeasurementWS(@NonNull byte[] data, @Nullable BodyCompositionFeature feature) {
        this(data, null, feature);
    }

    public OmronMeasurementWS(@NonNull byte[] data1, @Nullable byte[] data2) {
        this(data1, data2, null);
    }

    public OmronMeasurementWS(@NonNull byte[] data1, @Nullable byte[] data2, @Nullable BodyCompositionFeature feature) {
        class DataParser {
            private void parse(@NonNull byte[] data, @Nullable BodyCompositionFeature feature) {
                int offset = 0;

                EnumSet<Flag> flags = Flag.parse((Bytes.parse4BytesAsInt(data, offset, true) & 0x00ffffff));
                offset += 3;

                final float weightMeasurementResolution;
                final float heightMeasurementResolution;
                if (flags.contains(Flag.ImperialUnit)) {
                    weightMeasurementResolution = null != feature ? feature.getWeightMeasurementResolutionLB() : WEIGHT_RESOLUTION_LB_DEFAULT;
                    heightMeasurementResolution = null != feature ? feature.getHeightMeasurementResolutionIn() : HEIGHT_RESOLUTION_IN_DEFAULT;
                    mWeightUnit = WEIGHT_UNIT_POUND;
                    mHeightUnit = HEIGHT_UNIT_INCH;
                } else {
                    weightMeasurementResolution = null != feature ? feature.getWeightMeasurementResolutionKG() : WEIGHT_RESOLUTION_KG_DEFAULT;
                    heightMeasurementResolution = null != feature ? feature.getHeightMeasurementResolutionM() : HEIGHT_RESOLUTION_M_DEFAULT;
                    mWeightUnit = WEIGHT_UNIT_KILOGRAM;
                    mHeightUnit = HEIGHT_UNIT_METER;
                }
                float rawValue;

                if (flags.contains(Flag.SequenceNumberPresent)) {
                    mSequenceNumber = new BigDecimal(Bytes.parse2BytesAsInt(data, offset, true));
                    offset += 2;
                }

                if (flags.contains(Flag.WeightPresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mWeight = new BigDecimal(rawValue * weightMeasurementResolution).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.TimeStampPresent)) {
                    mTimeStamp = Bytes.parse7BytesAsDateString(data, offset, true);
                    offset += 7;
                }

                if (flags.contains(Flag.UserIDPresent)) {
                    mUserID = new BigDecimal(data[offset] & 0xff);
                    offset += 1;
                }

                if (flags.contains(Flag.BMIAndHeightPresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mBMI = new BigDecimal(rawValue * 0.1f).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;

                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mHeight = new BigDecimal(rawValue * heightMeasurementResolution).setScale(1, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.BodyFatPercentagePresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mBodyFatPercentage = new BigDecimal(rawValue * 0.1f * 0.01f).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.BasalMetabolismPresent)) {
                    mBasalMetabolism = new BigDecimal(Bytes.parse2BytesAsInt(data, offset, true));
                    offset += 2;
                }

                if (flags.contains(Flag.MusclePercentagePresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mMusclePercentage = new BigDecimal(rawValue * 0.1f * 0.01f).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.MuscleMassPresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mMuscleMass = new BigDecimal(rawValue * weightMeasurementResolution).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.FatFreeMassPresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mFatFreeMass = new BigDecimal(rawValue * weightMeasurementResolution).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.SoftLeanMassPresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mSoftLeanMass = new BigDecimal(rawValue * weightMeasurementResolution).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.BodyWaterMassPresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mBodyWaterMass = new BigDecimal(rawValue * weightMeasurementResolution).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.ImpedancePresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mImpedance = new BigDecimal(rawValue * 0.1f).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.SkeletalMusclePercentagePresent)) {
                    rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
                    mSkeletalMusclePercentage = new BigDecimal(rawValue * 0.1f * 0.01f).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 2;
                }

                if (flags.contains(Flag.VisceralFatLevelPresent)) {
                    rawValue = data[offset];
                    mVisceralFatLevel = new BigDecimal(rawValue * 0.5f).setScale(SCALE, RoundingMode.HALF_UP);
                    offset += 1;
                }

                if (flags.contains(Flag.BodyAgePresent)) {
                    mBodyAge = new BigDecimal(data[offset]);
                    offset += 1;
                }

                if (flags.contains(Flag.BodyFatPercentageStageEvaluationPresent)) {
                    mBodyFatPercentageStageEvaluation = new BigDecimal(data[offset]);
                    offset += 1;
                }

                if (flags.contains(Flag.SkeletalMusclePercentageStageEvaluationPresent)) {
                    mSkeletalMusclePercentageStageEvaluation = new BigDecimal(data[offset]);
                    offset += 1;
                }

                if (flags.contains(Flag.VisceralFatLevelStageEvaluationPresent)) {
                    mVisceralFatLevelStageEvaluation = new BigDecimal(data[offset]);
                    offset += 1;
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
    public String getWeightUnit() {
        return mWeightUnit;
    }

    @NonNull
    public String getHeightUnit() {
        return mHeightUnit;
    }

    @Nullable
    public BigDecimal getSequenceNumber() {
        return mSequenceNumber;
    }

    @Nullable
    public BigDecimal getWeight() {
        return mWeight;
    }

    @Nullable
    public String getTimeStamp() {
        return mTimeStamp;
    }

    @Nullable
    public BigDecimal getUserID() {
        return mUserID;
    }

    @Nullable
    public BigDecimal getBMI() {
        return mBMI;
    }

    @Nullable
    public BigDecimal getHeight() {
        return mHeight;
    }

    @Nullable
    public BigDecimal getBodyFatPercentage() {
        return mBodyFatPercentage;
    }

    @Nullable
    public BigDecimal getBasalMetabolism() {
        return mBasalMetabolism;
    }

    @Nullable
    public BigDecimal getMusclePercentage() {
        return mMusclePercentage;
    }

    @Nullable
    public BigDecimal getMuscleMass() {
        return mMuscleMass;
    }

    @Nullable
    public BigDecimal getFatFreeMass() {
        return mFatFreeMass;
    }

    @Nullable
    public BigDecimal getSoftLeanMass() {
        return mSoftLeanMass;
    }

    @Nullable
    public BigDecimal getBodyWaterMass() {
        return mBodyWaterMass;
    }

    @Nullable
    public BigDecimal getImpedance() {
        return mImpedance;
    }

    @Nullable
    public BigDecimal getSkeletalMusclePercentage() {
        return mSkeletalMusclePercentage;
    }

    @Nullable
    public BigDecimal getVisceralFatLevel() {
        return mVisceralFatLevel;
    }

    @Nullable
    public BigDecimal getBodyAge() {
        return mBodyAge;
    }

    @Nullable
    public BigDecimal getBodyFatPercentageStageEvaluation() {
        return mBodyFatPercentageStageEvaluation;
    }

    @Nullable
    public BigDecimal getSkeletalMusclePercentageStageEvaluation() {
        return mSkeletalMusclePercentageStageEvaluation;
    }

    @Nullable
    public BigDecimal getVisceralFatLevelStageEvaluation() {
        return mVisceralFatLevelStageEvaluation;
    }

    @Override
    public String toString() {
        return "OmronMeasurementWS{" +
                "mWeightUnit='" + mWeightUnit + '\'' +
                ", mHeightUnit='" + mHeightUnit + '\'' +
                ", mSequenceNumber=" + mSequenceNumber +
                ", mWeight=" + mWeight +
                ", mTimeStamp='" + mTimeStamp + '\'' +
                ", mUserID=" + mUserID +
                ", mBMI=" + mBMI +
                ", mHeight=" + mHeight +
                ", mBodyFatPercentage=" + mBodyFatPercentage +
                ", mBasalMetabolism=" + mBasalMetabolism +
                ", mMusclePercentage=" + mMusclePercentage +
                ", mMuscleMass=" + mMuscleMass +
                ", mFatFreeMass=" + mFatFreeMass +
                ", mSoftLeanMass=" + mSoftLeanMass +
                ", mBodyWaterMass=" + mBodyWaterMass +
                ", mImpedance=" + mImpedance +
                ", mSkeletalMusclePercentage=" + mSkeletalMusclePercentage +
                ", mVisceralFatLevel=" + mVisceralFatLevel +
                ", mBodyAge=" + mBodyAge +
                ", mBodyFatPercentageStageEvaluation=" + mBodyFatPercentageStageEvaluation +
                ", mSkeletalMusclePercentageStageEvaluation=" + mSkeletalMusclePercentageStageEvaluation +
                ", mVisceralFatLevelStageEvaluation=" + mVisceralFatLevelStageEvaluation +
                '}';
    }

    public enum Flag {
        ImperialUnit(1),
        SequenceNumberPresent(1 << 1),
        WeightPresent(1 << 2),
        TimeStampPresent(1 << 3),
        UserIDPresent(1 << 4),
        BMIAndHeightPresent(1 << 5),
        BodyFatPercentagePresent(1 << 6),
        BasalMetabolismPresent(1 << 7),
        MusclePercentagePresent(1 << 8),
        MuscleMassPresent(1 << 9),
        FatFreeMassPresent(1 << 10),
        SoftLeanMassPresent(1 << 11),
        BodyWaterMassPresent(1 << 12),
        ImpedancePresent(1 << 13),
        SkeletalMusclePercentagePresent(1 << 14),
        VisceralFatLevelPresent(1 << 15),
        BodyAgePresent(1 << 16),
        BodyFatPercentageStageEvaluationPresent(1 << 17),
        SkeletalMusclePercentageStageEvaluationPresent(1 << 18),
        VisceralFatLevelStageEvaluationPresent(1 << 19),
        MultiplePacketMeasurement(1 << 20),;
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
