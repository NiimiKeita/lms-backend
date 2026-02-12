package com.skillbridge.lms.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.request.CreateReviewRequest;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.ReviewResponse;
import com.skillbridge.lms.service.ReviewService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "コースレビュー API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(courseId, request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReviewResponse>> getReviews(
            @PathVariable Long courseId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviews(courseId, pageable));
    }

    @GetMapping("/my")
    public ResponseEntity<ReviewResponse> getMyReview(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.getMyReview(courseId, userDetails.getUsername()));
    }

    @PutMapping("/my")
    public ResponseEntity<ReviewResponse> updateMyReview(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.updateMyReview(courseId, request, userDetails.getUsername()));
    }

    @DeleteMapping("/my")
    public ResponseEntity<MessageResponse> deleteMyReview(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.deleteMyReview(courseId, userDetails.getUsername()));
    }
}
