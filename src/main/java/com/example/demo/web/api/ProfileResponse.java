package com.example.demo.web.api;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;

import java.time.LocalDate;

/** JSON view of a persisted profile returned by the REST API. */
public record ProfileResponse(
        Long id,
        String uuid,
        String registrationNumber,
        ProfileType type,
        String fullName,
        String department,
        String title,
        String email,
        String phone,
        String bloodGroup,
        LocalDate dateOfBirth,
        LocalDate issueDate,
        LocalDate expiryDate,
        BarcodeType barcodeType,
        boolean hasPhoto,
        Long templateId
) {
    public static ProfileResponse from(Profile p) {
        return new ProfileResponse(
                p.getId(),
                p.getUuid(),
                p.getRegistrationNumber(),
                p.getType(),
                p.getFullName(),
                p.getDepartment(),
                p.getTitle(),
                p.getEmail(),
                p.getPhone(),
                p.getBloodGroup(),
                p.getDateOfBirth(),
                p.getIssueDate(),
                p.getExpiryDate(),
                p.getBarcodeType(),
                p.hasPhoto(),
                p.getTemplate() != null ? p.getTemplate().getId() : null
        );
    }
}
