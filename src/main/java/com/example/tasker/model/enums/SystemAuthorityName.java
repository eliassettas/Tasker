package com.example.tasker.model.enums;

public enum SystemAuthorityName {
    CREATE_TASK("CREATE_TASK"),
    UPDATE_TASK("UPDATE_TASK"),
    DELETE_TASK("DELETE_TASK"),
    CREATE_COMMENT("CREATE_COMMENT"),
    UPDATE_COMMENT("UPDATE_COMMENT"),
    DELETE_COMMENT("DELETE_COMMENT"),
    CREATE_TEAM("CREATE_TEAM"),
    UPDATE_TEAM("UPDATE_TEAM"),
    DELETE_TEAM("DELETE_TEAM"),
    CREATE_TEAM_RELATIONSHIP("CREATE_TEAM_RELATIONSHIP"),
    DELETE_TEAM_RELATIONSHIP("DELETE_TEAM_RELATIONSHIP");

    private final String value;

    SystemAuthorityName(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
