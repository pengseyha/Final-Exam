package com.example.demo.web.api;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.ProfileType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** JSON payload for creating / updating a profile via the REST API. */
public record ProfileRequest(
        ProfileType type,
        String fullName,
        String department,
        String title,
        String email,
        String phone,
        String bloodGroup,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
        BarcodeType barcodeType,
        Long templateId
) {
}
