package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserCourseProgressResponse {

    private Long courseId;
    private String courseTitle;
    private int completedLessons;
    private int totalLessons;
    private double progressPercentage;
    private LocalDateTime enrolledAt;
}
