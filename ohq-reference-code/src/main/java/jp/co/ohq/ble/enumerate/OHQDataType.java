package jp.co.ohq.ble.enumerate;

public enum OHQDataType {
    /**
     * < Current Time (Type of Data : String)
     */
    CurrentTime,
    /**
     * < Battery Level (Type of Data : Integer)
     */
    BatteryLevel,
    /**
     * < Model Name (Type of Data : String)
     */
    ModelName,
    /**
     * < Device Category (Type of Data : OHQDeviceCategory)
     */
    DeviceCategory,
    /**
     * < Registered User Data Index (Type of Data : Integer)
     */
    RegisteredUserIndex,
    /**
     * < Authenticated User Data Index (Type of Data : Integer)
     */
    AuthenticatedUserIndex,
    /**
     * < Deleted User Data Index (Type of Data : Integer)
     */
    DeletedUserIndex,
    /**
     * < User Data (Type of Data : Map<OHQUserDataKey, Object>)
     */
    UserData,
    /**
     * < Database Change Increment (Type of Data : Long)
     */
    DatabaseChangeIncrement,
    /**
     * < Sequence Number (Type of Data : Integer)
     */
    SequenceNumberOfLatestRecord,
    /**
     * < Measurement Records (Type of Data : LinkedList<Map<OHQMeasurementRecordKey, Object>>>)
     */
    MeasurementRecords,
}
