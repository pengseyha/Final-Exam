package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Externalised settings for ID-card photo handling, bound from the
 * {@code idcard.*} keys in {@code application.properties}.
 */
@ConfigurationProperties(prefix = "idcard")
public class IdCardProperties {

    private final Photo photo = new Photo();

    /** Base URL embedded into QR verification links, e.g. https://cards.example.com. */
    private String baseUrl = "http://localhost:8080";

    public Photo getPhoto() {
        return photo;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static class Photo {
        /** Directory where uploaded photos are stored on local disk. */
        private String dir = "uploads/photos";

        /** Maximum accepted photo size in bytes (default 2 MiB). */
        private long maxSizeBytes = 2_097_152L;

        /** Accepted MIME types for uploaded photos. */
        private List<String> allowedContentTypes = List.of("image/jpeg", "image/png");

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public long getMaxSizeBytes() {
            return maxSizeBytes;
        }

        public void setMaxSizeBytes(long maxSizeBytes) {
            this.maxSizeBytes = maxSizeBytes;
        }

        public List<String> getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(List<String> allowedContentTypes) {
            this.allowedContentTypes = allowedContentTypes;
        }
    }
}
