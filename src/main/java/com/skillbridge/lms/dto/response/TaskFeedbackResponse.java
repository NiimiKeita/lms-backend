package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.TaskFeedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TaskFeedbackResponse {

    private Long id;
    private Long submissionId;
    private Long reviewerId;
    private String reviewerName;
    private String comment;
    private LocalDateTime createdAt;

    public static TaskFeedbackResponse from(TaskFeedback feedback) {
        return TaskFeedbackResponse.builder()
                .id(feedback.getId())
                .submissionId(feedback.getSubmission().getId())
                .reviewerId(feedback.getReviewer().getId())
                .reviewerName(feedback.getReviewer().getUsername())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
