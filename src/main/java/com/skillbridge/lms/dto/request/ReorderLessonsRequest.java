package com.skillbridge.lms.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReorderLessonsRequest {

    @NotEmpty(message = "レッスンの並び順を指定してください")
    private List<LessonOrderItem> lessonOrders;

    @Getter
    @Setter
    public static class LessonOrderItem {
        private Long lessonId;
        private Integer sortOrder;
    }
}
