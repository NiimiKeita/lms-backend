package com.skillbridge.lms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.Lesson;
import com.skillbridge.lms.entity.LessonProgress;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.LessonProgressRepository;
import com.skillbridge.lms.repository.LessonRepository;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String adminToken;
    private String learnerToken;
    private User admin;
    private User learner;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .email("admin@progress.com")
                .password(passwordEncoder.encode("password123"))
                .username("Admin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        adminToken = tokenProvider.generateAccessToken("admin@progress.com");

        learner = User.builder()
                .email("learner@progress.com")
                .password(passwordEncoder.encode("password123"))
                .username("Learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();
        userRepository.save(learner);
        learnerToken = tokenProvider.generateAccessToken("learner@progress.com");

        course = Course.builder()
                .title("Test Course")
                .description("Description")
                .sortOrder(0)
                .published(true)
                .build();
        courseRepository.save(course);

        lesson = Lesson.builder()
                .course(course)
                .title("Lesson 1")
                .contentPath("courses/1/lessons/1.md")
                .sortOrder(1)
                .published(true)
                .build();
        lessonRepository.save(lesson);

        Enrollment enrollment = Enrollment.builder()
                .user(learner)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);

        LessonProgress progress = LessonProgress.builder()
                .user(learner)
                .lesson(lesson)
                .completed(true)
                .build();
        lessonProgressRepository.save(progress);
    }

    // ===== GET /api/admin/progress =====

    @Test
    @DisplayName("GET /api/admin/progress - ADMIN: 進捗一覧取得成功 (200)")
    void getProgress_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/progress")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET /api/admin/progress - LEARNER: 403 Forbidden")
    void getProgress_asLearner_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/progress")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isForbidden());
    }

    // ===== GET /api/admin/users/{id}/progress =====

    @Test
    @DisplayName("GET /api/admin/users/{id}/progress - ユーザー進捗詳細取得成功 (200)")
    void getUserProgress_existingUser_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + learner.getId() + "/progress")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseId").value(course.getId()))
                .andExpect(jsonPath("$[0].courseTitle").value("Test Course"))
                .andExpect(jsonPath("$[0].completedLessons").value(1))
                .andExpect(jsonPath("$[0].totalLessons").value(1))
                .andExpect(jsonPath("$[0].progressPercentage").value(100.0));
    }

    @Test
    @DisplayName("GET /api/admin/users/{id}/progress - 存在しないユーザー (404)")
    void getUserProgress_nonExistingUser_returns404() throws Exception {
        mockMvc.perform(get("/api/admin/users/99999/progress")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ===== GET /api/admin/stats =====

    @Test
    @DisplayName("GET /api/admin/stats - 統計取得成功 (200)")
    void getStats_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalCourses").isNumber())
                .andExpect(jsonPath("$.totalEnrollments").isNumber())
                .andExpect(jsonPath("$.averageCompletionRate").isNumber());
    }

    @Test
    @DisplayName("GET /api/admin/stats - LEARNER: 403 Forbidden")
    void getStats_asLearner_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isForbidden());
    }
}
