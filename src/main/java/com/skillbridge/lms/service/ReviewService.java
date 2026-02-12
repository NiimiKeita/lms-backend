package com.skillbridge.lms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.request.CreateReviewRequest;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.ReviewResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Review;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.ReviewRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(Long courseId, CreateReviewRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Course course = findCourseById(courseId);

        if (reviewRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new BadRequestException("このコースには既にレビューを投稿しています");
        }

        Review review = Review.builder()
                .user(user)
                .course(course)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviews(Long courseId, Pageable pageable) {
        findCourseById(courseId);
        Page<Review> page = reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId, pageable);
        var content = page.getContent().stream()
                .map(ReviewResponse::from)
                .toList();
        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getMyReview(Long courseId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Review review = reviewRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("レビューが見つかりません"));
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateMyReview(Long courseId, CreateReviewRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Review review = reviewRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("レビューが見つかりません"));

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review = reviewRepository.save(review);
        return ReviewResponse.from(review);
    }

    @Transactional
    public MessageResponse deleteMyReview(Long courseId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Review review = reviewRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("レビューが見つかりません"));
        reviewRepository.delete(review);
        return new MessageResponse("レビューを削除しました");
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long courseId) {
        return reviewRepository.findAverageRatingByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public long getReviewCount(Long courseId) {
        return reviewRepository.countByCourseId(courseId);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
    }

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("コースが見つかりません: " + courseId));
    }
}
