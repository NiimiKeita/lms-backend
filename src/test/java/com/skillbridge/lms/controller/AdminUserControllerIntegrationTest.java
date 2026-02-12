package com.skillbridge.lms.controller;

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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.lms.dto.request.AdminCreateUserRequest;
import com.skillbridge.lms.dto.request.AdminUpdateUserRequest;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String learnerToken;
    private User admin;
    private User learner;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .username("Admin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        adminToken = tokenProvider.generateAccessToken("admin@test.com");

        learner = User.builder()
                .email("learner@test.com")
                .password(passwordEncoder.encode("password123"))
                .username("Learner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();
        userRepository.save(learner);
        learnerToken = tokenProvider.generateAccessToken("learner@test.com");
    }

    // ===== GET /api/admin/users =====

    @Test
    @DisplayName("GET /api/admin/users - ADMIN: ユーザー一覧取得成功 (200)")
    void getUsers_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET /api/admin/users - LEARNER: 403 Forbidden")
    void getUsers_asLearner_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/users?keyword=admin - キーワード検索成功")
    void getUsers_withKeyword_filtersResults() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", "admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("admin@test.com"));
    }

    // ===== GET /api/admin/users/{id} =====

    @Test
    @DisplayName("GET /api/admin/users/{id} - ユーザー詳細取得成功 (200)")
    void getUser_existingId_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + learner.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("learner@test.com"))
                .andExpect(jsonPath("$.name").value("Learner"));
    }

    @Test
    @DisplayName("GET /api/admin/users/{id} - 存在しないID (404)")
    void getUser_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/admin/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ===== POST /api/admin/users =====

    @Test
    @DisplayName("POST /api/admin/users - ユーザー作成成功 (201)")
    void createUser_validRequest_returns201() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setName("New User");
        request.setRole("LEARNER");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.role").value("LEARNER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("POST /api/admin/users - メール重複 (400)")
    void createUser_duplicateEmail_returns400() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password123");
        request.setName("Duplicate");
        request.setRole("LEARNER");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/admin/users - バリデーションエラー (400)")
    void createUser_invalidRequest_returns400() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("invalid-email");
        request.setPassword("short");
        request.setName("");
        request.setRole("INVALID");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== PUT /api/admin/users/{id} =====

    @Test
    @DisplayName("PUT /api/admin/users/{id} - ユーザー更新成功 (200)")
    void updateUser_validRequest_returns200() throws Exception {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setName("Updated Name");
        request.setRole("INSTRUCTOR");

        mockMvc.perform(put("/api/admin/users/" + learner.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.role").value("INSTRUCTOR"));
    }

    // ===== PATCH /api/admin/users/{id}/toggle-enabled =====

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/toggle-enabled - 有効→無効切替成功 (200)")
    void toggleEnabled_enabledUser_returns200disabled() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + learner.getId() + "/toggle-enabled")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/toggle-enabled - 自分自身の無効化 (400)")
    void toggleEnabled_selfDisable_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + admin.getId() + "/toggle-enabled")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
