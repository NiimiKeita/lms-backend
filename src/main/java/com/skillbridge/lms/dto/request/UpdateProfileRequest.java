package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "ユーザー名を入力してください")
    @Size(max = 100, message = "ユーザー名は100文字以内で入力してください")
    private String username;
}
