package com.hw.szoftarch.worklogger;

import android.support.annotation.NonNull;
import android.widget.DatePicker;

import java.util.Date;
import java.util.Calendar;

public class Utils {

    public static Date getDateFromDatePicker(final @NonNull DatePicker datePicker) {
        final int day = datePicker.getDayOfMonth();
        final int month = datePicker.getMonth();
        final int year = datePicker.getYear();

        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    public static void updateDatePicker(final @NonNull DatePicker datePicker, final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DATE);

        datePicker.updateDate(year, month, day);
    }
}
