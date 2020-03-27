package com.fondova.finance.ui.util;

import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

public class DateFormatUtil {
    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------
    public static final String TAG = DateFormatUtil.class.getSimpleName();
    private static final DateTimeFormatter uiFormat = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss z");

    private static final DateTimeFormatter DTNDateFormat23 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final DateTimeFormatter DTNDateFormat19 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DTNDateFormat16 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DTNDateFormat10GMT = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));
    private static final DateTimeFormatter DTNDateFormat10 = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DTNShortDate = DateTimeFormat.forPattern("M/d");
    private static final DateTimeFormatter DTNShortDateGMT = DateTimeFormat.forPattern("M/d").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));
    private static final DateTimeFormatter DTNShortHour = DateTimeFormat.forPattern("h a");
    private static final DateTimeFormatter DTNDate = DateTimeFormat.forPattern("M/d/yy");
    private static final DateTimeFormatter DTNChartLabelDateTime = DateTimeFormat.forPattern("M/d/yy h:mma zzz");
    private static final DateTimeFormatter DTNNewsHeadlineTimestampDateTime = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss z");
    private static final DateTimeFormatter DTNDateTime = DateTimeFormat.forPattern("M/d/yy h:mm:ss a zzz");
    private static final DateTimeFormatter HourMinuteSecondAMPM = DateTimeFormat.forPattern("h:mm:ssa");
    private static final DateTimeFormatter DateHourMinuteSecondAMPM = DateTimeFormat.forPattern("yyyy-MM-dd h:mm:ssa zzz");


    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    public static String serverDateStringToUiString(String dateTime) {
        DateTimeFormatter formatter = getFormatterForDate(dateTime);
        if (formatter == null) return dateTime;

        try {
            DateTime date = new DateTime(dateTime, DateTimeZone.UTC).withZone(DateTimeZone.getDefault());
            return uiFormat.print(date);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Cannot parse date " + dateTime);
        }

        return dateTime;
    }


    public static DateTime serverDateStringToDateTime(String dateString) {
        DateTimeFormatter formatter = getFormatterForDate(dateString);
        DateTime dateTime;
        if (formatter == null) {
            dateTime = null;
        } else {
            dateTime = formatter.parseDateTime(dateString);
        }
        return dateTime;
    }

    public static String serverDateStringToUiShortDateString(String dateServerString) {
        DateTime parsedDateTime = serverDateStringToDateTime(dateServerString);
        return parsedDateTime == null ? null : DTNShortDate.print(parsedDateTime);
    }

    public static String dateTimeToShortDateString(DateTime dateTime) {
        return DTNShortDate.print(dateTime);
    }

    public static String dateTimeToShortDateStringGMT(DateTime dateTime) {
        return DTNDateFormat10GMT.print(dateTime);
    }

    public static String dateTimeToShortDate10String(DateTime dateTime) {
        return DTNDateFormat10.print(dateTime);
    }

    public static String dateTimeToServerString16(DateTime dateTime) {
        return DTNDateFormat16.print(dateTime);
    }

    public static String dateTimeToLastUpdatedString(DateTime dateTime) {
        return HourMinuteSecondAMPM.print(dateTime);
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    private static DateTimeFormatter getFormatterForDate(String date) {
        if (TextUtils.isEmpty(date)) return null;

        switch (date.length()) {
            case 10:
                return DTNDateFormat10;
            case 16:
                return DTNDateFormat16;
            case 19:
                return DTNDateFormat19;
            case 23:
                return DTNDateFormat23;
            default:
                return null;
        }
    }

}
