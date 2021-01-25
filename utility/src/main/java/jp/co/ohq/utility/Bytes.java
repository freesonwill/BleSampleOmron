package jp.co.ohq.utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Bytes {

    public static short parse2BytesAsShort(byte[] data, int offset, boolean littleEndian) {
        return parseBytesAsByteBuffer(data, offset, littleEndian, 2).getShort();
    }

    public static int parse2BytesAsInt(byte[] data, int offset, boolean littleEndian) {
        return (int) parseBytesAsByteBuffer(data, offset, littleEndian, 2).getShort();
    }

    public static float parse2BytesAsFloat(byte[] data, int offset, boolean littleEndian) {
        return (float) parseBytesAsByteBuffer(data, offset, littleEndian, 2).getShort();
    }

    public static SFloat parse2BytesAsSFloat(byte[] data, int offset, boolean littleEndian) {
        return SFloat.valueOf(parseBytesAsByteBuffer(data, offset, littleEndian, 2).getShort());
    }

    public static int parse4BytesAsInt(byte[] data, int offset, boolean littleEndian) {
        return parseBytesAsByteBuffer(data, offset, littleEndian, 4).getInt();
    }

    public static long parse4BytesAsLong(byte[] data, int offset, boolean littleEndian) {
        return (long) parseBytesAsByteBuffer(data, offset, littleEndian, 4).getInt();
    }

    private static ByteBuffer parseBytesAsByteBuffer(byte[] data, int offset, boolean littleEndian, int numberOfByte) {
        byte[] buf = new byte[numberOfByte];
        System.arraycopy(data, offset, buf, 0, numberOfByte);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        if (littleEndian) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        return byteBuffer;
    }

    public static String parse7BytesAsDateString(byte[] data, int offset, boolean littleEndian) {
        byte[] buf = new byte[2];
        System.arraycopy(data, offset, buf, 0, 2);
        offset += 2;
        int year = Bytes.parse2BytesAsInt(buf, 0, littleEndian);
        int month = data[offset++];
        int day = data[offset++];
        int hour = data[offset++];
        int minutes = data[offset++];
        int seconds = data[offset];
        return String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minutes, seconds);
    }

    public static Date parse7BytesAsDate(byte[] data, int offset, boolean littleEndian) {
        byte[] buf = new byte[2];
        System.arraycopy(data, offset, buf, 0, 2);
        offset += 2;
        int year = Bytes.parse2BytesAsInt(buf, 0, littleEndian);
        int month = data[offset++];
        int day = data[offset++];
        int hour = data[offset++];
        int minutes = data[offset++];
        int seconds = data[offset];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Date date = null;
        try {
            date = sdf.parse(String.format(Locale.US, "%04d%02d%02d%02d%02d%02d", year, month, day, hour, minutes, seconds));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String toHexString(byte[] data) {
        if (null == data) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }
}
