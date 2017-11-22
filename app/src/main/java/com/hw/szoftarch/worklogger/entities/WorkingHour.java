package com.hw.szoftarch.worklogger.entities;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WorkingHour extends RealmObject {

    @PrimaryKey
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
}