package com.hw.szoftarch.worklogger.entities;

public enum ReportType {
    DAILY("Day", 1),
    WEEKLY("Week", 7),
    MONTHLY("Month", 30);

    private String shownName;
    private int lengthDays;

    ReportType(String shownName, int lengthDays) {
        this.shownName = shownName;
        this.lengthDays = lengthDays;
    }

    public String getShownName() {
        return shownName;
    }

    public int getLengthDays() {
        return lengthDays;
    }
}
