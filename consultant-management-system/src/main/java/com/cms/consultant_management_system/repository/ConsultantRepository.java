package com.cms.consultant_management_system.repository;

import com.cms.consultant_management_system.entity.Consultant;
import com.cms.consultant_management_system.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConsultantRepository extends JpaRepository<Consultant, Long> {

    // Part B: search by name OR technology, case-insensitive, partial match
    @Query("""
           SELECT c FROM Consultant c
           WHERE LOWER(c.name)       LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.technology) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    Page<Consultant> search(@Param("keyword") String keyword, Pageable pageable);

    // Duplicate-email checks
    Optional<Consultant> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // Dashboard counters
    long countByStatus(Status status);

    long countByCreatedAtAfter(LocalDateTime since);
}