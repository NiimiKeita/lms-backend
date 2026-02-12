package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .courseId(task.getCourse().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .sortOrder(task.getSortOrder())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
