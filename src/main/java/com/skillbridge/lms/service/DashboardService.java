package com.skillbridge.lms.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.CourseProgressResponse;
import com.skillbridge.lms.dto.response.InstructorDashboardResponse;
import com.skillbridge.lms.dto.response.LearnerDashboardResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.Lesson;
import com.skillbridge.lms.entity.Task;
import com.skillbridge.lms.entity.TaskFeedback;
import com.skillbridge.lms.entity.TaskSubmission;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.EnrollmentStatus;
import com.skillbridge.lms.enums.SubmissionStatus;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.LessonProgressRepository;
import com.skillbridge.lms.repository.LessonRepository;
import com.skillbridge.lms.repository.TaskFeedbackRepository;
import com.skillbridge.lms.repository.TaskRepository;
import com.skillbridge.lms.repository.TaskSubmissionRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskFeedbackRepository taskFeedbackRepository;

    public LearnerDashboardResponse getLearnerDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        // Enrolled courses with progress (max 5, ACTIVE only)
        List<Enrollment> activeEnrollments = enrollmentRepository
                .findByUserIdAndStatusOrderByEnrolledAtDesc(user.getId(), EnrollmentStatus.ACTIVE);

        List<CourseProgressResponse> enrolledCourses = new ArrayList<>();
        int limit = Math.min(activeEnrollments.size(), 5);
        for (int i = 0; i < limit; i++) {
            Enrollment enrollment = activeEnrollments.get(i);
            Course course = enrollment.getCourse();
            List<Lesson> lessons = lessonRepository
                    .findByCourseIdAndPublishedTrueOrderBySortOrderAsc(course.getId());
            long completedCount = lessonProgressRepository
                    .countCompletedByUserIdAndCourseId(user.getId(), course.getId());
            enrolledCourses.add(CourseProgressResponse.of(
                    course.getId(), course.getTitle(),
                    lessons.size(), (int) completedCount, null));
        }

        // Pending tasks: tasks in enrolled courses where user has no submission
        List<LearnerDashboardResponse.PendingTaskItem> pendingTasks = new ArrayList<>();
        List<Enrollment> allEnrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(user.getId());
        Set<Long> submittedTaskIds = new HashSet<>();
        List<TaskSubmission> userSubmissions = taskSubmissionRepository
                .findByUserIdOrderBySubmittedAtDesc(user.getId());
        for (TaskSubmission sub : userSubmissions) {
            if (sub.getStatus() != SubmissionStatus.REJECTED) {
                submittedTaskIds.add(sub.getTask().getId());
            }
        }

        for (Enrollment enrollment : allEnrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.DROPPED) continue;
            Course course = enrollment.getCourse();
            List<Task> tasks = taskRepository.findByCourseIdOrderBySortOrderAsc(course.getId());
            for (Task task : tasks) {
                if (!submittedTaskIds.contains(task.getId())) {
                    pendingTasks.add(LearnerDashboardResponse.PendingTaskItem.builder()
                            .taskId(task.getId())
                            .taskTitle(task.getTitle())
                            .courseId(course.getId())
                            .courseTitle(course.getTitle())
                            .build());
                }
            }
        }

        // Recent feedbacks (max 5)
        List<TaskFeedback> recentFeedbackEntities = taskFeedbackRepository
                .findTop5BySubmissionUserIdOrderByCreatedAtDesc(user.getId());
        List<LearnerDashboardResponse.RecentFeedbackItem> recentFeedbacks = recentFeedbackEntities.stream()
                .map(f -> LearnerDashboardResponse.RecentFeedbackItem.builder()
                        .submissionId(f.getSubmission().getId())
                        .taskTitle(f.getSubmission().getTask().getTitle())
                        .reviewerName(f.getReviewer().getUsername())
                        .comment(f.getComment())
                        .createdAt(f.getCreatedAt().toString())
                        .build())
                .toList();

        return LearnerDashboardResponse.builder()
                .enrolledCourses(enrolledCourses)
                .pendingTasks(pendingTasks)
                .recentFeedbacks(recentFeedbacks)
                .build();
    }

    public InstructorDashboardResponse getInstructorDashboard() {
        long unreviewedCount = taskSubmissionRepository.countByStatus(SubmissionStatus.SUBMITTED);

        List<TaskSubmission> recentSubmissions = taskSubmissionRepository
                .findTop10ByOrderBySubmittedAtDesc();

        List<InstructorDashboardResponse.RecentSubmissionItem> items = recentSubmissions.stream()
                .map(s -> InstructorDashboardResponse.RecentSubmissionItem.builder()
                        .submissionId(s.getId())
                        .taskTitle(s.getTask().getTitle())
                        .learnerName(s.getUser().getUsername())
                        .status(s.getStatus().name())
                        .submittedAt(s.getSubmittedAt().toString())
                        .build())
                .toList();

        return InstructorDashboardResponse.builder()
                .unreviewedCount(unreviewedCount)
                .recentSubmissions(items)
                .build();
    }
}
