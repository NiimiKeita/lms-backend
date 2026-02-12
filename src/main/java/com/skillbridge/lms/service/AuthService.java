package com.skillbridge.lms.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.request.ForgotPasswordRequest;
import com.skillbridge.lms.dto.request.LoginRequest;
import com.skillbridge.lms.dto.request.RegisterRequest;
import com.skillbridge.lms.dto.request.ResetPasswordRequest;
import com.skillbridge.lms.dto.response.AuthResponse;
import com.skillbridge.lms.dto.response.UserResponse;
import com.skillbridge.lms.entity.PasswordResetToken;
import com.skillbridge.lms.entity.RefreshToken;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.PasswordResetTokenRepository;
import com.skillbridge.lms.repository.RefreshTokenRepository;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("このメールアドレスは既に登録されています");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .role(UserRole.LEARNER)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        return createAuthResponse(user);
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BadRequestException("無効なリフレッシュトークンです"));

        if (refreshToken.getRevoked()) {
            throw new BadRequestException("リフレッシュトークンは無効化されています");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("リフレッシュトークンの有効期限が切れています");
        }

        // Revoke old token (token rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return createAuthResponse(refreshToken.getUser());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            mailService.sendPasswordResetEmail(user.getEmail(), token);
        });
        // Always return success to prevent email enumeration
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("無効なトークンです"));

        if (resetToken.getUsed()) {
            throw new BadRequestException("このトークンは既に使用されています");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("トークンの有効期限が切れています");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        return UserResponse.from(user);
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshTokenStr = tokenProvider.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plus(
                        tokenProvider.getRefreshTokenExpiration(), ChronoUnit.MILLIS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .user(UserResponse.from(user))
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .build();
    }
}
