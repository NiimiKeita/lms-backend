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
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String adminToken;
    private String learnerToken;
    private Long courseId;

    @BeforeEach
    void setUp() {
        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("admin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        adminToken = tokenProvider.generateAccessToken("admin@example.com");

        User learner = User.builder()
                .email("learner@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();
        userRepository.save(learner);
        learnerToken = tokenProvider.generateAccessToken("learner@example.com");

        Course course = Course.builder()
                .title("Test Course")
                .description("Test Description")
                .published(true)
                .sortOrder(0)
                .build();
        course = courseRepository.save(course);
        courseId = course.getId();
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/tasks - 認証済みで成功")
    void getTasks_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/courses/{courseId}/tasks - 未認証で401")
    void getTasks_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/courses/" + courseId + "/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/courses/{courseId}/tasks - ADMINで成功 (201)")
    void createTask_admin_returns201() throws Exception {
        String body = """
                {
                    "title": "New Task",
                    "description": "Task Description"
                }
                """;

        mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Task Description"))
                .andExpect(jsonPath("$.courseId").value(courseId));
    }

    @Test
    @DisplayName("POST /api/courses/{courseId}/tasks - LEARNERで403")
    void createTask_learner_returns403() throws Exception {
        String body = """
                {
                    "title": "New Task",
                    "description": "Task Description"
                }
                """;

        mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/courses/{courseId}/tasks/{taskId} - ADMINで成功")
    void updateTask_admin_returnsOk() throws Exception {
        // Create task first
        String createBody = """
                {
                    "title": "Original",
                    "description": "Original Desc"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createResponse = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long taskId = createResponse.get("id").asLong();

        // Update task
        String updateBody = """
                {
                    "title": "Updated Task",
                    "description": "Updated Description"
                }
                """;

        mockMvc.perform(put("/api/courses/" + courseId + "/tasks/" + taskId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @DisplayName("DELETE /api/courses/{courseId}/tasks/{taskId} - ADMINで成功")
    void deleteTask_admin_returnsOk() throws Exception {
        // Create task first
        String createBody = """
                {
                    "title": "To Delete",
                    "description": "Delete Me"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createResponse = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long taskId = createResponse.get("id").asLong();

        mockMvc.perform(delete("/api/courses/" + courseId + "/tasks/" + taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("課題を削除しました"));
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/submissions - 提出成功 (201)")
    void submitTask_learner_returns201() throws Exception {
        // Create task first
        String taskBody = """
                {
                    "title": "Submit Task",
                    "description": "Submit Description"
                }
                """;

        MvcResult taskResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode taskResponse = objectMapper.readTree(taskResult.getResponse().getContentAsString());
        Long taskId = taskResponse.get("id").asLong();

        // Submit
        String submitBody = """
                {
                    "githubUrl": "https://github.com/test/repo"
                }
                """;

        mockMvc.perform(post("/api/tasks/" + taskId + "/submissions")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.githubUrl").value("https://github.com/test/repo"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/submissions/my - 自分の提出一覧")
    void getMySubmissions_returns200() throws Exception {
        // Create task
        String taskBody = """
                { "title": "My Task" }
                """;

        MvcResult taskResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode taskResponse = objectMapper.readTree(taskResult.getResponse().getContentAsString());
        Long taskId = taskResponse.get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + taskId + "/submissions/my")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/submissions - ADMIN提出一覧(ページネーション)")
    void getSubmissions_admin_returns200() throws Exception {
        String taskBody = """
                { "title": "Admin Task" }
                """;

        MvcResult taskResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode taskResponse = objectMapper.readTree(taskResult.getResponse().getContentAsString());
        Long taskId = taskResponse.get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + taskId + "/submissions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/submissions - LEARNERで403")
    void getSubmissions_learner_returns403() throws Exception {
        String taskBody = """
                { "title": "Forbidden Task" }
                """;

        MvcResult taskResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode taskResponse = objectMapper.readTree(taskResult.getResponse().getContentAsString());
        Long taskId = taskResponse.get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + taskId + "/submissions")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/tasks/submissions/{id}/status - ADMINでステータス変更")
    void updateStatus_admin_returnsOk() throws Exception {
        // Create task + submission
        String taskBody = """
                { "title": "Status Task" }
                """;

        MvcResult taskResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        String submitBody = """
                { "githubUrl": "https://github.com/test/repo" }
                """;

        MvcResult submitResult = mockMvc.perform(post("/api/tasks/" + taskId + "/submissions")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long submissionId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("id").asLong();

        // Update status
        String statusBody = """
                { "status": "APPROVED" }
                """;

        mockMvc.perform(patch("/api/tasks/submissions/" + submissionId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/tasks/submissions/{id}/feedback - ADMINでフィードバック追加")
    void addFeedback_admin_returns201() throws Exception {
        // Create task + submission
        String taskBody = """
                { "title": "Feedback Task" }
                """;

        MvcResult taskResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        String submitBody = """
                { "githubUrl": "https://github.com/test/feedback" }
                """;

        MvcResult submitResult = mockMvc.perform(post("/api/tasks/" + taskId + "/submissions")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long submissionId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("id").asLong();

        // Add feedback
        String feedbackBody = """
                { "comment": "Great work!" }
                """;

        mockMvc.perform(post("/api/tasks/submissions/" + submissionId + "/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Great work!"))
                .andExpect(jsonPath("$.reviewerName").value("admin"));
    }

    @Test
    @DisplayName("GET /api/tasks/submissions/{id} - 提出詳細取得")
    void getSubmission_returns200() throws Exception {
        // Create task + submission
        String taskBody = """
                { "title": "Detail Task" }
                """;

        MvcResult taskResult = mockMvc.perform(post("/api/courses/" + courseId + "/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        String submitBody = """
                { "githubUrl": "https://github.com/test/detail" }
                """;

        MvcResult submitResult = mockMvc.perform(post("/api/tasks/" + taskId + "/submissions")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long submissionId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/tasks/submissions/" + submissionId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId))
                .andExpect(jsonPath("$.feedbacks").isArray());
    }
}
