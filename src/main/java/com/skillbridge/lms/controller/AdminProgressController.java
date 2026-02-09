package com.skillbridge.lms.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.response.AdminStatsResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.UserCourseProgressResponse;
import com.skillbridge.lms.dto.response.UserProgressSummaryResponse;
import com.skillbridge.lms.service.AdminProgressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProgressController {

    private final AdminProgressService adminProgressService;

    @GetMapping("/progress")
    public ResponseEntity<PageResponse<UserProgressSummaryResponse>> getProgress(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<UserProgressSummaryResponse> response = adminProgressService.getUserProgressSummaries(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/progress")
    public ResponseEntity<List<UserCourseProgressResponse>> getUserProgress(@PathVariable Long userId) {
        List<UserCourseProgressResponse> response = adminProgressService.getUserCourseProgress(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse response = adminProgressService.getStats();
        return ResponseEntity.ok(response);
    }
}
