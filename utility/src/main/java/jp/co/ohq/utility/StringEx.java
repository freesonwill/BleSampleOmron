package jp.co.ohq.utility;

import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@SuppressWarnings("unused")
public class StringEx {

    private StringEx() {
        // must be not create instance.
    }

    public static String toNumberString(BigDecimal value) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        return nf.format(value.longValue());
    }

    public static String toDecimalString(BigDecimal value, int minFractionDigits, int maxFractionDigits) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        nf.setMinimumFractionDigits(minFractionDigits);
        nf.setMaximumFractionDigits(maxFractionDigits);
        return nf.format(value.doubleValue());
    }

    public static String toPercentString(BigDecimal value, int minFractionDigits, int maxFractionDigits) {
        NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
        nf.setMinimumFractionDigits(minFractionDigits);
        nf.setMaximumFractionDigits(maxFractionDigits);
        return nf.format(value.doubleValue());
    }

    public static String toDateString(long timeInMillis, @NonNull FormatType format) {
        return DateFormat.format(format.format, timeInMillis).toString();
    }

    public enum FormatType {
        Form1("yyyy-MM-dd kk:mm:ss"),
        Form2("yyyy-MM-dd"),
        Form3("yyyyMMddkkmmss"),;
        final String format;

        FormatType(String format) {
            this.format = format;
        }
    }
}
