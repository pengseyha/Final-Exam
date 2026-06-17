package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates many ID cards at once (for a class, team or employee group) and
 * bundles the individual PDFs into a single ZIP archive.
 */
@Service
public class BatchIdCardService {

    private final ProfileService profileService;
    private final IdCardPdfService pdfService;

    public BatchIdCardService(ProfileService profileService, IdCardPdfService pdfService) {
        this.profileService = profileService;
        this.pdfService = pdfService;
    }

    /** Builds a ZIP containing one PDF per profile id. */
    public byte[] zipForIds(List<Long> ids) {
        return zip(profileService.findByIds(ids));
    }

    /** Builds a ZIP containing one PDF per profile of the given type. */
    public byte[] zipForType(ProfileType type) {
        return zip(profileService.findByType(type));
    }

    /** Builds a ZIP for every profile in the system. */
    public byte[] zipAll() {
        return zip(profileService.findAll());
    }

    private byte[] zip(List<Profile> profiles) {
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("No profiles match the batch request.");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Profile profile : profiles) {
                byte[] pdf = pdfService.render(profile);
                zip.putNextEntry(new ZipEntry(entryName(profile)));
                zip.write(pdf);
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build batch ZIP", e);
        }
        return out.toByteArray();
    }

    private String entryName(Profile profile) {
        String safeReg = profile.getRegistrationNumber().replaceAll("[^A-Za-z0-9._-]", "_");
        return "id-card-" + safeReg + ".pdf";
    }
}
