package com.example.demo.repository;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository repository;

    private Profile newProfile(String name, String reg, ProfileType type, String dept) {
        return Profile.builder()
                .uuid(java.util.UUID.randomUUID().toString())
                .registrationNumber(reg)
                .type(type)
                .fullName(name)
                .department(dept)
                .barcodeType(BarcodeType.CODE_128)
                .build();
    }

    @Test
    void savesAndFindsByUuid() {
        Profile saved = repository.save(newProfile("Alice", "2026-ENG-001", ProfileType.STUDENT, "Engineering"));

        assertThat(repository.findByUuid(saved.getUuid())).isPresent();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void existsByRegistrationNumber() {
        repository.save(newProfile("Bob", "2026-ENG-002", ProfileType.STUDENT, "Engineering"));

        assertThat(repository.existsByRegistrationNumber("2026-ENG-002")).isTrue();
        assertThat(repository.existsByRegistrationNumber("2026-ENG-999")).isFalse();
    }

    @Test
    void countsByRegistrationNumberPrefix() {
        repository.save(newProfile("A", "2026-ENG-001", ProfileType.STUDENT, "Engineering"));
        repository.save(newProfile("B", "2026-ENG-002", ProfileType.STUDENT, "Engineering"));
        repository.save(newProfile("C", "2026-ADM-001", ProfileType.EMPLOYEE, "Administration"));

        assertThat(repository.countByRegistrationNumberStartingWith("2026-ENG-")).isEqualTo(2);
        assertThat(repository.countByRegistrationNumberStartingWith("2026-ADM-")).isEqualTo(1);
    }

    @Test
    void searchMatchesNameAndType() {
        repository.save(newProfile("Charlie Brown", "2026-ENG-010", ProfileType.STUDENT, "Engineering"));
        repository.save(newProfile("Dana White", "2026-ADM-010", ProfileType.EMPLOYEE, "Administration"));

        Page<Profile> byName = repository.search("charlie", null, PageRequest.of(0, 10));
        assertThat(byName.getContent()).hasSize(1);
        assertThat(byName.getContent().get(0).getFullName()).isEqualTo("Charlie Brown");

        Page<Profile> byType = repository.search(null, ProfileType.EMPLOYEE, PageRequest.of(0, 10));
        assertThat(byType.getContent()).hasSize(1);
        assertThat(byType.getContent().get(0).getType()).isEqualTo(ProfileType.EMPLOYEE);
    }
}
