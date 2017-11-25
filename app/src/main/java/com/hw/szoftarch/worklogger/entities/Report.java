package com.hw.szoftarch.worklogger.entities;

public class Report {
    public static final String ALL = "allUser";

    private long id;
    private User owner;
    private String googleId;
    private String reportType;
    private long startDate;

    public long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(final User owner) {
        this.owner = owner;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(final String googleId) {
        this.googleId = googleId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(final String reportType) {
        this.reportType = reportType;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(final long startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", owner=" + owner +
                ", googleId='" + googleId + '\'' +
                ", reportType='" + reportType + '\'' +
                ", startDate=" + startDate +
                '}';
    }
}
