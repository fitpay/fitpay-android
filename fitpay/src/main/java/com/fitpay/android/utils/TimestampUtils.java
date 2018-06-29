package com.fitpay.android.utils;

import android.support.annotation.Nullable;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Methods for dealing with timestamps
 */
public class TimestampUtils {

    /**
     * Return combined date and time string for specified date/time
     *
     * @param time time in milliseconds
     * @return String with format "yyyy-MM-dd"
     */
    public static String getReadableDate(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_SIMPLE, Locale.getDefault());
        return dateFormat.format(new Date(time));
    }

    /**
     * Return a date for specified ISO 8601 time
     *
     * @param time time in ISO 8601 format "yyyy-MM-dd'T'HH:mm:ss'Z'" or "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     * @return Date
     */
    public static Date getDateForISO8601String(String time) throws ParseException {
        return ISO8601Utils.parse(time, new ParsePosition(0));
    }

    /**
     * Return an ISO 8601 combined date and time string for specified date/time
     *
     * @param time time in milliseconds
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     */
    public static String getISO8601StringForTime(long time) {
        return ISO8601Utils.format(new Date(time), true);
    }

    /**
     * Return days between specified date and current time
     *
     * @param date specified date
     * @return days or null
     */
    public static Integer getDaysBetweenDates(@Nullable Date date){
        return date == null ? null : Days.daysBetween(new LocalDateTime(date), new LocalDateTime()).getDays();
    }
}
