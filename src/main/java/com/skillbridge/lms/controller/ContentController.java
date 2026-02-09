package com.skillbridge.lms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.response.LessonContentResponse;
import com.skillbridge.lms.service.ContentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/{lessonId}/content")
    public ResponseEntity<LessonContentResponse> getContent(
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        LessonContentResponse response = contentService.getContent(courseId, lessonId);
        return ResponseEntity.ok(response);
    }
}
