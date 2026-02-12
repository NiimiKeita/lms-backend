package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.skillbridge.lms.dto.request.LoginRequest;
import com.skillbridge.lms.dto.request.RegisterRequest;
import com.skillbridge.lms.dto.response.AuthResponse;
import com.skillbridge.lms.entity.RefreshToken;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.repository.PasswordResetTokenRepository;
import com.skillbridge.lms.repository.RefreshTokenRepository;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .username("testuser")
                .role(UserRole.LEARNER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("register - 成功")
    void register_success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setUsername("newuser");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });
        when(tokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(tokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("register - メールアドレス重複でエラー")
    void register_duplicateEmail_throwsBadRequestException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setUsername("existinguser");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("このメールアドレスは既に登録されています");
    }

    @Test
    @DisplayName("login - 成功")
    void login_success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("test@example.com", "password123"));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken("test@example.com")).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken("test@example.com")).thenReturn("refresh-token");
        when(tokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login - メールアドレス不正でエラー")
    void login_invalidEmail_throwsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login - パスワード不正でエラー")
    void login_invalidPassword_throwsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("refreshToken - 成功")
    void refreshToken_success() {
        // Given
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token("valid-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token"))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenProvider.generateAccessToken("test@example.com")).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken("test@example.com")).thenReturn("new-refresh-token");
        when(tokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);

        // When
        AuthResponse response = authService.refreshToken("valid-refresh-token");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(refreshToken.getRevoked()).isTrue();
    }

    @Test
    @DisplayName("refreshToken - 無効なトークンでエラー")
    void refreshToken_invalidToken_throwsBadRequestException() {
        // Given
        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("無効なリフレッシュトークンです");
    }
}
