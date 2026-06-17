package com.example.demo.web.api;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * JSON REST API for profile CRUD, complementing the Thymeleaf web UI. Useful for
 * integrations and automated testing.
 */
@RestController
@RequestMapping("/api/profiles")
public class ProfileApiController {

    private final ProfileService profileService;

    public ProfileApiController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public List<ProfileResponse> list() {
        return profileService.findAll().stream().map(ProfileResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProfileResponse get(@PathVariable Long id) {
        return ProfileResponse.from(profileService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse create(@RequestBody ProfileRequest request) {
        Profile created = profileService.create(toEntity(request), request.templateId(), null);
        return ProfileResponse.from(created);
    }

    @PutMapping("/{id}")
    public ProfileResponse update(@PathVariable Long id, @RequestBody ProfileRequest request) {
        Profile updated = profileService.update(id, toEntity(request), request.templateId(), null, false);
        return ProfileResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        profileService.delete(id);
    }

    private Profile toEntity(ProfileRequest r) {
        return Profile.builder()
                .type(r.type() != null ? r.type() : ProfileType.USER)
                .fullName(r.fullName())
                .department(r.department())
                .title(r.title())
                .email(r.email())
                .phone(r.phone())
                .bloodGroup(r.bloodGroup())
                .dateOfBirth(r.dateOfBirth())
                .issueDate(r.issueDate())
                .expiryDate(r.expiryDate())
                .barcodeType(r.barcodeType() != null ? r.barcodeType() : BarcodeType.CODE_128)
                .build();
    }
}
