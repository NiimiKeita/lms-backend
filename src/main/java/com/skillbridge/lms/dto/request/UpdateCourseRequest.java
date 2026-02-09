package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCourseRequest {

    @NotBlank(message = "コースタイトルを入力してください")
    @Size(max = 255, message = "コースタイトルは255文字以内で入力してください")
    private String title;

    @Size(max = 5000, message = "説明は5000文字以内で入力してください")
    private String description;
}
