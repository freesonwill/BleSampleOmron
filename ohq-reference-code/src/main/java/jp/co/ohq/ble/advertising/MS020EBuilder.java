package jp.co.ohq.ble.advertising;


import com.neovisionaries.bluetooth.ble.advertising.MSBuilder;

/**
 * Builder for Omron Healthcare Co., LTD (Company ID = 0x020E)
 */
class MS020EBuilder extends MSBuilder {
    public MS020EBuilder() {
        super(new EachUserDataBuilder());
    }
}
