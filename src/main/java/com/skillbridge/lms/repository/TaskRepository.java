package com.skillbridge.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillbridge.lms.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCourseIdOrderBySortOrderAsc(Long courseId);

    @Query("SELECT COALESCE(MAX(t.sortOrder), 0) FROM Task t WHERE t.course.id = :courseId")
    int findMaxSortOrderByCourseId(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);
}
