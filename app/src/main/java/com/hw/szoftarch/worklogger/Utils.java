package com.hw.szoftarch.worklogger;

import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Calendar;
import java.util.Date;

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

    public static void updatePickers(final @NonNull DatePicker datePicker, final @NonNull TimePicker timePicker, final @NonNull Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DATE);
        final int hour = calendar.get(Calendar.HOUR);
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

    public static long getElapsedTimeBetween(final long fromTime, final long toTime) {
        final DateTime startDateTime = new DateTime(fromTime);
        final DateTime nowDateTime = new DateTime(toTime);

        final Duration duration = new Duration(startDateTime, nowDateTime);
        //TODO when using minutes, use this: final long elapsedMinutes = duration.getStandardMinutes();
        return duration.getStandardMinutes();
    }
}
