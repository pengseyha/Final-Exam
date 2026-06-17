package com.example.demo.repository;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Listing, creating, searching and existence checks for {@link Profile}.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUuid(String uuid);

    Optional<Profile> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByUuid(String uuid);

    List<Profile> findByType(ProfileType type);

    long countByType(ProfileType type);

    /** Counts profiles whose registration number starts with the given prefix (e.g. "2026-ENG-"). */
    long countByRegistrationNumberStartingWith(String prefix);

    /** Free-text search across name, registration number, department and email. */
    @Query("""
            SELECT p FROM Profile p
            WHERE (:type IS NULL OR p.type = :type)
              AND (:q IS NULL OR :q = ''
                   OR LOWER(p.fullName)          LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.department)        LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.email)             LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Profile> search(@Param("q") String q, @Param("type") ProfileType type, Pageable pageable);
}
