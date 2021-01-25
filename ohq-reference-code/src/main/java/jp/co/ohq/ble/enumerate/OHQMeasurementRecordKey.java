package jp.co.ohq.ble.enumerate;

import android.support.annotation.NonNull;

public enum OHQMeasurementRecordKey {
    /**
     * User Index (Type of value : BigDecimal)
     */
    UserIndexKey("User Index"),
    /**
     * Time Stamp (Type of value : String)
     */
    TimeStampKey("Time Stamp"),
    /**
     * Sequence Number (Type of value : BigDecimal)
     */
    SequenceNumberKey("Sequence Number"),
    /**
     * Blood Pressure Unit (Type of value : String, Unit is ["mmHg" or "kPa"])
     */
    BloodPressureUnitKey("Blood Pressure Unit"),
    /**
     * Systolic Blood Pressure (Type of value : BigDecimal)
     */
    SystolicKey("Systolic"),
    /**
     * Diastolic Blood Pressure (Type of value : BigDecimal)
     */
    DiastolicKey("Diastolic"),
    /**
     * Mean Arterial Pressure (Type of value : BigDecimal)
     */
    MeanArterialPressureKey("Mean Arterial Pressure"),
    /**
     * Pulse Rate (Type of value : BigDecimal)
     */
    PulseRateKey("Pulse Rate"),
    /**
     * Blood Pressure Measurement status (Type of value : EnumSet<BloodPressureMeasurementStatus>)
     */
    BloodPressureMeasurementStatusKey("Blood Pressure Measurement Status"),
    /**
     * Continuous Number of Measurements (Type of value : BigDecimal)
     */
    ContinuousNumberOfMeasurementsKey("Continuous Number of Measurements"),
    /**
     * Artifact Detection Count (Type of value : BigDecimal)
     */
    ArtifactDetectionCountKey("Artifact Detection Count"),
    /**
     * Arrhythmia Detection Count (Type of value : BigDecimal)
     */
    ArrhythmiaDetectionCountKey("Arrhythmia Detection Count"),
    /**
     * Room Temperature (Type of value : BigDecimal)
     */
    RoomTemperatureKey("Room Temperature"),
    /**
     * Weight Unit (Type of value : String, Value is ["kg" or "lb"])
     */
    WeightUnitKey("Weight Unit"),
    /**
     * Height Unit (Type of value : String, Value is ["m" of "in"])
     */
    HeightUnitKey("Height Unit"),
    /**
     * Weight (Type of value : BigDecimal)
     */
    WeightKey("Weight"),
    /**
     * Height (Type of value : BigDecimal)
     */
    HeightKey("Height"),
    /**
     * BMI (Type of value : BigDecimal)
     */
    BMIKey("BMI"),
    /**
     * Body Fat Percentage (Type of value : BigDecimal)
     */
    BodyFatPercentageKey("Body Fat Percentage"),
    /**
     * Basal Metabolism (Type of value : BigDecimal, Unit is ["kJ"])
     */
    BasalMetabolismKey("Basal Metabolism"),
    /**
     * Muscle Percentage (Type of value : BigDecimal)
     */
    MusclePercentageKey("Muscle Percentage"),
    /**
     * Muscle Mass (Type of value : BigDecimal, Unit is ["kg" or "lb"])
     */
    MuscleMassKey("Muscle Mass"),
    /**
     * Fat Free Mass (Type of value : BigDecimal, Unit is ["kg" or "lb"])
     */
    FatFreeMassKey("Fat Free Mass"),
    /**
     * Soft Lean Mass (Type of value : BigDecimal, Unit is ["kg" or "lb"])
     */
    SoftLeanMassKey("Soft Lean Mass"),
    /**
     * Body Water Mass (Type of value : BigDecimal, Unit is ["kg" or "lb"])
     */
    BodyWaterMassKey("Body Water Mass"),
    /**
     * Impedance (Type of value : BigDecimal, Unit is ["Ω"])
     */
    ImpedanceKey("Impedance"),
    /**
     * Skeletal Muscle Percentage (Type of value : BigDecimal)
     */
    SkeletalMusclePercentageKey("Skeletal Muscle Percentage"),
    /**
     * Visceral Fat Level (Type of value : BigDecimal)
     */
    VisceralFatLevelKey("Visceral Fat Level"),
    /**
     * Body Age (Type of value : BigDecimal)
     */
    BodyAgeKey("Body Age"),
    /**
     * Body Fat Percentage Stage Evaluation (Type of value : BigDecimal)
     */
    BodyFatPercentageStageEvaluationKey("Body Fat Percentage Stage Evaluation"),
    /**
     * Skeletal Muscle Percentage Stage Evaluation (Type of value : BigDecimal)
     */
    SkeletalMusclePercentageStageEvaluationKey("Skeletal Muscle Percentage Stage Evaluation"),
    /**
     * Visceral Fat Level Stage Evaluation (Type of value : BigDecimal)
     */
    VisceralFatLevelStageEvaluationKey("Visceral Fat Level Stage Evaluation"),;
    @NonNull
    private String mDescription;

    OHQMeasurementRecordKey(@NonNull String description) {
        mDescription = description;
    }

    @NonNull
    public String description() {
        return mDescription;
    }
}
