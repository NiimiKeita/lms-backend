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
class ProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String learnerToken;
    private User learner;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    void setUp() {
        learner = User.builder()
                .email("learner@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();
        userRepository.save(learner);
        learnerToken = tokenProvider.generateAccessToken("learner@example.com");

        course = Course.builder()
                .title("Test Course")
                .description("Description")
                .sortOrder(0)
                .published(true)
                .build();
        courseRepository.save(course);

        lesson1 = Lesson.builder()
                .course(course)
                .title("Lesson 1")
                .contentPath("/lesson1.md")
                .sortOrder(1)
                .published(true)
                .build();
        lessonRepository.save(lesson1);

        lesson2 = Lesson.builder()
                .course(course)
                .title("Lesson 2")
                .contentPath("/lesson2.md")
                .sortOrder(2)
                .published(true)
                .build();
        lessonRepository.save(lesson2);

        // 受講登録
        Enrollment enrollment = Enrollment.builder()
                .user(learner)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);
    }

    @Test
    @DisplayName("POST /api/courses/{courseId}/lessons/{lessonId}/complete - レッスン完了成功 (200)")
    void completeLesson_success_returns200() throws Exception {
        mockMvc.perform(post("/api/courses/" + course.getId() + "/lessons/" + lesson1.getId() + "/complete")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("レッスンを完了しました"));
    }

    @Test
    @DisplayName("POST /api/courses/{courseId}/lessons/{lessonId}/complete - 未認証 (401)")
    void completeLesson_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/courses/" + course.getId() + "/lessons/" + lesson1.getId() + "/complete"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/courses/{courseId}/lessons/{lessonId}/complete - 完了取り消し成功 (200)")
    void uncompleteLesson_success_returns200() throws Exception {
        // 先に完了マーク
        LessonProgress progress = LessonProgress.builder()
                .user(learner)
                .lesson(lesson1)
                .completed(true)
                .completedAt(java.time.LocalDateTime.now())
                .build();
        lessonProgressRepository.save(progress);

        mockMvc.perform(delete("/api/courses/" + course.getId() + "/lessons/" + lesson1.getId() + "/complete")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("レッスン完了を取り消しました"));
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/progress - コース進捗取得成功 (200)")
    void getCourseProgress_success_returns200() throws Exception {
        // レッスン1を完了
        LessonProgress progress = LessonProgress.builder()
                .user(learner)
                .lesson(lesson1)
                .completed(true)
                .completedAt(java.time.LocalDateTime.now())
                .build();
        lessonProgressRepository.save(progress);

        mockMvc.perform(get("/api/courses/" + course.getId() + "/progress")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(course.getId()))
                .andExpect(jsonPath("$.totalLessons").value(2))
                .andExpect(jsonPath("$.completedLessons").value(1))
                .andExpect(jsonPath("$.progressPercentage").value(50.0))
                .andExpect(jsonPath("$.lessonProgresses").isArray())
                .andExpect(jsonPath("$.lessonProgresses.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/enrollments/my/progress - 全コース進捗サマリー成功 (200)")
    void getMyProgress_success_returns200() throws Exception {
        mockMvc.perform(get("/api/enrollments/my/progress")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].courseId").value(course.getId()))
                .andExpect(jsonPath("$[0].totalLessons").value(2))
                .andExpect(jsonPath("$[0].completedLessons").value(0));
    }
}
