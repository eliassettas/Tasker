package com.example.tasker.model.enums;

public enum TaskStatusName {
    NEW("TASK_STATUS_NEW"),
    ASSIGNED("TASK_STATUS_ASSIGNED"),
    BLOCKED("TASK_STATUS_BLOCKED"),
    RESOLVED("TASK_STATUS_RESOLVED"),
    VERIFIED("TASK_STATUS_VERIFIED"),
    CLOSED("TASK_STATUS_CLOSED");

    private final String value;

    TaskStatusName(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
