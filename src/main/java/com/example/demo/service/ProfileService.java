package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.repository.ProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Core CRUD orchestration for {@link Profile}s: assigns the UUID and registration
 * number on creation, manages the attached photo and the selected template.
 */
@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final RegistrationNumberGenerator registrationNumberGenerator;
    private final PhotoStorageService photoStorageService;
    private final TemplateService templateService;

    public ProfileService(ProfileRepository profileRepository,
                          RegistrationNumberGenerator registrationNumberGenerator,
                          PhotoStorageService photoStorageService,
                          TemplateService templateService) {
        this.profileRepository = profileRepository;
        this.registrationNumberGenerator = registrationNumberGenerator;
        this.photoStorageService = photoStorageService;
        this.templateService = templateService;
    }

    @Transactional(readOnly = true)
    public Page<Profile> search(String q, ProfileType type, Pageable pageable) {
        return profileRepository.search(q, type, pageable);
    }

    @Transactional(readOnly = true)
    public List<Profile> findAll() {
        return profileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Profile> findByType(ProfileType type) {
        return profileRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Profile> findByIds(List<Long> ids) {
        return profileRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
    }

    @Transactional(readOnly = true)
    public Profile findByUuid(String uuid) {
        return profileRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + uuid));
    }

    /**
     * Persists a brand-new profile: assigns a UUID and registration number, links
     * the chosen template and stores the photo (if any).
     */
    public Profile create(Profile profile, Long templateId, MultipartFile photo) {
        profile.setUuid(registrationNumberGenerator.newUuid());
        profile.setRegistrationNumber(
                registrationNumberGenerator.nextRegistrationNumber(profile.getType(), profile.getDepartment()));
        applyTemplate(profile, templateId);
        if (photo != null && !photo.isEmpty()) {
            profile.setPhotoFileName(photoStorageService.store(photo));
            profile.setPhotoContentType(photo.getContentType());
        }
        return profileRepository.save(profile);
    }

    /**
     * Updates the editable fields of an existing profile. The UUID and registration
     * number are immutable. A new photo replaces the previous one; {@code removePhoto}
     * clears it.
     */
    public Profile update(Long id, Profile changes, Long templateId, MultipartFile photo, boolean removePhoto) {
        Profile existing = findById(id);
        existing.setType(changes.getType());
        existing.setFullName(changes.getFullName());
        existing.setDepartment(changes.getDepartment());
        existing.setTitle(changes.getTitle());
        existing.setEmail(changes.getEmail());
        existing.setPhone(changes.getPhone());
        existing.setBloodGroup(changes.getBloodGroup());
        existing.setDateOfBirth(changes.getDateOfBirth());
        existing.setIssueDate(changes.getIssueDate());
        existing.setExpiryDate(changes.getExpiryDate());
        existing.setBarcodeType(changes.getBarcodeType());
        applyTemplate(existing, templateId);

        if (removePhoto && existing.hasPhoto()) {
            photoStorageService.delete(existing.getPhotoFileName());
            existing.setPhotoFileName(null);
            existing.setPhotoContentType(null);
        }
        if (photo != null && !photo.isEmpty()) {
            String stored = photoStorageService.store(photo);
            if (existing.hasPhoto()) {
                photoStorageService.delete(existing.getPhotoFileName());
            }
            existing.setPhotoFileName(stored);
            existing.setPhotoContentType(photo.getContentType());
        }
        return profileRepository.save(existing);
    }

    public void delete(Long id) {
        Profile existing = findById(id);
        if (existing.hasPhoto()) {
            photoStorageService.delete(existing.getPhotoFileName());
        }
        profileRepository.delete(existing);
    }

    private void applyTemplate(Profile profile, Long templateId) {
        if (templateId != null) {
            Template template = templateService.findById(templateId);
            profile.setTemplate(template);
        } else if (profile.getTemplate() == null) {
            Optional<Template> first = templateService.findAll().stream().findFirst();
            first.ifPresent(profile::setTemplate);
        }
    }

    /** Reads the raw photo bytes for serving over HTTP. */
    @Transactional(readOnly = true)
    public byte[] loadPhoto(Profile profile) {
        if (!profile.hasPhoto()) {
            throw new ResourceNotFoundException("Profile has no photo: " + profile.getId());
        }
        return photoStorageService.load(profile.getPhotoFileName());
    }

    public boolean hasStoredPhoto(Profile profile) {
        return profile.hasPhoto() && photoStorageService.exists(profile.getPhotoFileName());
    }
}
