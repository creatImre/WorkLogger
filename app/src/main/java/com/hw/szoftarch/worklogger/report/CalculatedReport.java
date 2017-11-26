package com.hw.szoftarch.worklogger.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hw.szoftarch.worklogger.Utils;
import com.hw.szoftarch.worklogger.entities.Report;
import com.hw.szoftarch.worklogger.entities.ReportType;
import com.hw.szoftarch.worklogger.entities.User;

import org.joda.time.LocalDate;

public class CalculatedReport {
    private static final String EVERYONE = "Everyone";
    @NonNull
    private Report report;
    private User subjectUser = null;
    private long workedHours = -1L;
    private boolean calculated = false;

    CalculatedReport(final @NonNull Report report, final @Nullable User subjectUser) {
        this.report = report;
        this.subjectUser = subjectUser;
    }

    long getId() {
        return report.getId();
    }

    void setWorkedHours(long workedHours) {
        this.workedHours = workedHours;
        calculated = true;
    }

    String getSubjectName() {
        if (subjectUser == null) {
            return EVERYONE;
        }
        return subjectUser.getName();
    }

    String getStartDateText() {
        return Utils.getDateText(new LocalDate(report.getStartDate()));
    }

    String getIntervalText() {
        return ReportType.valueOf(report.getReportType()).getShownName();
    }

    String getWorkedHoursText() {
        if (!calculated) {
            return "Not calculated yet.";
        }
        return Utils.getShowedElapsedTime(workedHours);
    }

    @NonNull
    public Report getReport() {
        return report;
    }

    void invalidate() {
        workedHours = -1L;
        calculated = false;
    }
}
