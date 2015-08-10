package com.halosolutions.itranslator.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by luhonghai on 25/02/2015.
 */
public class DateHelper {
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_HOUR_MINUTE_FORMAT = "HH:mm";

    public static Date convertStringToDate(String date) {
        try {
            if (date == null || date.length() == 0) return null;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.US);
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String convertDateToString(Date date) {
        return convertDateToString(date, DEFAULT_DATE_FORMAT);
    }

    public static String convertDateToString(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        return simpleDateFormat.format(date);
    }

    public static String convertDateToHourMinuteString(Date date) {
        if (date == null) return "--:--";
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_HOUR_MINUTE_FORMAT, Locale.US);
        return sdf.format(date);
    }

    public static String convertDateToHourMinuteString(Date dateStart, Date dateEnd) {
        return convertDateToHourMinuteString(dateStart) + " - " + convertDateToHourMinuteString(dateEnd);
    }

    public static String[] getTimeList(int startHour, int endHour) {
        if (startHour < 0) startHour = 0;
        if (endHour < startHour) return new String[] {toTimeNumberString(endHour)};
        String[] items = new String[endHour - startHour + 1];
        for (int i = 0; i < (endHour - startHour + 1); i++) {
            items[i] = toTimeNumberString(startHour + i);
        }
        return items;
    }

    public static String toTimeNumberString(int t) {
        if (t > 9) return Integer.toString(t);
        return "0" + Integer.toString(t);
    }

}
