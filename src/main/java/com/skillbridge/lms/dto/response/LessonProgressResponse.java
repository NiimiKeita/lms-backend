package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.LessonProgress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LessonProgressResponse {

    private Long lessonId;
    private String lessonTitle;
    private Integer sortOrder;
    private boolean completed;
    private LocalDateTime completedAt;

    public static LessonProgressResponse from(LessonProgress progress) {
        return LessonProgressResponse.builder()
                .lessonId(progress.getLesson().getId())
                .lessonTitle(progress.getLesson().getTitle())
                .sortOrder(progress.getLesson().getSortOrder())
                .completed(progress.getCompleted())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}
