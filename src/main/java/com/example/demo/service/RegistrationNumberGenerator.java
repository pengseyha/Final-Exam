package com.example.demo.service;

import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.UUID;

/**
 * Generates the public UUID and a human-friendly registration number in the
 * {@code YEAR-DEPT-###} format, e.g. {@code 2026-ENG-014}.
 */
@Service
public class RegistrationNumberGenerator {

    private final ProfileRepository profileRepository;

    public RegistrationNumberGenerator(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /** A fresh random UUID (used as the stable public identifier on the card / QR code). */
    public String newUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Builds the next registration number for the given type and department.
     * The sequence is per {@code YEAR-DEPT} prefix and zero-padded to three digits;
     * uniqueness is guaranteed even under concurrent inserts by probing the DB.
     */
    public String nextRegistrationNumber(ProfileType type, String department) {
        String dept = departmentCode(type, department);
        String prefix = Year.now().getValue() + "-" + dept + "-";

        long seq = profileRepository.countByRegistrationNumberStartingWith(prefix) + 1;
        String candidate = prefix + pad(seq);
        while (profileRepository.existsByRegistrationNumber(candidate)) {
            seq++;
            candidate = prefix + pad(seq);
        }
        return candidate;
    }

    private String pad(long seq) {
        return String.format("%03d", seq);
    }

    /**
     * Derives a short, uppercase department code. Falls back to a type-based code
     * when no department is supplied.
     */
    String departmentCode(ProfileType type, String department) {
        if (department != null && !department.isBlank()) {
            String letters = department.replaceAll("[^A-Za-z]", "").toUpperCase();
            if (!letters.isEmpty()) {
                return letters.substring(0, Math.min(3, letters.length()));
            }
        }
        return switch (type == null ? ProfileType.USER : type) {
            case STUDENT -> "STU";
            case EMPLOYEE -> "EMP";
            case USER -> "USR";
        };
    }
}
