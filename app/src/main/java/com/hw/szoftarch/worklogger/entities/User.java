package com.hw.szoftarch.worklogger.entities;

public class User {
    private String googleId;
    private String name;
    private String level;

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

    @Override
    public String toString() {
        return "User{" +
                "googleId='" + googleId + '\'' +
                ", name='" + name + '\'' +
                ", level='" + level + '\'' +
                '}';
    }

}