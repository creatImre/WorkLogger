package com.hw.szoftarch.worklogger.report;

import android.support.annotation.Nullable;

import com.hw.szoftarch.worklogger.entities.User;

public class UserSpinnerItem {
    @Nullable
    private User user;

    public UserSpinnerItem(@Nullable User user) {
        this.user = user;
    }

    @Nullable
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        if (user == null) {
            return "-All-";
        }
        return user.getName();
    }
}
