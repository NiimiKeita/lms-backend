package com.skillbridge.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionStatsResponse {

    private String courseTitle;
    private long totalEnrollments;
    private long completedEnrollments;
    private double completionRate;
}
