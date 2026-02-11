package com.skillbridge.lms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.request.CreateFeedbackRequest;
import com.skillbridge.lms.dto.request.CreateSubmissionRequest;
import com.skillbridge.lms.dto.request.UpdateSubmissionStatusRequest;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.TaskFeedbackResponse;
import com.skillbridge.lms.dto.response.TaskSubmissionResponse;
import com.skillbridge.lms.entity.Task;
import com.skillbridge.lms.entity.TaskFeedback;
import com.skillbridge.lms.entity.TaskSubmission;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.TaskFeedbackRepository;
import com.skillbridge.lms.repository.TaskRepository;
import com.skillbridge.lms.repository.TaskSubmissionRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskSubmissionService {

    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository submissionRepository;
    private final TaskFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public TaskSubmissionResponse submit(Long taskId, String email, CreateSubmissionRequest request) {
        Task task = findTaskById(taskId);
        User user = findUserByEmail(email);

        TaskSubmission submission = TaskSubmission.builder()
                .task(task)
                .user(user)
                .githubUrl(request.getGithubUrl())
                .build();

        submission = submissionRepository.save(submission);
        return TaskSubmissionResponse.from(submission);
    }

    public List<TaskSubmissionResponse> getMySubmissions(Long taskId, String email) {
        findTaskById(taskId);
        User user = findUserByEmail(email);
        return submissionRepository.findByTaskIdAndUserIdOrderBySubmittedAtDesc(taskId, user.getId())
                .stream()
                .map(TaskSubmissionResponse::from)
                .toList();
    }

    public PageResponse<TaskSubmissionResponse> getSubmissions(Long taskId, Pageable pageable) {
        findTaskById(taskId);
        Page<TaskSubmission> page = submissionRepository.findByTaskIdOrderBySubmittedAtDesc(taskId, pageable);

        List<TaskSubmissionResponse> content = page.getContent().stream()
                .map(TaskSubmissionResponse::from)
                .toList();

        return PageResponse.from(page, content);
    }

    public TaskSubmissionResponse getSubmission(Long submissionId) {
        TaskSubmission submission = findSubmissionById(submissionId);
        List<TaskFeedbackResponse> feedbacks = feedbackRepository
                .findBySubmissionIdOrderByCreatedAtDesc(submissionId)
                .stream()
                .map(TaskFeedbackResponse::from)
                .toList();
        return TaskSubmissionResponse.from(submission, feedbacks);
    }

    @Transactional
    public TaskSubmissionResponse updateStatus(Long submissionId, UpdateSubmissionStatusRequest request) {
        TaskSubmission submission = findSubmissionById(submissionId);
        submission.setStatus(request.getStatus());
        submission = submissionRepository.save(submission);
        return TaskSubmissionResponse.from(submission);
    }

    @Transactional
    public TaskFeedbackResponse addFeedback(Long submissionId, String reviewerEmail, CreateFeedbackRequest request) {
        TaskSubmission submission = findSubmissionById(submissionId);
        User reviewer = findUserByEmail(reviewerEmail);

        TaskFeedback feedback = TaskFeedback.builder()
                .submission(submission)
                .reviewer(reviewer)
                .comment(request.getComment())
                .build();

        feedback = feedbackRepository.save(feedback);

        // Create notification for the submission owner
        User submissionOwner = submission.getUser();
        notificationService.createNotification(
                submissionOwner,
                "課題にフィードバックが届きました",
                "「" + submission.getTask().getTitle() + "」にフィードバックが追加されました。",
                "FEEDBACK",
                "/courses/" + submission.getTask().getCourse().getId()
                        + "/tasks/" + submission.getTask().getId());

        return TaskFeedbackResponse.from(feedback);
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("課題が見つかりません: " + taskId));
    }

    private TaskSubmission findSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("提出が見つかりません: " + submissionId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
    }
}
