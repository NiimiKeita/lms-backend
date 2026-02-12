package com.skillbridge.lms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register - 成功 (201)")
    void register_success_returns201() throws Exception {
        String requestBody = """
                {
                    "username": "testuser",
                    "email": "register-test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("register-test@example.com"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.role").value("LEARNER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - メール重複 (400)")
    void register_duplicateEmail_returns400() throws Exception {
        // First registration
        String requestBody = """
                {
                    "username": "firstuser",
                    "email": "duplicate@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // Duplicate registration
        String duplicateRequest = """
                {
                    "username": "seconduser",
                    "email": "duplicate@example.com",
                    "password": "password456"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("このメールアドレスは既に登録されています"));
    }

    @Test
    @DisplayName("POST /api/auth/register - バリデーションエラー (400)")
    void register_validationError_returns400() throws Exception {
        String requestBody = """
                {
                    "username": "",
                    "email": "invalid-email",
                    "password": "short"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login - 成功 (200)")
    void login_success_returns200() throws Exception {
        // First register a user
        String registerRequest = """
                {
                    "username": "loginuser",
                    "email": "login-test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        // Then login
        String loginRequest = """
                {
                    "email": "login-test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("login-test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 認証失敗 (401)")
    void login_invalidCredentials_returns401() throws Exception {
        String loginRequest = """
                {
                    "email": "nonexistent@example.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }
}
