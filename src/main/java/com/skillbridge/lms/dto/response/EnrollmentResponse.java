package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.enums.EnrollmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long courseId;
    private String courseTitle;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;

    public static EnrollmentResponse from(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .username(enrollment.getUser().getUsername())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .build();
    }
}
