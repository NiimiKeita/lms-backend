package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.skillbridge.lms.dto.response.CourseProgressResponse;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.Lesson;
import com.skillbridge.lms.entity.LessonProgress;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.LessonProgressRepository;
import com.skillbridge.lms.repository.LessonRepository;
import com.skillbridge.lms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProgressService progressService;

    private User learner;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        learner = User.builder()
                .id(1L)
                .email("learner@example.com")
                .password("encoded")
                .username("learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        course = Course.builder()
                .id(1L)
                .title("Test Course")
                .description("Description")
                .sortOrder(0)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lessons(new ArrayList<>())
                .build();

        lesson1 = Lesson.builder()
                .id(1L)
                .course(course)
                .title("Lesson 1")
                .contentPath("/lesson1.md")
                .sortOrder(1)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        lesson2 = Lesson.builder()
                .id(2L)
                .course(course)
                .title("Lesson 2")
                .contentPath("/lesson2.md")
                .sortOrder(2)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        enrollment = Enrollment.builder()
                .id(1L)
                .user(learner)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("completeLesson - 新規完了 - 成功")
    void completeLesson_newCompletion_success() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonProgressRepository.findByUserIdAndLessonId(1L, 1L)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(i -> {
            LessonProgress lp = i.getArgument(0);
            lp.setId(1L);
            return lp;
        });
        when(lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(1L))
                .thenReturn(List.of(lesson1, lesson2));
        when(lessonProgressRepository.countCompletedByUserIdAndCourseId(1L, 1L)).thenReturn(1L);

        // When
        MessageResponse response = progressService.completeLesson(1L, 1L, "learner@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("完了");
        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    @Test
    @DisplayName("completeLesson - 既に完了済み - メッセージ返却")
    void completeLesson_alreadyCompleted_returnsMessage() {
        // Given
        LessonProgress existingProgress = LessonProgress.builder()
                .id(1L)
                .user(learner)
                .lesson(lesson1)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonProgressRepository.findByUserIdAndLessonId(1L, 1L)).thenReturn(Optional.of(existingProgress));

        // When
        MessageResponse response = progressService.completeLesson(1L, 1L, "learner@example.com");

        // Then
        assertThat(response.getMessage()).contains("既に完了済み");
    }

    @Test
    @DisplayName("completeLesson - 未受講コース - BadRequestException")
    void completeLesson_notEnrolled_throwsBadRequest() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE)).thenReturn(false);
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.COMPLETED)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> progressService.completeLesson(1L, 1L, "learner@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("受講登録されていません");
    }

    @Test
    @DisplayName("completeLesson - 存在しないレッスン - ResourceNotFoundException")
    void completeLesson_nonExistingLesson_throwsNotFound() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> progressService.completeLesson(1L, 999L, "learner@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("uncompleteLesson - 完了取り消し - 成功")
    void uncompleteLesson_success() {
        // Given
        LessonProgress completedProgress = LessonProgress.builder()
                .id(1L)
                .user(learner)
                .lesson(lesson1)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(lessonProgressRepository.findByUserIdAndLessonId(1L, 1L)).thenReturn(Optional.of(completedProgress));
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L)).thenReturn(Optional.of(enrollment));

        // When
        MessageResponse response = progressService.uncompleteLesson(1L, 1L, "learner@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("取り消し");
        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    @Test
    @DisplayName("getCourseProgress - 進捗取得 - 成功")
    void getCourseProgress_success() {
        // Given
        LessonProgress progress1 = LessonProgress.builder()
                .id(1L)
                .user(learner)
                .lesson(lesson1)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(1L))
                .thenReturn(List.of(lesson1, lesson2));
        when(lessonProgressRepository.findByUserIdAndCourseId(1L, 1L))
                .thenReturn(List.of(progress1));

        // When
        CourseProgressResponse response = progressService.getCourseProgress(1L, "learner@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCourseId()).isEqualTo(1L);
        assertThat(response.getTotalLessons()).isEqualTo(2);
        assertThat(response.getCompletedLessons()).isEqualTo(1);
        assertThat(response.getProgressPercentage()).isEqualTo(50.0);
        assertThat(response.getLessonProgresses()).hasSize(2);
    }

    @Test
    @DisplayName("getMyProgress - 全コース進捗サマリー - 成功")
    void getMyProgress_success() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(1L))
                .thenReturn(List.of(enrollment));
        when(lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(1L))
                .thenReturn(List.of(lesson1, lesson2));
        when(lessonProgressRepository.countCompletedByUserIdAndCourseId(1L, 1L))
                .thenReturn(1L);

        // When
        List<CourseProgressResponse> response = progressService.getMyProgress("learner@example.com");

        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCourseId()).isEqualTo(1L);
        assertThat(response.get(0).getTotalLessons()).isEqualTo(2);
        assertThat(response.get(0).getCompletedLessons()).isEqualTo(1);
        assertThat(response.get(0).getProgressPercentage()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("completeLesson - 全レッスン完了でコース完了に自動更新")
    void completeLesson_allLessonsCompleted_updatesEnrollmentToCompleted() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(lesson2));
        when(lessonProgressRepository.findByUserIdAndLessonId(1L, 2L)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(i -> {
            LessonProgress lp = i.getArgument(0);
            lp.setId(2L);
            return lp;
        });
        when(lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(1L))
                .thenReturn(List.of(lesson1, lesson2));
        // All lessons completed
        when(lessonProgressRepository.countCompletedByUserIdAndCourseId(1L, 1L)).thenReturn(2L);
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArgument(0));

        // When
        progressService.completeLesson(1L, 2L, "learner@example.com");

        // Then
        verify(enrollmentRepository).save(any(Enrollment.class));
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
    }
}
