package com.skillbridge.lms.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CourseProgressResponse {

    private Long courseId;
    private String courseTitle;
    private int totalLessons;
    private int completedLessons;
    private double progressPercentage;
    private List<LessonProgressResponse> lessonProgresses;

    public static CourseProgressResponse of(
            Long courseId,
            String courseTitle,
            int totalLessons,
            int completedLessons,
            List<LessonProgressResponse> lessonProgresses) {

        double percentage = totalLessons > 0
                ? (double) completedLessons / totalLessons * 100.0
                : 0.0;

        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseTitle(courseTitle)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .progressPercentage(Math.round(percentage * 10.0) / 10.0)
                .lessonProgresses(lessonProgresses)
                .build();
    }
}
