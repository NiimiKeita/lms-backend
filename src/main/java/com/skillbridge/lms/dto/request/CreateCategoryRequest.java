package com.skillbridge.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank(message = "カテゴリ名を入力してください")
    @Size(max = 100, message = "カテゴリ名は100文字以内で入力してください")
    private String name;

    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;
}
