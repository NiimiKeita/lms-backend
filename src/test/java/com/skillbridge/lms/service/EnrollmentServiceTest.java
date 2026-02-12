package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.skillbridge.lms.dto.response.EnrollmentResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User learner;
    private Course publishedCourse;
    private Course unpublishedCourse;
    private Enrollment activeEnrollment;

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

        publishedCourse = Course.builder()
                .id(1L)
                .title("Published Course")
                .description("Description")
                .sortOrder(0)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lessons(new ArrayList<>())
                .build();

        unpublishedCourse = Course.builder()
                .id(2L)
                .title("Unpublished Course")
                .description("Description")
                .sortOrder(1)
                .published(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lessons(new ArrayList<>())
                .build();

        activeEnrollment = Enrollment.builder()
                .id(1L)
                .user(learner)
                .course(publishedCourse)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("enroll - 公開コースに受講登録 - 成功")
    void enroll_publishedCourse_success() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(1L);
            e.setEnrolledAt(LocalDateTime.now());
            return e;
        });

        // When
        EnrollmentResponse response = enrollmentService.enroll(1L, "learner@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCourseId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("enroll - 非公開コース - BadRequestException")
    void enroll_unpublishedCourse_throwsBadRequest() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(unpublishedCourse));

        // When & Then
        assertThatThrownBy(() -> enrollmentService.enroll(2L, "learner@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("非公開");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll - 重複登録 - BadRequestException")
    void enroll_alreadyEnrolled_throwsBadRequest() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> enrollmentService.enroll(1L, "learner@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("既に");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll - 存在しないコース - ResourceNotFoundException")
    void enroll_nonExistingCourse_throwsNotFound() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> enrollmentService.enroll(999L, "learner@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("unenroll - 受講取り消し - 成功")
    void unenroll_success() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L)).thenReturn(Optional.of(activeEnrollment));

        // When
        enrollmentService.unenroll(1L, "learner@example.com");

        // Then
        verify(enrollmentRepository).delete(activeEnrollment);
    }

    @Test
    @DisplayName("unenroll - 受講登録なし - ResourceNotFoundException")
    void unenroll_notEnrolled_throwsNotFound() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> enrollmentService.unenroll(1L, "learner@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("受講登録が見つかりません");
    }

    @Test
    @DisplayName("getEnrollment - 受講状態確認 - 成功")
    void getEnrollment_success() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L)).thenReturn(Optional.of(activeEnrollment));

        // When
        EnrollmentResponse response = enrollmentService.getEnrollment(1L, "learner@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(response.getCourseTitle()).isEqualTo("Published Course");
    }

    @Test
    @DisplayName("getMyEnrollments - 自分の受講一覧 - 成功")
    void getMyEnrollments_success() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(1L))
                .thenReturn(List.of(activeEnrollment));

        // When
        List<EnrollmentResponse> response = enrollmentService.getMyEnrollments("learner@example.com");

        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCourseTitle()).isEqualTo("Published Course");
    }

    @Test
    @DisplayName("getCourseEnrollments - ADMIN受講者一覧 - 成功")
    void getCourseEnrollments_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Enrollment> enrollments = List.of(activeEnrollment);
        Page<Enrollment> page = new PageImpl<>(enrollments, pageable, enrollments.size());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.findByCourseIdOrderByEnrolledAtDesc(1L, pageable)).thenReturn(page);

        // When
        PageResponse<EnrollmentResponse> response = enrollmentService.getCourseEnrollments(1L, pageable);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("isEnrolled - 受講中 - true")
    void isEnrolled_enrolled_returnsTrue() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE))
                .thenReturn(true);

        // When
        boolean result = enrollmentService.isEnrolled(1L, "learner@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isEnrolled - 未受講 - false")
    void isEnrolled_notEnrolled_returnsFalse() {
        // Given
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(learner));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(1L, 1L, EnrollmentStatus.ACTIVE))
                .thenReturn(false);

        // When
        boolean result = enrollmentService.isEnrolled(1L, "learner@example.com");

        // Then
        assertThat(result).isFalse();
    }
}
