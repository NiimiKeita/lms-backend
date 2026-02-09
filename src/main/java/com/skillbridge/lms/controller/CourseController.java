package com.skillbridge.lms.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.request.CreateCourseRequest;
import com.skillbridge.lms.dto.request.UpdateCourseRequest;
import com.skillbridge.lms.dto.response.CourseResponse;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * コース一覧取得
     * ADMIN: 全コース表示 / LEARNER: publishedのみ
     */
    @GetMapping
    public ResponseEntity<PageResponse<CourseResponse>> getCourses(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = hasAdminRole(userDetails);
        PageResponse<CourseResponse> response = courseService.getCourses(keyword, isAdmin, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * コース詳細取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = hasAdminRole(userDetails);
        CourseResponse response = courseService.getCourse(id, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * コース新規作成 (ADMINのみ)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {

        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * コース更新 (ADMINのみ)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {

        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * コース削除 (ADMINのみ)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(new MessageResponse("コースを削除しました"));
    }

    /**
     * コース公開/非公開切替 (ADMINのみ)
     */
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> togglePublish(@PathVariable Long id) {
        CourseResponse response = courseService.togglePublish(id);
        return ResponseEntity.ok(response);
    }

    private boolean hasAdminRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
