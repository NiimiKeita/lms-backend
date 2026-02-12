package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.skillbridge.lms.entity.TaskSubmission;
import com.skillbridge.lms.enums.SubmissionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TaskSubmissionResponse {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long userId;
    private String userName;
    private String githubUrl;
    private SubmissionStatus status;
    private List<TaskFeedbackResponse> feedbacks;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    public static TaskSubmissionResponse from(TaskSubmission submission) {
        return TaskSubmissionResponse.builder()
                .id(submission.getId())
                .taskId(submission.getTask().getId())
                .taskTitle(submission.getTask().getTitle())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getUsername())
                .githubUrl(submission.getGithubUrl())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }

    public static TaskSubmissionResponse from(TaskSubmission submission, List<TaskFeedbackResponse> feedbacks) {
        return TaskSubmissionResponse.builder()
                .id(submission.getId())
                .taskId(submission.getTask().getId())
                .taskTitle(submission.getTask().getTitle())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getUsername())
                .githubUrl(submission.getGithubUrl())
                .status(submission.getStatus())
                .feedbacks(feedbacks)
                .submittedAt(submission.getSubmittedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
