package com.skillbridge.lms.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.CompletionStatsResponse;
import com.skillbridge.lms.dto.response.EnrollmentTrendResponse;
import com.skillbridge.lms.dto.response.PopularCourseResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.LessonProgressRepository;
import com.skillbridge.lms.repository.ReviewRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final LessonProgressRepository lessonProgressRepository;

    @Transactional(readOnly = true)
    public List<EnrollmentTrendResponse> getEnrollmentTrends(String period) {
        LocalDateTime startDate = switch (period) {
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "30d" -> LocalDateTime.now().minusDays(30);
            default -> LocalDateTime.of(2020, 1, 1, 0, 0);
        };

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getEnrolledAt().isAfter(startDate))
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<EnrollmentTrendResponse> trends = new ArrayList<>();

        LocalDate current = startDate.toLocalDate();
        LocalDate end = LocalDate.now();

        while (!current.isAfter(end)) {
            LocalDate date = current;
            long count = enrollments.stream()
                    .filter(e -> e.getEnrolledAt().toLocalDate().equals(date))
                    .count();
            trends.add(EnrollmentTrendResponse.builder()
                    .date(date.format(formatter))
                    .count(count)
                    .build());
            current = current.plusDays(1);
        }

        return trends;
    }

    @Transactional(readOnly = true)
    public List<CompletionStatsResponse> getCompletionStats() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(course -> {
                    long total = enrollmentRepository.countByCourseId(course.getId());
                    long completed = enrollmentRepository.countByCourseIdAndStatus(
                            course.getId(), EnrollmentStatus.COMPLETED);
                    double rate = total > 0 ? (double) completed / total * 100 : 0;
                    return CompletionStatsResponse.builder()
                            .courseTitle(course.getTitle())
                            .totalEnrollments(total)
                            .completedEnrollments(completed)
                            .completionRate(Math.round(rate * 10) / 10.0)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PopularCourseResponse> getPopularCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(course -> PopularCourseResponse.builder()
                        .courseId(course.getId())
                        .courseTitle(course.getTitle())
                        .enrollmentCount(enrollmentRepository.countByCourseId(course.getId()))
                        .averageRating(reviewRepository.findAverageRatingByCourseId(course.getId()))
                        .build())
                .sorted((a, b) -> Long.compare(b.getEnrollmentCount(), a.getEnrollmentCount()))
                .limit(10)
                .toList();
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("User ID,Username,Email,Role,Course ID,Course Title,Enrollment Status,Enrolled At,Completed At");

        List<User> users = userRepository.findAll();
        for (User user : users) {
            List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
            if (enrollments.isEmpty()) {
                pw.printf("%d,%s,%s,%s,,,,,%n",
                        user.getId(),
                        escapeCsv(user.getUsername()),
                        escapeCsv(user.getEmail()),
                        user.getRole());
            } else {
                for (Enrollment enrollment : enrollments) {
                    pw.printf("%d,%s,%s,%s,%d,%s,%s,%s,%s%n",
                            user.getId(),
                            escapeCsv(user.getUsername()),
                            escapeCsv(user.getEmail()),
                            user.getRole(),
                            enrollment.getCourse().getId(),
                            escapeCsv(enrollment.getCourse().getTitle()),
                            enrollment.getStatus(),
                            enrollment.getEnrolledAt(),
                            enrollment.getCompletedAt() != null ? enrollment.getCompletedAt() : "");
                }
            }
        }

        pw.flush();
        return sw.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
