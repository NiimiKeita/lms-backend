package com.skillbridge.lms.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class InstructorDashboardResponse {

    private long unreviewedCount;
    private List<RecentSubmissionItem> recentSubmissions;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class RecentSubmissionItem {
        private Long submissionId;
        private String taskTitle;
        private String learnerName;
        private String status;
        private String submittedAt;
    }
}
