package jp.co.ohq.ble.advertising;

import android.support.annotation.NonNull;

import com.neovisionaries.bluetooth.ble.advertising.ADStructure;

import java.util.List;

public class ADPayloadParser {

    @NonNull
    private static final ADPayloadParser sInstance = new ADPayloadParser();
    @NonNull
    private final com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser mParser;

    private ADPayloadParser() {
        mParser = com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser.getInstance();
        mParser.registerManufacturerSpecificBuilder(0x020E, new MS020EBuilder());
    }

    public static ADPayloadParser getInstance() {
        return sInstance;
    }

    public List<ADStructure> parse(byte[] payload) {
        return mParser.parse(payload);
    }

    public List<ADStructure> parse(byte[] payload, int offset, int length) {
        return mParser.parse(payload, offset, length);
    }
}
