package com.skillbridge.lms.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

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
                .email("sec-admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("secadmin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        adminToken = tokenProvider.generateAccessToken("sec-admin@example.com");

        // Create LEARNER user
        User learner = User.builder()
                .email("sec-learner@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("seclearner")
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();
        userRepository.save(learner);
        learnerToken = tokenProvider.generateAccessToken("sec-learner@example.com");
    }

    @Test
    @DisplayName("認証不要エンドポイント (/api/auth/register) にアクセス可能")
    void publicEndpoint_register_accessible() throws Exception {
        String requestBody = """
                {
                    "username": "sectest",
                    "email": "sectest@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("認証不要エンドポイント (/api/auth/login) にアクセス可能")
    void publicEndpoint_login_accessible() throws Exception {
        // Login with non-existent user will return 401 (not a security filter block, but auth failure)
        // The point is the endpoint is reachable (not blocked by security filter)
        String requestBody = """
                {
                    "email": "nobody@example.com",
                    "password": "password123"
                }
                """;

        // The endpoint is reachable - returns 401 from AuthenticationManager, not from security filter
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("認証不要エンドポイント (/api/auth/forgot-password) にアクセス可能")
    void publicEndpoint_forgotPassword_accessible() throws Exception {
        String requestBody = """
                {
                    "email": "any@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("認証必要エンドポイント (/api/courses) に未認証でアクセス不可 (401)")
    void protectedEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN限定エンドポイントにLEARNERでアクセス不可 (403)")
    void adminEndpoint_learner_returns403() throws Exception {
        String requestBody = """
                {
                    "title": "Test Course",
                    "description": "Test"
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CORSヘッダーの確認")
    void cors_preflightRequest_returnsCorrectHeaders() throws Exception {
        mockMvc.perform(options("/api/courses")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
