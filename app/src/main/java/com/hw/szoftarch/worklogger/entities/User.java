package com.hw.szoftarch.worklogger.entities;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    private String googleId;
    private String name;
    private String level;
    private RealmList<WorkingHour> workingHours;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public UserLevel getUserLevel() {
        return UserLevel.valueOf(level);
    }

    public void setUserLevel(UserLevel level) {
        this.level = level.toString();
    }

    public RealmList<WorkingHour> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(RealmList<WorkingHour> workingHours) {
        this.workingHours = workingHours;
    }

    public void update(User user, Realm realm) {
        realm.beginTransaction();
        workingHours.clear();
        workingHours.addAll(user.getWorkingHours());
        setName(user.getName());
        setLevel(user.getLevel());
        realm.commitTransaction();
    }

    public void updateLevel(UserLevel newLevel, Realm realm) {
        realm.beginTransaction();
        setLevel(newLevel.toString());
        realm.commitTransaction();
    }

    @Override
    public String toString() {
        return "User{" +
                "googleId='" + googleId + '\'' +
                ", name='" + name + '\'' +
                ", level='" + level + '\'' +
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