package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.skillbridge.lms.dto.response.CertificateResponse;
import com.skillbridge.lms.entity.Certificate;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CertificateRepository;
import com.skillbridge.lms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CertificateService certificateService;

    private User user;
    private Course course;
    private Certificate certificate;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").username("testuser").build();
        course = Course.builder().id(1L).title("Test Course").build();
        certificate = Certificate.builder()
                .id(1L)
                .user(user)
                .course(course)
                .certificateNumber(UUID.randomUUID().toString())
                .issuedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getMyCertificates - 証明書一覧取得")
    void getMyCertificates_returnsList() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(certificateRepository.findByUserIdOrderByIssuedAtDesc(1L)).thenReturn(List.of(certificate));

        List<CertificateResponse> result = certificateService.getMyCertificates("test@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseTitle()).isEqualTo("Test Course");
    }

    @Test
    @DisplayName("getCertificate - 証明書詳細取得")
    void getCertificate_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

        CertificateResponse result = certificateService.getCertificate(1L, "test@example.com");

        assertThat(result.getCourseTitle()).isEqualTo("Test Course");
    }

    @Test
    @DisplayName("getCertificate - 他ユーザーの証明書はアクセス不可")
    void getCertificate_otherUser_throwsException() {
        User otherUser = User.builder().id(2L).email("other@example.com").username("other").build();
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

        assertThatThrownBy(() -> certificateService.getCertificate(1L, "other@example.com"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("issueCertificate - 新規発行")
    void issueCertificate_new() {
        when(certificateRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(false);
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        Certificate result = certificateService.issueCertificate(user, course);

        assertThat(result).isNotNull();
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    @DisplayName("issueCertificate - 既に発行済みの場合は既存を返す")
    void issueCertificate_alreadyExists() {
        when(certificateRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(true);
        when(certificateRepository.findByUserIdAndCourseId(1L, 1L)).thenReturn(Optional.of(certificate));

        Certificate result = certificateService.issueCertificate(user, course);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("generatePdf - PDF生成成功")
    void generatePdf_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

        byte[] pdf = certificateService.generatePdf(1L, "test@example.com");

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
        // PDF starts with %PDF
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
