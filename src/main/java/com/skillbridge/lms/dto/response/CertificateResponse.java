package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.Certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponse {

    private Long id;
    private String certificateNumber;
    private String courseTitle;
    private Long courseId;
    private String userName;
    private LocalDateTime issuedAt;

    public static CertificateResponse from(Certificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .courseTitle(certificate.getCourse().getTitle())
                .courseId(certificate.getCourse().getId())
                .userName(certificate.getUser().getUsername())
                .issuedAt(certificate.getIssuedAt())
                .build();
    }
}
