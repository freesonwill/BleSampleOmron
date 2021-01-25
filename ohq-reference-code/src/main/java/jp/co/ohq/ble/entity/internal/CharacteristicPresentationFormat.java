package jp.co.ohq.ble.entity.internal;

import java.util.Locale;

import jp.co.ohq.utility.Bytes;

public class CharacteristicPresentationFormat {
    public final int format;
    public final int exponent;
    public final int unitUUIDValue;
    public final int namespace;
    public final int description;

    public CharacteristicPresentationFormat(byte[] data) {
        format = data[0];
        exponent = data[1];
        unitUUIDValue = Bytes.parse2BytesAsInt(data, 2, true);
        namespace = data[4];
        description = Bytes.parse2BytesAsInt(data, 5, true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n===== ");
        sb.append(getClass().getSimpleName());
        sb.append(" =====\n");

        sb.append(String.format(Locale.US, "format:0x%02x, ", format));
        sb.append(String.format(Locale.US, "exponent:%d, ", exponent));
        sb.append(String.format(Locale.US, "unitUUIDValue:0x%04x, ", unitUUIDValue));
        sb.append(String.format(Locale.US, "namespace:0x%02x, ", namespace));
        sb.append(String.format(Locale.US, "description:0x%04x, ", description));

        return sb.toString();
    }
}
