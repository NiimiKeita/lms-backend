package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.skillbridge.lms.dto.request.CreateFeedbackRequest;
import com.skillbridge.lms.dto.request.CreateSubmissionRequest;
import com.skillbridge.lms.dto.request.UpdateSubmissionStatusRequest;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.TaskFeedbackResponse;
import com.skillbridge.lms.dto.response.TaskSubmissionResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Task;
import com.skillbridge.lms.entity.TaskFeedback;
import com.skillbridge.lms.entity.TaskSubmission;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.SubmissionStatus;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.TaskFeedbackRepository;
import com.skillbridge.lms.repository.TaskRepository;
import com.skillbridge.lms.repository.TaskSubmissionRepository;
import com.skillbridge.lms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TaskSubmissionServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskSubmissionRepository submissionRepository;

    @Mock
    private TaskFeedbackRepository feedbackRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskSubmissionService submissionService;

    private Course course;
    private Task task;
    private User learner;
    private User admin;
    private TaskSubmission submission;

    @BeforeEach
    void setUp() {
        course = Course.builder().id(1L).title("Test Course").build();
        task = Task.builder()
                .id(1L)
                .course(course)
                .title("Test Task")
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        learner = User.builder()
                .id(1L)
                .email("learner@example.com")
                .username("learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();

        admin = User.builder()
                .id(2L)
                .email("admin@example.com")
                .username("admin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        submission = TaskSubmission.builder()
                .id(1L)
                .task(task)
                .user(learner)
                .githubUrl("https://github.com/test/repo")
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("submit - 提出成功")
    void submit_success() {
        CreateSubmissionRequest request = new CreateSubmissionRequest();
        request.setGithubUrl("https://github.com/test/repo");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(submissionRepository.save(any(TaskSubmission.class))).thenAnswer(invocation -> {
            TaskSubmission s = invocation.getArgument(0);
            s.setId(1L);
            s.setStatus(SubmissionStatus.SUBMITTED);
            s.setSubmittedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        TaskSubmissionResponse result = submissionService.submit(1L, "learner@example.com", request);

        assertThat(result).isNotNull();
        assertThat(result.getGithubUrl()).isEqualTo("https://github.com/test/repo");
        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
    }

    @Test
    @DisplayName("submit - 存在しない課題 - ResourceNotFoundException")
    void submit_taskNotFound_throwsException() {
        CreateSubmissionRequest request = new CreateSubmissionRequest();
        request.setGithubUrl("https://github.com/test/repo");

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submit(999L, "learner@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("submit - 存在しないユーザー - ResourceNotFoundException")
    void submit_userNotFound_throwsException() {
        CreateSubmissionRequest request = new CreateSubmissionRequest();
        request.setGithubUrl("https://github.com/test/repo");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submit(1L, "unknown@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getMySubmissions - 自分の提出一覧取得")
    void getMySubmissions_returnsSubmissions() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(submissionRepository.findByTaskIdAndUserIdOrderBySubmittedAtDesc(1L, 1L))
                .thenReturn(List.of(submission));

        List<TaskSubmissionResponse> result = submissionService.getMySubmissions(1L, "learner@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGithubUrl()).isEqualTo("https://github.com/test/repo");
    }

    @Test
    @DisplayName("getSubmissions - 管理者向け提出一覧(ページネーション)")
    void getSubmissions_returnsPagedSubmissions() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TaskSubmission> page = new PageImpl<>(List.of(submission), pageable, 1);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(submissionRepository.findByTaskIdOrderBySubmittedAtDesc(1L, pageable)).thenReturn(page);

        PageResponse<TaskSubmissionResponse> result = submissionService.getSubmissions(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getSubmission - 提出詳細取得(フィードバック付き)")
    void getSubmission_returnsSubmissionWithFeedbacks() {
        TaskFeedback feedback = TaskFeedback.builder()
                .id(1L)
                .submission(submission)
                .reviewer(admin)
                .comment("Good work!")
                .createdAt(LocalDateTime.now())
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(feedbackRepository.findBySubmissionIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(feedback));

        TaskSubmissionResponse result = submissionService.getSubmission(1L);

        assertThat(result).isNotNull();
        assertThat(result.getFeedbacks()).hasSize(1);
        assertThat(result.getFeedbacks().get(0).getComment()).isEqualTo("Good work!");
    }

    @Test
    @DisplayName("getSubmission - 存在しない提出 - ResourceNotFoundException")
    void getSubmission_notFound_throwsException() {
        when(submissionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.getSubmission(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateStatus - ステータス更新成功")
    void updateStatus_success() {
        UpdateSubmissionStatusRequest request = new UpdateSubmissionStatusRequest();
        request.setStatus(SubmissionStatus.APPROVED);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(TaskSubmission.class))).thenAnswer(i -> i.getArgument(0));

        TaskSubmissionResponse result = submissionService.updateStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
    }

    @Test
    @DisplayName("updateStatus - REVIEWING")
    void updateStatus_reviewing() {
        UpdateSubmissionStatusRequest request = new UpdateSubmissionStatusRequest();
        request.setStatus(SubmissionStatus.REVIEWING);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(TaskSubmission.class))).thenAnswer(i -> i.getArgument(0));

        TaskSubmissionResponse result = submissionService.updateStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.REVIEWING);
    }

    @Test
    @DisplayName("updateStatus - REJECTED")
    void updateStatus_rejected() {
        UpdateSubmissionStatusRequest request = new UpdateSubmissionStatusRequest();
        request.setStatus(SubmissionStatus.REJECTED);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(TaskSubmission.class))).thenAnswer(i -> i.getArgument(0));

        TaskSubmissionResponse result = submissionService.updateStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
    }

    @Test
    @DisplayName("addFeedback - フィードバック追加成功")
    void addFeedback_success() {
        CreateFeedbackRequest request = new CreateFeedbackRequest();
        request.setComment("Great job!");

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(feedbackRepository.save(any(TaskFeedback.class))).thenAnswer(invocation -> {
            TaskFeedback f = invocation.getArgument(0);
            f.setId(1L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        TaskFeedbackResponse result = submissionService.addFeedback(1L, "admin@example.com", request);

        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo("Great job!");
        assertThat(result.getReviewerName()).isEqualTo("admin");
    }

    @Test
    @DisplayName("addFeedback - 存在しない提出 - ResourceNotFoundException")
    void addFeedback_submissionNotFound_throwsException() {
        CreateFeedbackRequest request = new CreateFeedbackRequest();
        request.setComment("Feedback");

        when(submissionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.addFeedback(999L, "admin@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
