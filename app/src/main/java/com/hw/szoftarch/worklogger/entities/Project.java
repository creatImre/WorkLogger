package com.hw.szoftarch.worklogger.entities;

import android.support.annotation.VisibleForTesting;

public class Project {
    private long id;
    private String name;
    private String description;

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

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @VisibleForTesting
    public void setId(int id) {
        this.id = id;
    }
}
