package com.skillbridge.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.lms.entity.TaskFeedback;

public interface TaskFeedbackRepository extends JpaRepository<TaskFeedback, Long> {

    List<TaskFeedback> findBySubmissionIdOrderByCreatedAtDesc(Long submissionId);
}
