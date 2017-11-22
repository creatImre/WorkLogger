package com.hw.szoftarch.worklogger.entities;

import android.support.annotation.NonNull;

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

    @Override
    public String toString() {
        return "Issue{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", project=" + project +
                ", workingHoursIds=" + getWorkingHourIds() +
                '}';
    }

    @NonNull
    private String getWorkingHourIds() {
        StringBuilder result = new StringBuilder("[");
        for(WorkingHour workingHour : workingHours) {
            if (result.toString().equals("[")) {
                result.append(" ").append(workingHour.getId());
            } else {
                result.append(", ").append(workingHour.getId());
            }
        }
        result.append("]");
        return result.toString();
    }
}
