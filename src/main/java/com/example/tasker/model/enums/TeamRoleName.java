package com.example.tasker.model.enums;

public enum TeamRoleName {
    LEADER("Leader"),
    MEMBER("Member");

    private final String value;

    TeamRoleName(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
