package jp.co.ohq.utility;

public class SFloat {

    public static short NaN = 0x07FF;
    public static short NRes = 0x0800;
    public static short INFINITY_POS = 0x07FE;
    public static short INFINITY_NEG = 0x0802;
    public static short Reserved = 0x0801;
    private short value;

    public SFloat(short value) {
        this.value = value;
    }

    public static SFloat valueOf(short value) {
        return new SFloat(value);
    }

    public float floatValue() {
        if (value == NaN) {
            return Float.NaN;
        } else if (value == NRes) {
            return Float.NaN;
        } else if (value == INFINITY_POS) {
            return Float.POSITIVE_INFINITY;
        } else if (value == INFINITY_NEG) {
            return Float.NEGATIVE_INFINITY;
        } else if (value == Reserved) {
            return Float.NaN;
        } else {
            return (float) ((double) getMantissa(value) * Math.pow(10, getExponent(value)));
        }
    }

    public double doubleValue() {
        if (value == NaN) {
            return Double.NaN;
        } else if (value == NRes) {
            return Double.NaN;
        } else if (value == INFINITY_POS) {
            return Double.POSITIVE_INFINITY;
        } else if (value == INFINITY_NEG) {
            return Double.NEGATIVE_INFINITY;
        } else if (value == Reserved) {
            return Double.NaN;
        } else {
            return ((double) getMantissa(value)) * Math.pow(10, getExponent(value));
        }
    }

    private short getExponent(short value) {
        if (value < 0) {
            return (byte) (((value >> 12) & 0x0F) | 0xF0);
        }
        return (short) ((value >> 12) & 0x0F);
    }

    private short getMantissa(short value) {
        if ((value & 0x0800) != 0) {
            return (short) ((value & 0x0FFF) | 0xF000);
        }
        return (short) (value & 0x0FFF);
    }
}
