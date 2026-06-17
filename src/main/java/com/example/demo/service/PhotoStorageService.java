package com.example.demo.service;

import com.example.demo.config.IdCardProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Stores uploaded ID-card photos on the local filesystem, validating type and
 * size before accepting the file. Returns an opaque generated file name that is
 * persisted on the {@link com.example.demo.model.Profile}.
 */
@Service
public class PhotoStorageService {

    private final IdCardProperties properties;
    private final Path root;

    public PhotoStorageService(IdCardProperties properties) {
        this.properties = properties;
        this.root = Paths.get(properties.getPhoto().getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create photo storage directory: " + root, e);
        }
    }

    /**
     * Validates and stores the photo, returning the stored file name.
     *
     * @throws InvalidPhotoException if the file is empty, too large or an unsupported type
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPhotoException("Photo file is empty.");
        }
        long max = properties.getPhoto().getMaxSizeBytes();
        if (file.getSize() > max) {
            throw new InvalidPhotoException(
                    "Photo is too large (%d bytes); maximum is %d bytes.".formatted(file.getSize(), max));
        }
        String contentType = file.getContentType();
        if (contentType == null || !properties.getPhoto().getAllowedContentTypes().contains(contentType)) {
            throw new InvalidPhotoException(
                    "Unsupported photo type '%s'. Allowed: %s"
                            .formatted(contentType, properties.getPhoto().getAllowedContentTypes()));
        }

        String extension = "image/png".equals(contentType) ? ".png" : ".jpg";
        String fileName = UUID.randomUUID() + extension;
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new InvalidPhotoException("Resolved path escapes storage directory.");
        }
        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store photo " + fileName, e);
        }
        return fileName;
    }

    /** Reads a stored photo's bytes. */
    public byte[] load(String fileName) {
        Path target = resolveExisting(fileName);
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read photo " + fileName, e);
        }
    }

    /** Best-effort delete; ignores a missing file. */
    public void delete(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }
        try {
            Files.deleteIfExists(root.resolve(fileName).normalize());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete photo " + fileName, e);
        }
    }

    public boolean exists(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        return Files.exists(root.resolve(fileName).normalize());
    }

    private Path resolveExisting(String fileName) {
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root) || !Files.exists(target)) {
            throw new ResourceNotFoundException("Photo not found: " + fileName);
        }
        return target;
    }
}
