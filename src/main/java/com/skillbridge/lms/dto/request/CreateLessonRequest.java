package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLessonRequest {

    @NotBlank(message = "レッスンタイトルを入力してください")
    @Size(max = 255, message = "レッスンタイトルは255文字以内で入力してください")
    private String title;

    @NotBlank(message = "コンテンツパスを入力してください")
    @Size(max = 500, message = "コンテンツパスは500文字以内で入力してください")
    private String contentPath;

    private Integer sortOrder;

    private Boolean published = false;
}
