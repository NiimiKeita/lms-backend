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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.request.CreateFeedbackRequest;
import com.skillbridge.lms.dto.request.CreateSubmissionRequest;
import com.skillbridge.lms.dto.request.UpdateSubmissionStatusRequest;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.TaskFeedbackResponse;
import com.skillbridge.lms.dto.response.TaskSubmissionResponse;
import com.skillbridge.lms.service.TaskSubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskSubmissionController {

    private final TaskSubmissionService submissionService;

    @PostMapping("/{taskId}/submissions")
    public ResponseEntity<TaskSubmissionResponse> submit(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TaskSubmissionResponse response = submissionService.submit(taskId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{taskId}/submissions/my")
    public ResponseEntity<List<TaskSubmissionResponse>> getMySubmissions(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TaskSubmissionResponse> response = submissionService.getMySubmissions(taskId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<PageResponse<TaskSubmissionResponse>> getSubmissions(
            @PathVariable Long taskId,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<TaskSubmissionResponse> response = submissionService.getSubmissions(taskId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<TaskSubmissionResponse> getSubmission(@PathVariable Long submissionId) {
        TaskSubmissionResponse response = submissionService.getSubmission(submissionId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/submissions/{submissionId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<TaskSubmissionResponse> updateStatus(
            @PathVariable Long submissionId,
            @Valid @RequestBody UpdateSubmissionStatusRequest request) {
        TaskSubmissionResponse response = submissionService.updateStatus(submissionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submissions/{submissionId}/feedback")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<TaskFeedbackResponse> addFeedback(
            @PathVariable Long submissionId,
            @Valid @RequestBody CreateFeedbackRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TaskFeedbackResponse response = submissionService.addFeedback(
                submissionId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
