package com.example.tasker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.tasker.exception.CustomException;
import com.example.tasker.exception.ExceptionForm;

@ControllerAdvice
public class EndpointAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointAdvice.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionForm> customExceptionHandler(CustomException exception) {
        LOGGER.error(exception.getMessage());
        ExceptionForm exceptionform = new ExceptionForm(
                exception.getMessage(),
                exception.getStatus(),
                exception.getTimestamp()
        );
        return new ResponseEntity<>(exceptionform, exception.getStatus());
    }
}
