package com.skillbridge.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserProgressSummaryResponse {

    private Long userId;
    private String userName;
    private String email;
    private int enrolledCourses;
    private int completedCourses;
    private double averageProgress;
}
