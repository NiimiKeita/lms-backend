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
    private Double averageRating;
    private Long reviewCount;
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

    public static CourseResponse from(Course course, Double averageRating, Long reviewCount) {
        CourseResponse response = from(course);
        return CourseResponse.builder()
                .id(response.getId())
                .title(response.getTitle())
                .description(response.getDescription())
                .sortOrder(response.getSortOrder())
                .published(response.getPublished())
                .lessonCount(response.getLessonCount())
                .thumbnailUrl(response.getThumbnailUrl())
                .categories(response.getCategories())
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
