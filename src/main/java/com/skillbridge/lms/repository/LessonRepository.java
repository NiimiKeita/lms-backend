package com.skillbridge.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillbridge.lms.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderBySortOrderAsc(Long courseId);

    List<Lesson> findByCourseIdAndPublishedTrueOrderBySortOrderAsc(Long courseId);

    long countByCourseId(Long courseId);

    boolean existsByCourseIdAndId(Long courseId, Long id);

    @Query("SELECT COALESCE(MAX(l.sortOrder), 0) FROM Lesson l WHERE l.course.id = :courseId")
    int findMaxSortOrderByCourseId(@Param("courseId") Long courseId);
}
