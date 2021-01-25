package jp.co.ohq.ble.enumerate;

public enum OHQSessionOptionKey {

    /**
     * Read Measurement Records (Type of value : Boolean)
     * <p>
     * A Boolean value that specifies whether reading records of measurement.
     * The value for this key is an NSNumber object. If the key is not specified, the default value is NO.
     */
    ReadMeasurementRecordsKey,

    /**
     * Allow Control of Reading Position to Measurement Records (Type of value : Boolean)
     * <p>
     * A boolean value that specifies whether to control the reading position of the measurement record.
     * If you specify YES, the reading position of the measurement record depends on the value specified by SequenceNumberOfFirstRecordToReadKey.
     * If SequenceNumberOfFirstRecordToReadKey is not specified, all records are read.
     * It works only on devices that support the Omron Extension protocol.
     * The value for this key is an NSNumber object. If the key is not specified, the default value is NO.
     */
    AllowControlOfReadingPositionToMeasurementRecordsKey,

    /**
     * Sequence Number of First Record to Read (Type of value : Integer[0 - 65535])
     * <p>
     * A sequence number that specifies the reading start position for measurement record.
     * It works only on devices that support the Omron Extension protocol.
     */
    SequenceNumberOfFirstRecordToReadKey,

    /**
     * Allow Access to Beacon Identifier (Type of value : Boolean)
     * <p>
     * A Boolean value that specifies whether reading beacon identifier.
     * It works only on devices that support the Omron Extension protocol.
     * The value for this key is an NSNumber object. If the key is not specified, the default value is NO.
     */
    AllowAccessToBeaconIdentifierKey,

    /**
     * Allow Access to Omron Extended Measurement Records (Type of value : Boolean)
     * <p>
     * A Boolean value that specifies whether reading omron extended measurement records instead of bluetooth standard measurement record.
     * It works only on devices that support the Omron Extension protocol.
     * The value for this key is an NSNumber object. If the key is not specified, the default value is NO.
     */
    AllowAccessToOmronExtendedMeasurementRecordsKey,

    /**
     * Register New User (Type of value : Boolean)
     * <p>
     * A Boolean value that specifies whether register new user to device.
     * If the user index is not specified with UserIndexKey, it is assigned to an unregistered user index, and if it is specified, it is assigned to the specified User Index.
     * You can specify the User Index only on devices that support Omron Extension Protocol.
     * If registration fails, the session will fail with the reason of FailedToRegisterUser.
     * It works only on devices that manage users.
     * The value for this key is an NSNumber object. If the key is not specified, the default value is NO.
     */
    RegisterNewUserKey,

    /**
     * Delete User Data (Type of value : Boolean)
     * <p>
     * A Boolean value that specifies whether delete user data from device.
     * If you specify YES, the user information registered in the User Index specified by UserIndexKey is deleted.
     * If deletion fails, the session will fail with the reason of FailedToDeleteUser.
     * It works only with devices that manage users.
     * The value for this key is an NSNumber object. If the key is not specified, the default value is NO.
     */
    DeleteUserDataKey,

    /**
     * Consent Code (Type of value : Integer[0x0000 - 0x270F])
     * <p>
     * Consent code used for user authentication and user registration.
     * This only works if UserIndexKey is specified.
     * The value for this key is an NSNumber object. If the key is not specified, the default value is OHQDefaultConsentCode(0x020E).
     */
    ConsentCodeKey,

    /**
     * User Index (Type of value : Integer[1 - 4])
     * <p>
     * User index used for user authentication and user registration.
     * It works only with devices that manage users.
     */
    UserIndexKey,

    /**
     * User Data (Type of value : Map<OHQUserDataKey, Object>)
     */
    UserDataKey,

    /**
     * User Data Update Flag (Type of value : Boolean)
     */
    UserDataUpdateFlagKey,

    /**
     * Database Change Increment Value (Type of value : Long[0 - 4294967295])
     */
    DatabaseChangeIncrementValueKey,

    /**
     * Connection Wait Time (Type of value : Long[0 - 4294967295])
     */
    ConnectionWaitTimeKey,
}
