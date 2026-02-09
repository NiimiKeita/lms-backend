package com.skillbridge.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LessonContentResponse {

    private Long lessonId;
    private String title;
    private String content;
    private int orderIndex;
}
