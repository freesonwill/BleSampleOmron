package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;

import java.util.EnumSet;
import java.util.Locale;

import jp.co.ohq.utility.Bytes;

public class WeightScaleFeature {

    private final static float WEIGHT_RESOLUTION_KG[] = {0.005f, 0.5f, 0.2f, 0.1f, 0.05f, 0.02f, 0.01f, 0.005f};
    private final static float WEIGHT_RESOLUTION_LB[] = {0.01f, 1.0f, 0.5f, 0.2f, 0.1f, 0.05f, 0.02f, 0.01f};

    private final static float HEIGHT_RESOLUTION_M[] = {0.001f, 0.01f, 0.005f, 0.001f};
    private final static float HEIGHT_RESOLUTION_IN[] = {0.1f, 1.0f, 0.5f, 0.1f};
    @NonNull
    private final EnumSet<SupportedFlag> mSupportedFlags;
    private final float mWeightMeasurementResolutionKG;
    private final float mWeightMeasurementResolutionLB;
    private final float mHeightMeasurementResolutionM;
    private final float mHeightMeasurementResolutionIn;
    public WeightScaleFeature(@NonNull byte[] data) {
        int feature = Bytes.parse4BytesAsInt(data, 0, true);
        mSupportedFlags = SupportedFlag.parse(feature);
        int numberOfWeightMeasurementResolution = (feature >> 3) & 0x0000000F;
        mWeightMeasurementResolutionKG = WEIGHT_RESOLUTION_KG[numberOfWeightMeasurementResolution];
        mWeightMeasurementResolutionLB = WEIGHT_RESOLUTION_LB[numberOfWeightMeasurementResolution];
        int numberOfHeightMeasurementResolution = (feature >> 7) & 0x00000007;
        mHeightMeasurementResolutionM = HEIGHT_RESOLUTION_M[numberOfHeightMeasurementResolution];
        mHeightMeasurementResolutionIn = HEIGHT_RESOLUTION_IN[numberOfHeightMeasurementResolution];
    }

    @Override
    public String toString() {
        return "WeightScaleFeature{" +
                "mSupportedFlags=" + mSupportedFlags +
                ", mWeightMeasurementResolutionKG=" + mWeightMeasurementResolutionKG +
                ", mWeightMeasurementResolutionLB=" + mWeightMeasurementResolutionLB +
                ", mHeightMeasurementResolutionM=" + mHeightMeasurementResolutionM +
                ", mHeightMeasurementResolutionIn=" + mHeightMeasurementResolutionIn +
                '}';
    }

    @NonNull
    public EnumSet<SupportedFlag> getSupportedFlags() {
        return mSupportedFlags;
    }

    public float getWeightMeasurementResolutionKG() {
        return mWeightMeasurementResolutionKG;
    }

    public float getWeightMeasurementResolutionLB() {
        return mWeightMeasurementResolutionLB;
    }

    public float getHeightMeasurementResolutionM() {
        return mHeightMeasurementResolutionM;
    }

    public float getHeightMeasurementResolutionIn() {
        return mHeightMeasurementResolutionIn;
    }

    public enum SupportedFlag {
        TimeStamp(1),
        MultipleUsers(1 << 1),
        BMI(1 << 2),;
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
