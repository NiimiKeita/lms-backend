package com.skillbridge.lms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.response.InstructorDashboardResponse;
import com.skillbridge.lms.dto.response.LearnerDashboardResponse;
import com.skillbridge.lms.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "ダッシュボード API")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "受講者ダッシュボード取得")
    @GetMapping("/learner")
    public ResponseEntity<LearnerDashboardResponse> getLearnerDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        LearnerDashboardResponse response = dashboardService.getLearnerDashboard(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "講師ダッシュボード取得")
    @GetMapping("/instructor")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<InstructorDashboardResponse> getInstructorDashboard() {
        InstructorDashboardResponse response = dashboardService.getInstructorDashboard();
        return ResponseEntity.ok(response);
    }
}
