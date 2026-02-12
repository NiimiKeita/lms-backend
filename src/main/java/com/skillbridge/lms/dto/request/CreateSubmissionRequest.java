package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubmissionRequest {

    @NotBlank(message = "GitHub URLを入力してください")
    @Size(max = 500, message = "URLは500文字以内で入力してください")
    private String githubUrl;
}
