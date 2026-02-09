package com.skillbridge.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillbridge.lms.entity.LessonProgress;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    boolean existsByUserIdAndLessonIdAndCompletedTrue(Long userId, Long lessonId);

    List<LessonProgress> findByUserIdAndLessonCourseIdAndCompletedTrue(Long userId, Long courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp " +
           "WHERE lp.user.id = :userId AND lp.lesson.course.id = :courseId AND lp.completed = true")
    long countCompletedByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT lp FROM LessonProgress lp " +
           "WHERE lp.user.id = :userId AND lp.lesson.course.id = :courseId " +
           "ORDER BY lp.lesson.sortOrder ASC")
    List<LessonProgress> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    void deleteByUserIdAndLessonId(Long userId, Long lessonId);
}
