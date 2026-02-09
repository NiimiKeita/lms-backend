package com.skillbridge.lms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnrollmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String adminToken;
    private String learnerToken;
    private User admin;
    private User learner;
    private Course publishedCourse;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("admin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        adminToken = tokenProvider.generateAccessToken("admin@example.com");

        learner = User.builder()
                .email("learner@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();
        userRepository.save(learner);
        learnerToken = tokenProvider.generateAccessToken("learner@example.com");

        publishedCourse = Course.builder()
                .title("Test Course")
                .description("Test Description")
                .sortOrder(0)
                .published(true)
                .build();
        courseRepository.save(publishedCourse);
    }

    @Test
    @DisplayName("POST /api/courses/{courseId}/enroll - 受講登録成功 (201)")
    void enroll_success_returns201() throws Exception {
        mockMvc.perform(post("/api/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(publishedCourse.getId()))
                .andExpect(jsonPath("$.userId").value(learner.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/courses/{courseId}/enroll - 重複登録 (400)")
    void enroll_duplicate_returns400() throws Exception {
        // 事前に受講登録
        Enrollment enrollment = Enrollment.builder()
                .user(learner)
                .course(publishedCourse)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/courses/{courseId}/enroll - 未認証 (401)")
    void enroll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/courses/" + publishedCourse.getId() + "/enroll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/courses/{courseId}/enroll - 受講取り消し成功 (200)")
    void unenroll_success_returns200() throws Exception {
        // 事前に受講登録
        Enrollment enrollment = Enrollment.builder()
                .user(learner)
                .course(publishedCourse)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(delete("/api/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("受講登録を取り消しました"));
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/enrollment - 受講状態確認成功 (200)")
    void getEnrollment_success_returns200() throws Exception {
        Enrollment enrollment = Enrollment.builder()
                .user(learner)
                .course(publishedCourse)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(get("/api/courses/" + publishedCourse.getId() + "/enrollment")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.courseTitle").value("Test Course"));
    }

    @Test
    @DisplayName("GET /api/enrollments/my - 自分の受講一覧成功 (200)")
    void getMyEnrollments_success_returns200() throws Exception {
        Enrollment enrollment = Enrollment.builder()
                .user(learner)
                .course(publishedCourse)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(get("/api/enrollments/my")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].courseTitle").value("Test Course"));
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/enrollments - ADMIN受講者一覧成功 (200)")
    void getCourseEnrollments_admin_returns200() throws Exception {
        Enrollment enrollment = Enrollment.builder()
                .user(learner)
                .course(publishedCourse)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(get("/api/courses/" + publishedCourse.getId() + "/enrollments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/enrollments - LEARNERは403")
    void getCourseEnrollments_learner_returns403() throws Exception {
        mockMvc.perform(get("/api/courses/" + publishedCourse.getId() + "/enrollments")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isForbidden());
    }
}
