package com.example.demo.model;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Builds sensible default {@link Profile} instances for each {@link ProfileType}.
 *
 * <p>This is a thin convenience layer on top of Lombok's generated builder: it
 * pre-fills validity dates, a default barcode symbology and type-specific labels
 * so the create form (or seed data) starts from a usable card rather than a blank
 * object.
 */
@Component
public class ProfileBuilder {

    /** Default card validity in years from the issue date. */
    public static final int DEFAULT_VALIDITY_YEARS = 4;

    /**
     * Returns a fresh default profile for the given type. The returned object is
     * <em>not</em> persisted and has neither a UUID nor a registration number yet
     * (those are assigned by the service when the profile is saved).
     */
    public Profile defaultFor(ProfileType type) {
        ProfileType resolved = type == null ? ProfileType.USER : type;
        LocalDate issue = LocalDate.now();
        return Profile.builder()
                .type(resolved)
                .barcodeType(BarcodeType.CODE_128)
                .issueDate(issue)
                .expiryDate(issue.plusYears(DEFAULT_VALIDITY_YEARS))
                .title(defaultTitle(resolved))
                .department(defaultDepartment(resolved))
                .build();
    }

    private String defaultTitle(ProfileType type) {
        return switch (type) {
            case STUDENT -> "Student";
            case EMPLOYEE -> "Staff";
            case USER -> "Member";
        };
    }

    private String defaultDepartment(ProfileType type) {
        return switch (type) {
            case STUDENT -> "General Studies";
            case EMPLOYEE -> "Administration";
            case USER -> "General";
        };
    }
}
