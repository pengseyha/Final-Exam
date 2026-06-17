package com.example.demo.repository;

import com.example.demo.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Listing, creating, searching and existence checks for {@link Template}.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
            SELECT t FROM Template t
            WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(t.code) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY t.name ASC
            """)
    List<Template> search(@Param("q") String q);
}
