package com.skillbridge.lms.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LearnerDashboardResponse {

    private List<CourseProgressResponse> enrolledCourses;
    private List<PendingTaskItem> pendingTasks;
    private List<RecentFeedbackItem> recentFeedbacks;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PendingTaskItem {
        private Long taskId;
        private String taskTitle;
        private Long courseId;
        private String courseTitle;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class RecentFeedbackItem {
        private Long submissionId;
        private String taskTitle;
        private String reviewerName;
        private String comment;
        private String createdAt;
    }
}
