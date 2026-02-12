package com.skillbridge.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AdminStatsResponse {

    private long totalUsers;
    private long totalCourses;
    private long totalEnrollments;
    private double averageCompletionRate;
}
