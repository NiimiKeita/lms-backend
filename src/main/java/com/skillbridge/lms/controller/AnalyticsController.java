package com.skillbridge.lms.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.response.CompletionStatsResponse;
import com.skillbridge.lms.dto.response.EnrollmentTrendResponse;
import com.skillbridge.lms.dto.response.PopularCourseResponse;
import com.skillbridge.lms.service.AnalyticsService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Analytics", description = "分析ダッシュボード API (ADMIN)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/enrollments")
    public ResponseEntity<List<EnrollmentTrendResponse>> getEnrollmentTrends(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(analyticsService.getEnrollmentTrends(period));
    }

    @GetMapping("/completions")
    public ResponseEntity<List<CompletionStatsResponse>> getCompletionStats() {
        return ResponseEntity.ok(analyticsService.getCompletionStats());
    }

    @GetMapping("/popular-courses")
    public ResponseEntity<List<PopularCourseResponse>> getPopularCourses() {
        return ResponseEntity.ok(analyticsService.getPopularCourses());
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = analyticsService.exportCsv();
        byte[] csvBytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics-export.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
