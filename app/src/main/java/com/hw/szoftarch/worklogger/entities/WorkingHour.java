package com.hw.szoftarch.worklogger.entities;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class WorkingHour {

    public static class WorkingHourComparator implements Comparator<WorkingHour> {
        private static WorkingHourComparator mInstance;

        private WorkingHourComparator() {
        }

        public static WorkingHourComparator getInstance() {
            if (mInstance == null) {
                mInstance = new WorkingHourComparator();
            }
            return mInstance;
        }

        @Override
        public int compare(WorkingHour workingHour1, WorkingHour workingHour2) {
            final Long startDate1 = workingHour1.getStarting();
            final Long startDate2 = workingHour2.getStarting();

            final int dateCompareResult = startDate1.compareTo(startDate2);
            if (dateCompareResult != 0) {
                return dateCompareResult;
            }

            final Long issueId1 = workingHour1.getIssue().getId();
            final Long issueId2 = workingHour2.getIssue().getId();
            final int issueCompareResult = issueId1.compareTo(issueId2);
            if (issueCompareResult != 0) {
                return issueCompareResult;
            }

            final Long duration1 = workingHour1.getDuration();
            final Long duration2 = workingHour2.getDuration();
            return duration1.compareTo(duration2);
        }
    }

    private long id;
    private long starting;
    private long duration;
    private User user;
    private Issue issue;

    public WorkingHour() {
        super();
    }

    public long getStarting() {
        return starting;
    }

    public void setStarting(long starting) {
        this.starting = starting;
    }

    public Date getStartingDate() {
        return new Date(starting);
    };

    public long getDuration() {
        return duration;
    }

    public String getFormattedDuration() {
        return duration + " hours";
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Issue getIssue() {
        return issue;
    }

    @NonNull
    public String getIssueName() {
        if (issue == null) {
            return "null";
        } return issue.getName();
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "WorkingHour{" +
                "id=" + id +
                ", starting=" + getStartingDate() +
                ", duration=" + duration +
                ", userId=" + (user == null ? "null" : user.getGoogleId()) +
                ", issueId=" + (issue == null ? "null" : issue.getId()) +
                '}';
    }

    public String getFormattedDate() {
        @SuppressLint("SimpleDateFormat")
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm\nyyyy-MM-dd");
        return dateFormat.format(new Date(getStarting()));
    }
}