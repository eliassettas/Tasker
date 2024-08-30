package com.example.tasker.util;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.tasker.exception.CustomException;

public class SecurityUtils {

    private static final String USER_ID_CLAIM = "userId";

    public static String getUsername() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    public static void validateUser(Integer userId) throws CustomException {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer connectedUserId = Integer.parseInt(jwt.getClaimAsString(USER_ID_CLAIM));
        if (!connectedUserId.equals(userId)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Action cannot be performed by specific user");
        }
    }
}
