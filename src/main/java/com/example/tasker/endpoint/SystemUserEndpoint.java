package com.example.tasker.endpoint;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.RegistrationRequest;
import com.example.tasker.model.dto.UserDataDTO;
import com.example.tasker.service.SystemUserService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/users")
public class SystemUserEndpoint {

    private final SystemUserService systemUserService;

    public SystemUserEndpoint(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @Operation(summary = "Retrieves all users")
    @GetMapping
    public List<UserDataDTO> getAllUsers() {
        return systemUserService.getAllUsers();
    }

    @Operation(summary = "Retrieves the data of a user")
    @GetMapping("/{userId}")
    public UserDataDTO getUserData(@PathVariable("userId") Integer userId) throws CustomException {
        return systemUserService.getUserData(userId);
    }

    @Operation(summary = "Registers a new user")
    @PostMapping("/registration")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationRequest) throws CustomException {
        systemUserService.registerUser(registrationRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Activates a user")
    @GetMapping("/activation")
    public ResponseEntity<String> activateUser(@RequestParam("token") String token) throws CustomException {
        systemUserService.activateUser(token);
        return new ResponseEntity<>("User activated", HttpStatus.OK);
    }

    @Operation(summary = "Updates the profile of a user")
    @PutMapping("/profile")
    public ResponseEntity<UserDataDTO> updateUserProfile(@RequestBody UserDataDTO userDataDTO) throws CustomException {
        UserDataDTO userData = systemUserService.updateUserProfile(userDataDTO);
        return new ResponseEntity<>(userData, HttpStatus.OK);
    }

    @Operation(summary = "Updates the profile image of a user")
    @PutMapping("/image")
    public ResponseEntity<?> updateUserImage(@RequestParam("userId") Integer userId, @RequestParam("image") MultipartFile image) throws CustomException {
        systemUserService.updateUserImage(userId, image);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
