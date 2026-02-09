package com.skillbridge.lms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.EnrollmentResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * コース受講登録
     */
    @Transactional
    public EnrollmentResponse enroll(Long courseId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Course course = findCourseById(courseId);

        // 公開コースのみ受講可能
        if (!course.getPublished()) {
            throw new BadRequestException("非公開のコースには受講登録できません");
        }

        // 重複チェック
        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new BadRequestException("既にこのコースに受講登録済みです");
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.from(enrollment);
    }

    /**
     * 受講取り消し
     */
    @Transactional
    public void unenroll(Long courseId, String userEmail) {
        User user = findUserByEmail(userEmail);
        findCourseById(courseId);

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("受講登録が見つかりません"));

        enrollmentRepository.delete(enrollment);
    }

    /**
     * 受講状態確認
     */
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollment(Long courseId, String userEmail) {
        User user = findUserByEmail(userEmail);
        findCourseById(courseId);

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("受講登録が見つかりません"));

        return EnrollmentResponse.from(enrollment);
    }

    /**
     * 自分の受講コース一覧
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String userEmail) {
        User user = findUserByEmail(userEmail);

        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(user.getId());
        return enrollments.stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    /**
     * コースの受講者一覧 (ADMIN)
     */
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getCourseEnrollments(Long courseId, Pageable pageable) {
        findCourseById(courseId);

        Page<Enrollment> page = enrollmentRepository.findByCourseIdOrderByEnrolledAtDesc(courseId, pageable);
        List<EnrollmentResponse> content = page.getContent().stream()
                .map(EnrollmentResponse::from)
                .toList();

        return PageResponse.from(page, content);
    }

    /**
     * 受講登録の存在確認
     */
    @Transactional(readOnly = true)
    public boolean isEnrolled(Long courseId, String userEmail) {
        User user = findUserByEmail(userEmail);
        return enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
                user.getId(), courseId, EnrollmentStatus.ACTIVE);
    }

    /**
     * Enrollmentのステータスを更新 (内部利用)
     */
    @Transactional
    public void updateStatus(Long userId, Long courseId, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("受講登録が見つかりません"));

        enrollment.setStatus(status);
        if (status == EnrollmentStatus.COMPLETED) {
            enrollment.setCompletedAt(java.time.LocalDateTime.now());
        }
        enrollmentRepository.save(enrollment);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
    }

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("コースが見つかりません: " + courseId));
    }
}
