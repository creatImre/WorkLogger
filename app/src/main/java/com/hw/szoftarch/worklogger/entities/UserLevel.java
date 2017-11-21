package com.hw.szoftarch.worklogger.entities;

public enum UserLevel {
    EMPLOYEE(0), PROJECT_LEADER(1), ADMIN(2);

    private int id;

    UserLevel(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static UserLevel fromId(final int id) {
        for (UserLevel level : values()) {
            if (level.id == id) {
                return level;
            }
        }
        throw new IllegalArgumentException("this id(" + id + ") is invalid");
    }
}
