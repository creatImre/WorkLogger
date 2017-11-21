package com.hw.szoftarch.worklogger.entities;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Issue extends RealmObject {
    @PrimaryKey
    private long id;

    private String name;
    private String description;

    private Project project;

    private RealmList<WorkingHour> workingHours;

    public Issue() {
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public long getId() {
        return id;
    }

}
