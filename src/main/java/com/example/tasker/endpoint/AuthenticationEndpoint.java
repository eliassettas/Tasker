package com.example.tasker.endpoint;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.LoginRequest;
import com.example.tasker.model.dto.LoginResponse;
import com.example.tasker.service.AuthenticationService;

@RestController
public class AuthenticationEndpoint {

    private final AuthenticationService authenticationService;

    public AuthenticationEndpoint(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.login(loginRequest);
    }

    @PostMapping("/refreshToken")
    public LoginResponse refreshToken(@RequestBody LoginRequest loginRequest) throws CustomException {
        return authenticationService.refreshToken(loginRequest);
    }

    // This is not used because the token is deleted in the front-end
    @PostMapping("/logout")
    public void logout(@RequestBody LoginRequest loginRequest) throws CustomException {
        authenticationService.logout(loginRequest);
    }
}
