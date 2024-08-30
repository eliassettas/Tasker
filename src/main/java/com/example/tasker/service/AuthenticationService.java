package com.example.tasker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.LoginRequest;
import com.example.tasker.model.dto.LoginResponse;
import com.example.tasker.model.persistence.RefreshToken;
import com.example.tasker.model.persistence.SystemUser;

@Service
public class AuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtProviderService jwtProviderService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtProviderService jwtProviderService,
            RefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtProviderService = jwtProviderService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        LOGGER.info("Received login request for user: {}", loginRequest.getUsername());
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()
        );
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SystemUser systemUser = (SystemUser) authentication.getPrincipal();
        Jwt jwt = jwtProviderService.generateToken(systemUser);
        String refreshToken = refreshTokenService.generateRefreshToken(authentication);
        return new LoginResponse(systemUser.getId(), jwt.getTokenValue(), refreshToken, jwt.getExpiresAt());
    }

    @Transactional(rollbackFor = CustomException.class)
    public LoginResponse refreshToken(LoginRequest loginRequest) throws CustomException {
        if (loginRequest.getRefreshToken() == null || loginRequest.getRefreshToken().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Refresh token cannot be empty");
        }
        RefreshToken refreshToken = refreshTokenService.updateRefreshToken(loginRequest.getRefreshToken());
        Jwt jwt = jwtProviderService.generateToken(refreshToken.getSystemUser());
        return new LoginResponse(jwt.getTokenValue(), refreshToken.getToken(), jwt.getExpiresAt());
    }

    @Transactional(rollbackFor = CustomException.class)
    public void logout(LoginRequest loginRequest) throws CustomException {
        if (loginRequest.getRefreshToken() == null || loginRequest.getRefreshToken().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Refresh token cannot be empty");
        }
        refreshTokenService.deleteByToken(loginRequest.getRefreshToken());
    }
}
