package jp.co.ohq.blesampleomron.controller.util;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    /**
     * e.g. dateString = 2000-04-04
     * cal.get(Calendar.YEAR) == 2000
     * cal.get(Calendar.MONTH) == 3
     * cal.get(Calendar.DAY_OF_MONTH) == 4
     */
    public static Calendar toDate(String dateString, @NonNull FormatType format) {
        Calendar cal = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format.format, Locale.US);
        try {
            Date date = sdf.parse(dateString);
            cal = Calendar.getInstance();
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public enum FormatType {
        Form1("yyyy-MM-dd kk:mm:ss"),
        Form2("yyyy-MM-dd"),
        Form3("yyyyMMddkkmmss"),;
        String format;

        FormatType(String format) {
            this.format = format;
        }
    }
}
