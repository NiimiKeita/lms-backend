package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {

    @NotNull(message = "評価を入力してください")
    @Min(value = 1, message = "評価は1以上で入力してください")
    @Max(value = 5, message = "評価は5以下で入力してください")
    private Integer rating;

    @Size(max = 2000, message = "コメントは2000文字以内で入力してください")
    private String comment;
}
