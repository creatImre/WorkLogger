package com.hw.szoftarch.worklogger.workinghour;

import android.support.annotation.Nullable;

import com.hw.szoftarch.worklogger.entities.Issue;

public class IssueSpinnerItem {
    @Nullable
    private Issue issue;

    public IssueSpinnerItem(@Nullable Issue issue) {
        this.issue = issue;
    }

    @Nullable
    public Issue getIssue() {
        return issue;
    }

    @Override
    public String toString() {
        if (issue == null) {
            return "-Not selected-";
        }
        return issue.getName();
    }
}
