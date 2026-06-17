package com.example.demo.web;

import com.example.demo.model.Profile;
import com.example.demo.model.Template;
import com.example.demo.service.IdCardService;
import com.example.demo.service.ProfileService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Builds {@link CardView}s for rendering. For saved profiles the QR/barcode/photo
 * point at HTTP endpoints; for an unsaved live preview they are inlined as base64
 * {@code data:} URIs generated on the fly.
 */
@Component
public class CardViewFactory {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final IdCardService idCardService;
    private final ProfileService profileService;

    public CardViewFactory(IdCardService idCardService, ProfileService profileService) {
        this.idCardService = idCardService;
        this.profileService = profileService;
    }

    /** View for a persisted profile, using media endpoints for images. */
    public CardView forSavedProfile(Profile p) {
        String base = "/profiles/" + p.getId();
        String photoSrc = profileService.hasStoredPhoto(p) ? base + "/photo" : null;
        return build(p, photoSrc, base + "/qr", base + "/barcode");
    }

    /** View for an unsaved profile (live preview), inlining images as data URIs. */
    public CardView forPreview(Profile p) {
        ensurePreviewIdentifiers(p);
        String qr = dataUri(idCardService.qrPng(p, 220));
        String barcode = dataUri(idCardService.barcodePng(p));
        return build(p, null, qr, barcode);
    }

    private CardView build(Profile p, String photoSrc, String qrSrc, String barcodeSrc) {
        Template t = p.getTemplate();
        return new CardView(
                t != null && t.getOrganizationName() != null ? t.getOrganizationName() : "ID Card",
                t != null ? t.getTagline() : null,
                p.getType() != null ? p.getType().name() : "USER",
                p.getFullName(),
                p.getTitle(),
                p.getRegistrationNumber(),
                p.getDepartment(),
                p.getBloodGroup(),
                p.getEmail(),
                p.getPhone(),
                fmt(p.getDateOfBirth()),
                fmt(p.getIssueDate()),
                fmt(p.getExpiryDate()),
                t != null ? t.getLayout() : "VERTICAL",
                t != null ? t.getPrimaryColor() : "#1d4ed8",
                t != null ? t.getSecondaryColor() : "#e0e7ff",
                t != null ? t.getTextColor() : "#111827",
                photoSrc,
                qrSrc,
                barcodeSrc,
                idCardService.verificationUrl(p)
        );
    }

    /** Fills in placeholder UUID / registration number so a preview can render codes. */
    private void ensurePreviewIdentifiers(Profile p) {
        if (p.getUuid() == null || p.getUuid().isBlank()) {
            p.setUuid("preview-0000");
        }
        if (p.getRegistrationNumber() == null || p.getRegistrationNumber().isBlank()) {
            p.setRegistrationNumber(LocalDate.now().getYear() + "-PRV-000");
        }
    }

    private static String fmt(LocalDate date) {
        return date == null ? null : date.format(DATE);
    }

    private static String dataUri(byte[] png) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }
}
