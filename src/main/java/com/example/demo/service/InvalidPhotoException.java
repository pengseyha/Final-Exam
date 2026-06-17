package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when an uploaded photo fails type/size validation. Maps to HTTP 400. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPhotoException extends RuntimeException {
    public InvalidPhotoException(String message) {
        super(message);
    }
}
