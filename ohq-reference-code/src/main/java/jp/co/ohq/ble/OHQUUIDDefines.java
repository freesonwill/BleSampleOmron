//
//  java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.ble;

import android.support.annotation.NonNull;

import jp.co.ohq.androidcorebluetooth.CBUUID;

class OHQUUIDDefines {

    @NonNull
    public static String description(@NonNull CBUUID uuid) {
        String ret;
        try {
            ret = valueOf(uuid).description();
        } catch (IllegalArgumentException e) {
            ret = uuid.uuidString();
        }
        return ret;
    }

    @NonNull
    private static IUUIDDefines valueOf(@NonNull CBUUID uuid) {
        IUUIDDefines ret = null;
        try {
            ret = Service.valueOf(uuid);
        } catch (IllegalArgumentException e) {
            // nop
        }
        try {
            ret = Characteristic.valueOf(uuid);
        } catch (IllegalArgumentException e) {
            // nop
        }
        try {
            ret = Descriptor.valueOf(uuid);
        } catch (IllegalArgumentException e) {
            // nop
        }
        if (null == ret) {
            throw new IllegalArgumentException("Unknown uuid. " + uuid.uuidString());
        }
        return ret;
    }

    public enum Service implements IUUIDDefines {
        GenericAccess("1800", "Generic Access"),
        GenericAttribute("1801", "Generic Attribute"),
        ImmediateAlert("1802", "Immediate Alert"),
        LinkLoss("1803", "Link Loss"),
        TxPower("1804", "Tx Power"),
        CurrentTimeService("1805", "Current Time Service"),
        ReferenceTimeUpdateService("1806", "Reference Time Update Service"),
        NextDSTChangeService("1807", "Next DST Change Service"),
        Glucose("1808", "Glucose"),
        HealthThermometer("1809", "Health Thermometer"),
        DeviceInformation("180A", "Device Information"),
        HeartRate("180D", "Heart Rate"),
        PhoneAlertStatusService("180E", "Phone Alert Status Service"),
        BatteryService("180F", "Battery Service"),
        BloodPressure("1810", "Blood Pressure"),
        AlertNotificationService("1811", "Alert Notification Service"),
        HumanInterfaceDevice("1812", "Human Interface Device"),
        ScanParameters("1813", "Scan Parameters"),
        RunningSpeedandCadence("1814", "Running Speed and Cadence"),
        AutomationIO("1815", "Automation IO"),
        CyclingSpeedandCadence("1816", "Cycling Speed and Cadence"),
        CyclingPower("1818", "Cycling Power"),
        LocationandNavigation("1819", "Location and Navigation"),
        EnvironmentalSensing("181A", "Environmental Sensing"),
        BodyComposition("181B", "Body Composition"),
        UserData("181C", "User Data"),
        WeightScale("181D", "Weight Scale"),
        BondManagementService("181E", "Bond Management Service"),
        ContinuousGlucoseMonitoring("181F", "Continuous Glucose Monitoring"),
        InternetProtocolSupportService("1820", "Internet Protocol Support Service"),
        IndoorPositioning("1821", "Indoor Positioning"),
        PulseOximeterService("1822", "Pulse Oximeter Service"),
        HTTPProxy("1823", "HTTP Proxy"),
        TransportDiscovery("1824", "Transport Discovery"),
        ObjectTransferService("1825", "Object Transfer Service"),
        FitnessMachine("1826", "Fitness Machine"),
        MeshProvisioningService("1827", "Mesh Provisioning Service"),
        MeshProxyService("1828", "Mesh Proxy Service"),

        OmronOptionalService("5DF5E817-A945-4F81-89C0-3D4E9759C07C", "Omron Optional Service"),;
        @NonNull
        private final String mUuidString;
        @NonNull
        private final String mDescription;

        Service(@NonNull String uuidString, @NonNull String description) {
            mUuidString = uuidString;
            mDescription = description;
        }

        @NonNull
        public static Service valueOf(@NonNull CBUUID uuid) {
            for (Service type : values()) {
                if (type.uuid().equals(uuid)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown uuid. " + uuid.uuidString());
        }

        @NonNull
        public CBUUID uuid() {
            return CBUUID.fromString(mUuidString);
        }

        @NonNull
        public String description() {
            return mDescription;
        }
    }

    public enum Characteristic implements IUUIDDefines {
        DeviceName("2A00", "Device Name"),
        Appearance("2A01", "Appearance"),
        PeripheralPrivacyFlag("2A02", "Peripheral Privacy Flag"),
        ReconnectionAddress("2A03", "Reconnection Address"),
        PeripheralPreferredConnectionParameters("2A04", "Peripheral Preferred Connection Parameters"),
        ServiceChanged("2A05", "Service Changed"),
        AlertLevel("2A06", "Alert Level"),
        TxPowerLevel("2A07", "Tx Power Level"),
        DateTime("2A08", "Date Time"),
        DayofWeek("2A09", "Day of Week"),
        DayDateTime("2A0A", "Day Date Time"),
        ExactTime100("2A0B", "Exact Time 100"),
        ExactTime256("2A0C", "Exact Time 256"),
        DSTOffset("2A0D", "DST Offset"),
        TimeZone("2A0E", "Time Zone"),
        LocalTimeInformation("2A0F", "Local Time Information"),
        SecondaryTimeZone("2A10", "Secondary Time Zone"),
        TimewithDST("2A11", "Time with DST"),
        TimeAccuracy("2A12", "Time Accuracy"),
        TimeSource("2A13", "Time Source"),
        ReferenceTimeInformation("2A14", "Reference Time Information"),
        TimeBroadcast("2A15", "Time Broadcast"),
        TimeUpdateControlPoint("2A16", "Time Update Control Point"),
        TimeUpdateState("2A17", "Time Update State"),
        GlucoseMeasurement("2A18", "Glucose Measurement"),
        BatteryLevel("2A19", "Battery Level"),
        BatteryPowerState("2A1A", "Battery Power State"),
        BatteryLevelState("2A1B", "Battery Level State"),
        TemperatureMeasurement("2A1C", "Temperature Measurement"),
        TemperatureType("2A1D", "Temperature Type"),
        IntermediateTemperature("2A1E", "Intermediate Temperature"),
        TemperatureCelsius("2A1F", "Temperature Celsius"),
        TemperatureFahrenheit("2A20", "Temperature Fahrenheit"),
        MeasurementInterval("2A21", "Measurement Interval"),
        BootKeyboardInputReport("2A22", "Boot Keyboard Input Report"),
        SystemID("2A23", "System ID"),
        ModelNumberString("2A24", "Model Number String"),
        SerialNumberString("2A25", "Serial Number String"),
        FirmwareRevisionString("2A26", "Firmware Revision String"),
        HardwareRevisionString("2A27", "Hardware Revision String"),
        SoftwareRevisionString("2A28", "Software Revision String"),
        ManufacturerNameString("2A29", "Manufacturer Name String"),
        IEEE1107320601RegulatoryCertificationDataList("2A2A", "IEEE 11073-20601 Regulatory Certification Data List"),
        CurrentTime("2A2B", "Current Time"),
        MagneticDeclination("2A2C", "Magnetic Declination"),
        Position2D("2A2F", "Position 2D"),
        Position3D("2A30", "Position 3D"),
        ScanRefresh("2A31", "Scan Refresh"),
        BootKeyboardOutputReport("2A32", "Boot Keyboard Output Report"),
        BootMouseInputReport("2A33", "Boot Mouse Input Report"),
        GlucoseMeasurementContext("2A34", "Glucose Measurement Context"),
        BloodPressureMeasurement("2A35", "Blood Pressure Measurement"),
        IntermediateCuffPressure("2A36", "Intermediate Cuff Pressure"),
        HeartRateMeasurement("2A37", "Heart Rate Measurement"),
        BodySensorLocation("2A38", "Body Sensor Location"),
        HeartRateControlPoint("2A39", "Heart Rate Control Point"),
        Removable("2A3A", "Removable"),
        ServiceRequired("2A3B", "Service Required"),
        ScientificTemperatureCelsius("2A3C", "Scientific Temperature Celsius"),
        String("2A3D", "String"),
        NetworkAvailability("2A3E", "Network Availability"),
        AlertStatus("2A3F", "Alert Status"),
        RingerControlpoint("2A40", "Ringer Control point"),
        RingerSetting("2A41", "Ringer Setting"),
        AlertCategoryIDBitMask("2A42", "Alert Category ID Bit Mask"),
        AlertCategoryID("2A43", "Alert Category ID"),
        AlertNotificationControlPoint("2A44", "Alert Notification Control Point"),
        UnreadAlertStatus("2A45", "Unread Alert Status"),
        NewAlert("2A46", "New Alert"),
        SupportedNewAlertCategory("2A47", "Supported New Alert Category"),
        SupportedUnreadAlertCategory("2A48", "Supported Unread Alert Category"),
        BloodPressureFeature("2A49", "Blood Pressure Feature"),
        HIDInformation("2A4A", "HID Information"),
        ReportMap("2A4B", "Report Map"),
        HIDControlPoint("2A4C", "HID Control Point"),
        Report("2A4D", "Report"),
        ProtocolMode("2A4E", "Protocol Mode"),
        ScanIntervalWindow("2A4F", "Scan Interval Window"),
        PnPID("2A50", "PnP ID"),
        GlucoseFeature("2A51", "Glucose Feature"),
        RecordAccessControlPoint("2A52", "Record Access Control Point"),
        RSCMeasurement("2A53", "RSC Measurement"),
        RSCFeature("2A54", "RSC Feature"),
        SCControlPoint("2A55", "SC Control Point"),
        Digital("2A56", "Digital"),
        DigitalOutput("2A57", "Digital Output"),
        Analog("2A58", "Analog"),
        AnalogOutput("2A59", "Analog Output"),
        Aggregate("2A5A", "Aggregate"),
        CSCMeasurement("2A5B", "CSC Measurement"),
        CSCFeature("2A5C", "CSC Feature"),
        SensorLocation("2A5D", "Sensor Location"),
        PLXSpotCheckMeasurement("2A5E", "PLX Spot-Check Measurement"),
        PLXContinuousMeasurementCharacteristic("2A5F", "PLX Continuous Measurement Characteristic"),
        PLXFeatures("2A60", "PLX Features"),
        PulseOximetryControlPoint("2A62", "Pulse Oximetry Control Point"),
        CyclingPowerMeasurement("2A63", "Cycling Power Measurement"),
        CyclingPowerVector("2A64", "Cycling Power Vector"),
        CyclingPowerFeature("2A65", "Cycling Power Feature"),
        CyclingPowerControlPoint("2A66", "Cycling Power Control Point"),
        LocationandSpeedCharacteristic("2A67", "Location and Speed Characteristic"),
        Navigation("2A68", "Navigation"),
        PositionQuality("2A69", "Position Quality"),
        LNFeature("2A6A", "LN Feature"),
        LNControlPoint("2A6B", "LN Control Point"),
        Elevation("2A6C", "Elevation"),
        Pressure("2A6D", "Pressure"),
        Temperature("2A6E", "Temperature"),
        Humidity("2A6F", "Humidity"),
        TrueWindSpeed("2A70", "True Wind Speed"),
        TrueWindDirection("2A71", "True Wind Direction"),
        ApparentWindSpeed("2A72", "Apparent Wind Speed"),
        ApparentWindDirection("2A73", "Apparent Wind Direction"),
        GustFactor("2A74", "Gust Factor"),
        PollenConcentration("2A75", "Pollen Concentration"),
        UVIndex("2A76", "UV Index"),
        Irradiance("2A77", "Irradiance"),
        Rainfall("2A78", "Rainfall"),
        WindChill("2A79", "Wind Chill"),
        HeatIndex("2A7A", "Heat Index"),
        DewPoint("2A7B", "Dew Point"),
        DescriptorValueChanged("2A7D", "Descriptor Value Changed"),
        AerobicHeartRateLowerLimit("2A7E", "Aerobic Heart Rate Lower Limit"),
        AerobicThreshold("2A7F", "Aerobic Threshold"),
        Age("2A80", "Age"),
        AnaerobicHeartRateLowerLimit("2A81", "Anaerobic Heart Rate Lower Limit"),
        AnaerobicHeartRateUpperLimit("2A82", "Anaerobic Heart Rate Upper Limit"),
        AnaerobicThreshold("2A83", "Anaerobic Threshold"),
        AerobicHeartRateUpperLimit("2A84", "Aerobic Heart Rate Upper Limit"),
        DateofBirth("2A85", "Date of Birth"),
        DateofThresholdAssessment("2A86", "Date of Threshold Assessment"),
        EmailAddress("2A87", "Email Address"),
        FatBurnHeartRateLowerLimit("2A88", "Fat Burn Heart Rate Lower Limit"),
        FatBurnHeartRateUpperLimit("2A89", "Fat Burn Heart Rate Upper Limit"),
        FirstName("2A8A", "First Name"),
        FiveZoneHeartRateLimits("2A8B", "Five Zone Heart Rate Limits"),
        Gender("2A8C", "Gender"),
        HeartRateMax("2A8D", "Heart Rate Max"),
        Height("2A8E", "Height"),
        HipCircumference("2A8F", "Hip Circumference"),
        LastName("2A90", "Last Name"),
        MaximumRecommendedHeartRate("2A91", "Maximum Recommended Heart Rate"),
        RestingHeartRate("2A92", "Resting Heart Rate"),
        SportTypeforAerobicandAnaerobicThresholds("2A93", "Sport Type for Aerobic and Anaerobic Thresholds"),
        ThreeZoneHeartRateLimits("2A94", "Three Zone Heart Rate Limits"),
        TwoZoneHeartRateLimit("2A95", "Two Zone Heart Rate Limit"),
        VO2Max("2A96", "VO2 Max"),
        WaistCircumference("2A97", "Waist Circumference"),
        Weight("2A98", "Weight"),
        DatabaseChangeIncrement("2A99", "Database Change Increment"),
        UserIndex("2A9A", "User Index"),
        BodyCompositionFeature("2A9B", "Body Composition Feature"),
        BodyCompositionMeasurement("2A9C", "Body Composition Measurement"),
        WeightMeasurement("2A9D", "Weight Measurement"),
        WeightScaleFeature("2A9E", "Weight Scale Feature"),
        UserControlPoint("2A9F", "User Control Point"),
        MagneticFluxDensity2D("2AA0", "Magnetic Flux Density - 2D"),
        MagneticFluxDensity3D("2AA1", "Magnetic Flux Density - 3D"),
        Language("2AA2", "Language"),
        BarometricPressureTrend("2AA3", "Barometric Pressure Trend"),
        BondManagementControlPoint("2AA4", "Bond Management Control Point"),
        BondManagementFeatures("2AA5", "Bond Management Features"),
        CentralAddressResolution("2AA6", "Central Address Resolution"),
        CGMMeasurement("2AA7", "CGM Measurement"),
        CGMFeature("2AA8", "CGM Feature"),
        CGMStatus("2AA9", "CGM Status"),
        CGMSessionStartTime("2AAA", "CGM Session Start Time"),
        CGMSessionRunTime("2AAB", "CGM Session Run Time"),
        CGMSpecificOpsControlPoint("2AAC", "CGM Specific Ops Control Point"),
        IndoorPositioningConfiguration("2AAD", "Indoor Positioning Configuration"),
        Latitude("2AAE", "Latitude"),
        Longitude("2AAF", "Longitude"),
        LocalNorthCoordinate("2AB0", "Local North Coordinate"),
        LocalEastCoordinate("2AB1", "Local East Coordinate"),
        FloorNumber("2AB2", "Floor Number"),
        Altitude("2AB3", "Altitude"),
        Uncertainty("2AB4", "Uncertainty"),
        LocationName("2AB5", "Location Name"),
        URI("2AB6", "URI"),
        HTTPHeaders("2AB7", "HTTP Headers"),
        HTTPStatusCode("2AB8", "HTTP Status Code"),
        HTTPEntityBody("2AB9", "HTTP Entity Body"),
        HTTPControlPoint("2ABA", "HTTP Control Point"),
        HTTPSSecurity("2ABB", "HTTPS Security"),
        TDSControlPoint("2ABC", "TDS Control Point"),
        OTSFeature("2ABD", "OTS Feature"),
        ObjectName("2ABE", "Object Name"),
        ObjectType("2ABF", "Object Type"),
        ObjectSize("2AC0", "Object Size"),
        ObjectFirstCreated("2AC1", "Object First-Created"),
        ObjectLastModified("2AC2", "Object Last-Modified"),
        ObjectID("2AC3", "Object ID"),
        ObjectProperties("2AC4", "Object Properties"),
        ObjectActionControlPoint("2AC5", "Object Action Control Point"),
        ObjectListControlPoint("2AC6", "Object List Control Point"),
        ObjectListFilter("2AC7", "Object List Filter"),
        ObjectChanged("2AC8", "Object Changed"),
        ResolvablePrivateAddressOnly("2AC9", "Resolvable Private Address Only"),
        FitnessMachineFeature("2ACC", "Fitness Machine Feature"),
        TreadmillData("2ACD", "Treadmill Data"),
        CrossTrainerData("2ACE", "Cross Trainer Data"),
        StepClimberData("2ACF", "Step Climber Data"),
        StairClimberData("2AD0", "Stair Climber Data"),
        RowerData("2AD1", "Rower Data"),
        IndoorBikeData("2AD2", "Indoor Bike Data"),
        TrainingStatus("2AD3", "Training Status"),
        SupportedSpeedRange("2AD4", "Supported Speed Range"),
        SupportedInclinationRange("2AD5", "Supported Inclination Range"),
        SupportedResistanceLevelRange("2AD6", "Supported Resistance Level Range"),
        SupportedHeartRateRange("2AD7", "Supported Heart Rate Range"),
        SupportedPowerRange("2AD8", "Supported Power Range"),
        FitnessMachineControlPoint("2AD9", "Fitness Machine Control Point"),
        FitnessMachineStatus("2ADA", "Fitness Machine Status"),

        OmronMeasurementBP("C195DA8A-0E23-4582-ACD8-D446C77C45DE", "Omron Measurement(BP)"),
        OmronMeasurementWS("8FF2DDFB-4A52-4CE5-85A4-D2F97917792A", "Omron Measurement(WS)"),;
        @NonNull
        private final String mUuidString;
        @NonNull
        private final String mDescription;

        Characteristic(@NonNull String uuidString, @NonNull String description) {
            mUuidString = uuidString;
            mDescription = description;
        }

        @NonNull
        public static Characteristic valueOf(@NonNull CBUUID uuid) {
            for (Characteristic type : values()) {
                if (type.uuid().equals(uuid)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown uuid. " + uuid.uuidString());
        }

        @NonNull
        public CBUUID uuid() {
            return CBUUID.fromString(mUuidString);
        }

        @NonNull
        public String description() {
            return mDescription;
        }
    }

    public enum Descriptor implements IUUIDDefines {
        CharacteristicExtendedProperties("2900", "Characteristic Extended Properties"),
        CharacteristicUserDescription("2901", "Characteristic User Description"),
        ClientCharacteristicConfiguration("2902", "Client Characteristic Configuration"),
        ServerCharacteristicConfiguration("2903", "Server Characteristic Configuration"),
        CharacteristicPresentationFormat("2904", "Characteristic Presentation Format"),
        CharacteristicAggregateFormat("2905", "Characteristic Aggregate Format"),
        ValidRange("2906", "Valid Range"),
        ExternalReportReference("2907", "External Report Reference"),
        ReportReference("2908", "Report Reference"),
        NumberofDigitals("2909", "Number of Digitals"),
        ValueTriggerSetting("290A", "Value Trigger Setting"),
        EnvironmentalSensingConfiguration("290B", "Environmental Sensing Configuration"),
        EnvironmentalSensingMeasurement("290C", "Environmental Sensing Measurement"),
        EnvironmentalSensingTriggerSetting("290D", "Environmental Sensing Trigger Setting"),
        TimeTriggerSetting("290E", "Time Trigger Setting"),;
        @NonNull
        private final String mUuidString;
        @NonNull
        private final String mDescription;

        Descriptor(@NonNull String uuidString, @NonNull String description) {
            mUuidString = uuidString;
            mDescription = description;
        }

        @NonNull
        public static Descriptor valueOf(@NonNull CBUUID uuid) {
            for (Descriptor type : values()) {
                if (type.uuid().equals(uuid)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown uuid. " + uuid.uuidString());
        }

        @NonNull
        public CBUUID uuid() {
            return CBUUID.fromString(mUuidString);
        }

        @NonNull
        public String description() {
            return mDescription;
        }
    }

    private interface IUUIDDefines {
        @NonNull
        CBUUID uuid();

        @NonNull
        String description();
    }
}
