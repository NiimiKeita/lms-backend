package com.skillbridge.lms.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.response.EnrollmentResponse;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.service.EnrollmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * コース受講登録
     */
    @PostMapping("/api/courses/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        EnrollmentResponse response = enrollmentService.enroll(courseId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 受講取り消し
     */
    @DeleteMapping("/api/courses/{courseId}/enroll")
    public ResponseEntity<MessageResponse> unenroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        enrollmentService.unenroll(courseId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("受講登録を取り消しました"));
    }

    /**
     * 受講状態確認
     */
    @GetMapping("/api/courses/{courseId}/enrollment")
    public ResponseEntity<EnrollmentResponse> getEnrollment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        EnrollmentResponse response = enrollmentService.getEnrollment(courseId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 自分の受講コース一覧
     */
    @GetMapping("/api/enrollments/my")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<EnrollmentResponse> response = enrollmentService.getMyEnrollments(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * コースの受講者一覧 (ADMINのみ)
     */
    @GetMapping("/api/courses/{courseId}/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<EnrollmentResponse>> getCourseEnrollments(
            @PathVariable Long courseId,
            @PageableDefault(size = 20, sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<EnrollmentResponse> response = enrollmentService.getCourseEnrollments(courseId, pageable);
        return ResponseEntity.ok(response);
    }
}
