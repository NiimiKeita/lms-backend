package com.skillbridge.lms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.CourseProgressResponse;
import com.skillbridge.lms.dto.response.LessonProgressResponse;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.Lesson;
import com.skillbridge.lms.entity.LessonProgress;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.LessonProgressRepository;
import com.skillbridge.lms.repository.LessonRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final NotificationService notificationService;

    /**
     * レッスン完了マーク
     */
    @Transactional
    public MessageResponse completeLesson(Long courseId, Long lessonId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Course course = findCourseById(courseId);

        // 受講登録チェック
        validateEnrollment(user.getId(), courseId);

        // レッスン存在チェック
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("レッスンが見つかりません: " + lessonId));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("レッスンが見つかりません: " + lessonId);
        }

        // 既に完了済みの場合
        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .orElse(null);

        if (progress != null && progress.getCompleted()) {
            return new MessageResponse("このレッスンは既に完了済みです");
        }

        if (progress == null) {
            progress = LessonProgress.builder()
                    .user(user)
                    .lesson(lesson)
                    .completed(true)
                    .completedAt(LocalDateTime.now())
                    .build();
        } else {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        lessonProgressRepository.save(progress);

        // コース完了チェック
        checkCourseCompletion(user.getId(), course);

        return new MessageResponse("レッスンを完了しました");
    }

    /**
     * レッスン完了取り消し
     */
    @Transactional
    public MessageResponse uncompleteLesson(Long courseId, Long lessonId, String userEmail) {
        User user = findUserByEmail(userEmail);
        findCourseById(courseId);

        // 受講登録チェック
        validateEnrollment(user.getId(), courseId);

        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("進捗記録が見つかりません"));

        progress.setCompleted(false);
        progress.setCompletedAt(null);
        lessonProgressRepository.save(progress);

        // コース完了状態をACTIVEに戻す
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElse(null);
        if (enrollment != null && enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setCompletedAt(null);
            enrollmentRepository.save(enrollment);
        }

        return new MessageResponse("レッスン完了を取り消しました");
    }

    /**
     * コース進捗取得
     */
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(Long courseId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Course course = findCourseById(courseId);

        // 受講登録チェック
        validateEnrollment(user.getId(), courseId);

        List<Lesson> lessons = lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(courseId);
        List<LessonProgress> progresses = lessonProgressRepository.findByUserIdAndCourseId(user.getId(), courseId);

        Map<Long, LessonProgress> progressMap = progresses.stream()
                .collect(Collectors.toMap(
                        p -> p.getLesson().getId(),
                        p -> p
                ));

        List<LessonProgressResponse> lessonProgresses = lessons.stream()
                .map(lesson -> {
                    LessonProgress lp = progressMap.get(lesson.getId());
                    if (lp != null) {
                        return LessonProgressResponse.from(lp);
                    }
                    return LessonProgressResponse.builder()
                            .lessonId(lesson.getId())
                            .lessonTitle(lesson.getTitle())
                            .sortOrder(lesson.getSortOrder())
                            .completed(false)
                            .completedAt(null)
                            .build();
                })
                .toList();

        int completedCount = (int) lessonProgresses.stream()
                .filter(LessonProgressResponse::isCompleted)
                .count();

        return CourseProgressResponse.of(
                course.getId(),
                course.getTitle(),
                lessons.size(),
                completedCount,
                lessonProgresses
        );
    }

    /**
     * 受講中全コースの進捗サマリー
     */
    @Transactional(readOnly = true)
    public List<CourseProgressResponse> getMyProgress(String userEmail) {
        User user = findUserByEmail(userEmail);

        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(user.getId());
        List<CourseProgressResponse> progressList = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            List<Lesson> lessons = lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(course.getId());
            long completedCount = lessonProgressRepository.countCompletedByUserIdAndCourseId(
                    user.getId(), course.getId());

            CourseProgressResponse progress = CourseProgressResponse.of(
                    course.getId(),
                    course.getTitle(),
                    lessons.size(),
                    (int) completedCount,
                    null  // サマリーなのでレッスン詳細は含めない
            );
            progressList.add(progress);
        }

        return progressList;
    }

    /**
     * コース完了チェック - 全レッスン完了時にEnrollmentステータスをCOMPLETEDに更新
     */
    private void checkCourseCompletion(Long userId, Course course) {
        List<Lesson> lessons = lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(course.getId());
        long completedCount = lessonProgressRepository.countCompletedByUserIdAndCourseId(userId, course.getId());

        if (lessons.size() > 0 && completedCount >= lessons.size()) {
            Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                    .orElse(null);
            if (enrollment != null && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
                enrollment.setCompletedAt(LocalDateTime.now());
                enrollmentRepository.save(enrollment);

                // 証明書を自動発行
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    certificateService.issueCertificate(user, course);
                    notificationService.createNotification(
                            user,
                            "コース完了おめでとうございます！",
                            "「" + course.getTitle() + "」の証明書が発行されました。",
                            "CERTIFICATE",
                            "/my-certificates");
                }
            }
        }
    }

    private void validateEnrollment(Long userId, Long courseId) {
        if (!enrollmentRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                && !enrollmentRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.COMPLETED)) {
            throw new BadRequestException("このコースに受講登録されていません");
        }
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
