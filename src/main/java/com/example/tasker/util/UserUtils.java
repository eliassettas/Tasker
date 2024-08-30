package com.example.tasker.util;

public class UserUtils {

    public static String constructName(String firstName, String lastName) {
        return (firstName != null ? firstName : "") + (lastName != null ? (" " + lastName) : "");
    }
}
