package com.skillbridge.lms.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.response.CourseProgressResponse;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.service.ProgressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * レッスン完了マーク
     */
    @PostMapping("/api/courses/{courseId}/lessons/{lessonId}/complete")
    public ResponseEntity<MessageResponse> completeLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse response = progressService.completeLesson(courseId, lessonId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * レッスン完了取り消し
     */
    @DeleteMapping("/api/courses/{courseId}/lessons/{lessonId}/complete")
    public ResponseEntity<MessageResponse> uncompleteLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse response = progressService.uncompleteLesson(courseId, lessonId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * コース進捗取得
     */
    @GetMapping("/api/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        CourseProgressResponse response = progressService.getCourseProgress(courseId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 受講中全コースの進捗サマリー
     */
    @GetMapping("/api/enrollments/my/progress")
    public ResponseEntity<List<CourseProgressResponse>> getMyProgress(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<CourseProgressResponse> response = progressService.getMyProgress(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
