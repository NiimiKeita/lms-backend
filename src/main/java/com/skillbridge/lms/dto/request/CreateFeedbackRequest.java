package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFeedbackRequest {

    @NotBlank(message = "コメントを入力してください")
    @Size(max = 5000, message = "コメントは5000文字以内で入力してください")
    private String comment;
}
