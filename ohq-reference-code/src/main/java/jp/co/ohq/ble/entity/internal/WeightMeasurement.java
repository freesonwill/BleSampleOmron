package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

import jp.co.ohq.utility.Bytes;

public class WeightMeasurement {

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
    private String mWeightUnit;
    @NonNull
    private String mHeightUnit;
    @NonNull
    private BigDecimal mWeight;
    @Nullable
    private String mTimeStamp;
    @Nullable
    private BigDecimal mUserID;
    @Nullable
    private BigDecimal mBMI;
    @Nullable
    private BigDecimal mHeight;
    public WeightMeasurement(@NonNull byte[] data) {
        this(data, null);
    }

    public WeightMeasurement(@NonNull byte[] data, @Nullable WeightScaleFeature feature) {
        int offset = 0;

        EnumSet<Flag> flags = Flag.parse((int) data[offset]);
        offset += 1;

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

        rawValue = Bytes.parse2BytesAsFloat(data, offset, true);
        mWeight = new BigDecimal(rawValue * weightMeasurementResolution).setScale(SCALE, RoundingMode.HALF_UP);
        offset += 2;

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
    }

    @NonNull
    public String getWeightUnit() {
        return mWeightUnit;
    }

    @NonNull
    public String getHeightUnit() {
        return mHeightUnit;
    }

    @NonNull
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

    @Override
    public String toString() {
        return "WeightMeasurement{" +
                "mWeightUnit='" + mWeightUnit + '\'' +
                ", mHeightUnit='" + mHeightUnit + '\'' +
                ", mWeight=" + mWeight +
                ", mTimeStamp='" + mTimeStamp + '\'' +
                ", mUserID=" + mUserID +
                ", mBMI=" + mBMI +
                ", mHeight=" + mHeight +
                '}';
    }

    public enum Flag {
        ImperialUnit(1),
        TimeStampPresent(1 << 1),
        UserIDPresent(1 << 2),
        BMIAndHeightPresent(1 << 3),;
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
