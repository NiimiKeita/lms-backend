package com.skillbridge.lms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.skillbridge.lms.dto.request.CreateLessonRequest;
import com.skillbridge.lms.dto.request.ReorderLessonsRequest;
import com.skillbridge.lms.dto.request.UpdateLessonRequest;
import com.skillbridge.lms.dto.response.LessonResponse;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.service.LessonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "レッスン管理 API")
public class LessonController {

    private final LessonService lessonService;

    /**
     * レッスン一覧取得
     * ADMIN: 全レッスン表示 / LEARNER: publishedのみ
     */
    @GetMapping
    public ResponseEntity<List<LessonResponse>> getLessons(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = hasAdminRole(userDetails);
        List<LessonResponse> response = lessonService.getLessons(courseId, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * レッスン詳細取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLesson(
            @PathVariable Long courseId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = hasAdminRole(userDetails);
        LessonResponse response = lessonService.getLesson(courseId, id, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * レッスン新規作成 (ADMINのみ)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateLessonRequest request) {

        LessonResponse response = lessonService.createLesson(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * レッスン更新 (ADMINのみ)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request) {

        LessonResponse response = lessonService.updateLesson(courseId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * レッスン削除 (ADMINのみ)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteLesson(
            @PathVariable Long courseId,
            @PathVariable Long id) {

        lessonService.deleteLesson(courseId, id);
        return ResponseEntity.ok(new MessageResponse("レッスンを削除しました"));
    }

    /**
     * レッスン並び替え (ADMINのみ)
     */
    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LessonResponse>> reorderLessons(
            @PathVariable Long courseId,
            @Valid @RequestBody ReorderLessonsRequest request) {

        List<LessonResponse> response = lessonService.reorderLessons(courseId, request);
        return ResponseEntity.ok(response);
    }

    private boolean hasAdminRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
