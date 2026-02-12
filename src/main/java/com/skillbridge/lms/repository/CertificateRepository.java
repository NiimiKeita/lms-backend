package com.skillbridge.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.lms.entity.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId);

    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
