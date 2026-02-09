package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "トークンを入力してください")
    private String token;

    @NotBlank(message = "新しいパスワードを入力してください")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String newPassword;
}
