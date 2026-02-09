package com.skillbridge.lms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String adminToken;
    private String learnerToken;

    @BeforeEach
    void setUp() {
        // Create ADMIN user
        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("admin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        adminToken = tokenProvider.generateAccessToken("admin@example.com");

        // Create LEARNER user
        User learner = User.builder()
                .email("learner@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();
        userRepository.save(learner);
        learnerToken = tokenProvider.generateAccessToken("learner@example.com");
    }

    @Test
    @DisplayName("GET /api/courses - 認証済みで成功 (200)")
    void getCourses_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/courses - 未認証で401")
    void getCourses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/courses - ADMINで成功 (201)")
    void createCourse_admin_returns201() throws Exception {
        String requestBody = """
                {
                    "title": "New Course",
                    "description": "Course Description"
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Course"))
                .andExpect(jsonPath("$.description").value("Course Description"))
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    @DisplayName("POST /api/courses - LEARNERで403")
    void createCourse_learner_returns403() throws Exception {
        String requestBody = """
                {
                    "title": "New Course",
                    "description": "Course Description"
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/courses/{id} - ADMINで成功")
    void updateCourse_admin_returnsOk() throws Exception {
        // First create a course
        String createBody = """
                {
                    "title": "Original Title",
                    "description": "Original Description"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createResponse = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long courseId = createResponse.get("id").asLong();

        // Then update it
        String updateBody = """
                {
                    "title": "Updated Title",
                    "description": "Updated Description"
                }
                """;

        mockMvc.perform(put("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - ADMINで成功")
    void deleteCourse_admin_returnsOk() throws Exception {
        // First create a course
        String createBody = """
                {
                    "title": "To Be Deleted",
                    "description": "Delete Me"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createResponse = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long courseId = createResponse.get("id").asLong();

        // Then delete it
        mockMvc.perform(delete("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("コースを削除しました"));
    }

    @Test
    @DisplayName("PATCH /api/courses/{id}/publish - ADMINで成功")
    void togglePublish_admin_returnsOk() throws Exception {
        // First create a course (default: published=false)
        String createBody = """
                {
                    "title": "Publish Test Course",
                    "description": "Test Description"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createResponse = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long courseId = createResponse.get("id").asLong();

        // Toggle publish (false -> true)
        mockMvc.perform(patch("/api/courses/" + courseId + "/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));
    }
}
