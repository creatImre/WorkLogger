package com.hw.szoftarch.worklogger.entities;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    private String googleId;
    private String name;
    private int levelId;
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

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public UserLevel getLevel() {
        return UserLevel.fromId(levelId);
    }

    public void setLevel(UserLevel level) {
        this.levelId = level.getId();
    }

    public RealmList<WorkingHour> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(RealmList<WorkingHour> workingHours) {
        this.workingHours = workingHours;
    }

    public void update(User user, Realm realm) {
        realm.beginTransaction();
        setGoogleId(user.getGoogleId());
        workingHours.clear();
        workingHours.addAll(user.getWorkingHours());
        setName(user.getName());
        setLevelId(user.getLevelId());
        realm.commitTransaction();
    }
}