package com.hw.szoftarch.worklogger.entities;

import android.support.annotation.VisibleForTesting;

public class Issue {
    private long id;
    private String name;
    private String description;
    private Project project;

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
                '}';
    }

    @VisibleForTesting
    public void setId(long id) {
        this.id = id;
    }

}
