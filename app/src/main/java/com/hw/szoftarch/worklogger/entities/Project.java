package com.hw.szoftarch.worklogger.entities;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Project  extends RealmObject {

    @PrimaryKey
    private long id;

    private String name;
    private String description;

    private RealmList<Issue> issues;

    public Project() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(RealmList<Issue> issues) {
        this.issues = issues;
    }


}
