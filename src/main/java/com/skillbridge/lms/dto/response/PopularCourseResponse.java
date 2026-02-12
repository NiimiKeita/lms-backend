package com.skillbridge.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularCourseResponse {

    private Long courseId;
    private String courseTitle;
    private long enrollmentCount;
    private Double averageRating;
}
