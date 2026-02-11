package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.skillbridge.lms.entity.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private Integer sortOrder;
    private Boolean published;
    private int lessonCount;
    private String thumbnailUrl;
    private List<CategoryResponse> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse from(Course course) {
        List<CategoryResponse> categoryList = null;
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            categoryList = course.getCategories().stream()
                    .map(CategoryResponse::from)
                    .toList();
        }

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .sortOrder(course.getSortOrder())
                .published(course.getPublished())
                .lessonCount(course.getLessons() != null ? course.getLessons().size() : 0)
                .thumbnailUrl(course.getThumbnailUrl())
                .categories(categoryList)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
