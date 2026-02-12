package com.skillbridge.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.enums.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, EnrollmentStatus status);

    List<Enrollment> findByUserIdAndStatusOrderByEnrolledAtDesc(Long userId, EnrollmentStatus status);

    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(Long userId);

    Page<Enrollment> findByCourseIdOrderByEnrolledAtDesc(Long courseId, Pageable pageable);

    long countByCourseId(Long courseId);

    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    List<Enrollment> findByUserId(Long userId);

    long countByStatus(EnrollmentStatus status);
}
