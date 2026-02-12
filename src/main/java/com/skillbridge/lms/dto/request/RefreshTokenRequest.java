package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "リフレッシュトークンを入力してください")
    private String refreshToken;
}
