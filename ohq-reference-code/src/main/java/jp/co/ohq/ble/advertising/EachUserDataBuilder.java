package jp.co.ohq.ble.advertising;


import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecific;
import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecificBuilder;

/**
 * Builder for {@link EachUserData}.
 */
class EachUserDataBuilder implements ADManufacturerSpecificBuilder {
    @Override
    public ADManufacturerSpecific build(int length, int type, byte[] data, int companyId) {
        return EachUserData.create(length, type, data, companyId);
    }
}
