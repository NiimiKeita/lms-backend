package com.skillbridge.lms.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.AdminStatsResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.UserCourseProgressResponse;
import com.skillbridge.lms.dto.response.UserProgressSummaryResponse;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.LessonProgressRepository;
import com.skillbridge.lms.repository.LessonRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProgressService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserProgressSummaryResponse> getUserProgressSummaries(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        List<UserProgressSummaryResponse> content = users.getContent().stream()
                .map(this::buildUserProgressSummary)
                .toList();

        return PageResponse.from(users, content);
    }

    @Transactional(readOnly = true)
    public List<UserCourseProgressResponse> getUserCourseProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません: " + userId));

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        List<UserCourseProgressResponse> result = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Long courseId = enrollment.getCourse().getId();
            long totalLessons = lessonRepository.countByCourseId(courseId);
            long completedLessons = lessonProgressRepository.countCompletedByUserIdAndCourseId(userId, courseId);
            double percentage = totalLessons > 0 ? (double) completedLessons / totalLessons * 100 : 0;

            result.add(UserCourseProgressResponse.builder()
                    .courseId(courseId)
                    .courseTitle(enrollment.getCourse().getTitle())
                    .completedLessons((int) completedLessons)
                    .totalLessons((int) totalLessons)
                    .progressPercentage(Math.round(percentage * 10.0) / 10.0)
                    .enrolledAt(enrollment.getEnrolledAt())
                    .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();

        long completedEnrollments = enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED);
        double averageCompletionRate = totalEnrollments > 0
                ? Math.round((double) completedEnrollments / totalEnrollments * 1000.0) / 10.0
                : 0;

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalCourses(totalCourses)
                .totalEnrollments(totalEnrollments)
                .averageCompletionRate(averageCompletionRate)
                .build();
    }

    private UserProgressSummaryResponse buildUserProgressSummary(User user) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        int enrolledCourses = enrollments.size();
        int completedCourses = (int) enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();

        double totalProgress = 0;
        int coursesWithLessons = 0;

        for (Enrollment enrollment : enrollments) {
            Long courseId = enrollment.getCourse().getId();
            long totalLessons = lessonRepository.countByCourseId(courseId);
            if (totalLessons > 0) {
                long completed = lessonProgressRepository.countCompletedByUserIdAndCourseId(user.getId(), courseId);
                totalProgress += (double) completed / totalLessons * 100;
                coursesWithLessons++;
            }
        }

        double averageProgress = coursesWithLessons > 0
                ? Math.round(totalProgress / coursesWithLessons * 10.0) / 10.0
                : 0;

        return UserProgressSummaryResponse.builder()
                .userId(user.getId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .enrolledCourses(enrolledCourses)
                .completedCourses(completedCourses)
                .averageProgress(averageProgress)
                .build();
    }
}
