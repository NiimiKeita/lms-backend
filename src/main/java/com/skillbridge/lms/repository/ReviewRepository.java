package com.skillbridge.lms.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillbridge.lms.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    Optional<Review> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);
}
