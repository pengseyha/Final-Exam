package com.example.demo.web;

/**
 * Flattened, display-ready view of an ID card shared by the live preview and the
 * saved-card page. Image fields are either HTTP endpoint URLs (for a saved
 * profile) or {@code data:} URIs (for an unsaved live preview).
 */
public record CardView(
        String organizationName,
        String tagline,
        String typeLabel,
        String fullName,
        String title,
        String registrationNumber,
        String department,
        String bloodGroup,
        String email,
        String phone,
        String dateOfBirth,
        String issueDate,
        String expiryDate,
        String layout,
        String primaryColor,
        String secondaryColor,
        String textColor,
        String photoSrc,
        String qrSrc,
        String barcodeSrc,
        String verificationUrl
) {
    public boolean hasPhoto() {
        return photoSrc != null && !photoSrc.isBlank();
    }
}
