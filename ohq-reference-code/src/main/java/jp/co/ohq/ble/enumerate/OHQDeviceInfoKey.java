package jp.co.ohq.ble.enumerate;

public enum OHQDeviceInfoKey {
    /**
     * Device Address (Type of value : String)
     */
    AddressKey,
    /**
     * Advertisement Data (Type of value : List<ADStructure>)
     */
    AdvertisementDataKey,
    /**
     * RSSI (Type of value : Integer, unit is in "dBm")
     */
    RSSIKey,
    /**
     * Model Name (Type of value : String)
     */
    ModelNameKey,
    /**
     * Device Category (Type of value : OHQDeviceCategory)
     */
    CategoryKey,
    /**
     * Local Name (Type of value : String)
     */
    LocalNameKey,
}
