package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskRequest {

    @NotBlank(message = "課題タイトルを入力してください")
    @Size(max = 255, message = "課題タイトルは255文字以内で入力してください")
    private String title;

    @Size(max = 5000, message = "説明は5000文字以内で入力してください")
    private String description;
}
