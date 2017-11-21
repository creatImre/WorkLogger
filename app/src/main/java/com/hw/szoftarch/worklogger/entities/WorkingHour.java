package com.hw.szoftarch.worklogger.entities;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WorkingHour extends RealmObject {

    @PrimaryKey
    private long id;

    private Date starting;
    private long duration;
    private User user;
    private Issue issue;

    public WorkingHour() {
        super();
    }

    public Date getStarting() {
        return starting;
    }

    public void setStarting(Date starting) {
        this.starting = starting;
    }

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

}