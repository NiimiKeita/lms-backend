package com.skillbridge.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.lms.entity.TaskSubmission;
import com.skillbridge.lms.enums.SubmissionStatus;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    List<TaskSubmission> findByTaskIdAndUserIdOrderBySubmittedAtDesc(Long taskId, Long userId);

    Optional<TaskSubmission> findByTaskIdAndUserId(Long taskId, Long userId);

    Page<TaskSubmission> findByTaskIdOrderBySubmittedAtDesc(Long taskId, Pageable pageable);

    long countByTaskId(Long taskId);

    long countByTaskIdAndStatus(Long taskId, SubmissionStatus status);

    List<TaskSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    long countByStatus(SubmissionStatus status);

    List<TaskSubmission> findTop10ByOrderBySubmittedAtDesc();
}
