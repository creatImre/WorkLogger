package com.hw.szoftarch.worklogger;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static Date getDateFromPickers(final @NonNull DatePicker datePicker, final @NonNull TimePicker timePicker) {
        final int day = datePicker.getDayOfMonth();
        final int month = datePicker.getMonth();
        final int year = datePicker.getYear();
        final int hour;
        final int minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);

        return calendar.getTime();
    }

    public static DateTime getDateFromDatePicker(final @NonNull DatePicker datePicker) {
        final int day = datePicker.getDayOfMonth();
        final int month = datePicker.getMonth();
        final int year = datePicker.getYear();

        return new DateTime(year, month, day,0,0);
    }

    public static void updatePickers(final @NonNull DatePicker datePicker, final @NonNull TimePicker timePicker, final @NonNull Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DATE);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        datePicker.updateDate(year, month, day);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        } else {
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);
        }
    }

    public static long getElapsedTimeUntilNow(final long fromTime) {
        return getElapsedTimeBetween(fromTime, new DateTime().getMillis());
    }

    private static long getElapsedTimeBetween(final long fromTime, final long toTime) {
        final DateTime startDateTime = new DateTime(fromTime);
        final DateTime nowDateTime = new DateTime(toTime);

        final Duration duration = new Duration(startDateTime, nowDateTime);
        return duration.getStandardSeconds();
    }

    public static String getShowedElapsedTime(final long elapsedSeconds) {
        final long hours = TimeUnit.SECONDS.toHours(elapsedSeconds);
        final long minutes = TimeUnit.SECONDS.toMinutes(elapsedSeconds) -  TimeUnit.HOURS.toMinutes(hours);
        final long seconds = elapsedSeconds - TimeUnit.MINUTES.toSeconds(minutes) -  TimeUnit.HOURS.toSeconds(hours);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long getSeconds(final int hours, final int minutes, final int seconds) {
        return seconds + minutes * 60 + hours * 3600;
    }

    public static long getHoursRemainder(final long seconds) {
        return TimeUnit.SECONDS.toHours(seconds);
    }

    public static long getMinutesRemainder(final long seconds) {
        final long hours = TimeUnit.SECONDS.toHours(seconds);
        return TimeUnit.SECONDS.toMinutes(seconds) -  TimeUnit.HOURS.toMinutes(hours);
    }

    public static String getDateText(final LocalDate date) {
        final int year = date.getYear();
        final int month = date.getMonthOfYear();
        final int day = date.getDayOfMonth();
        return String.format(Locale.getDefault(), "%04d.%02d.%02d", year, month, day);
    }
}
