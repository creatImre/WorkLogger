package com.hw.szoftarch.worklogger.workinghour;

import com.hw.szoftarch.worklogger.entities.Issue;

public class IssueSpinnerItem {
    private Issue issue;

    public IssueSpinnerItem(Issue issue) {
        this.issue = issue;
    }

    Issue getIssue() {
        return issue;
    }

    @Override
    public String toString() {
        return issue.getName();
    }
}
