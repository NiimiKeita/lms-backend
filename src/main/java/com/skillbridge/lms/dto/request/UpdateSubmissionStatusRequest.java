package com.skillbridge.lms.dto.request;

import com.skillbridge.lms.enums.SubmissionStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubmissionStatusRequest {

    @NotNull(message = "ステータスを指定してください")
    private SubmissionStatus status;
}
