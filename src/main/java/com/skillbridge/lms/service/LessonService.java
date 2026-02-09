package com.skillbridge.lms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.request.CreateLessonRequest;
import com.skillbridge.lms.dto.request.ReorderLessonsRequest;
import com.skillbridge.lms.dto.request.UpdateLessonRequest;
import com.skillbridge.lms.dto.response.LessonResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Lesson;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.LessonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    /**
     * レッスン一覧取得（ADMIN: 全件 / LEARNER: publishedのみ）
     */
    public List<LessonResponse> getLessons(Long courseId, boolean isAdmin) {
        findCourseById(courseId);

        List<Lesson> lessons = isAdmin
                ? lessonRepository.findByCourseIdOrderBySortOrderAsc(courseId)
                : lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(courseId);

        return lessons.stream()
                .map(LessonResponse::from)
                .toList();
    }

    /**
     * レッスン詳細取得
     */
    public LessonResponse getLesson(Long courseId, Long lessonId, boolean isAdmin) {
        findCourseById(courseId);
        Lesson lesson = findLessonByCourseIdAndId(courseId, lessonId);

        if (!isAdmin && !lesson.getPublished()) {
            throw new ResourceNotFoundException("レッスンが見つかりません: " + lessonId);
        }

        return LessonResponse.from(lesson);
    }

    /**
     * レッスン新規作成（ADMIN）
     */
    @Transactional
    public LessonResponse createLesson(Long courseId, CreateLessonRequest request) {
        Course course = findCourseById(courseId);

        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = lessonRepository.findMaxSortOrderByCourseId(courseId) + 1;
        }

        Lesson lesson = Lesson.builder()
                .course(course)
                .title(request.getTitle())
                .contentPath(request.getContentPath())
                .sortOrder(sortOrder)
                .published(request.getPublished() != null ? request.getPublished() : false)
                .build();

        lesson = lessonRepository.save(lesson);
        return LessonResponse.from(lesson);
    }

    /**
     * レッスン更新（ADMIN）
     */
    @Transactional
    public LessonResponse updateLesson(Long courseId, Long lessonId, UpdateLessonRequest request) {
        findCourseById(courseId);
        Lesson lesson = findLessonByCourseIdAndId(courseId, lessonId);

        lesson.setTitle(request.getTitle());
        lesson.setContentPath(request.getContentPath());
        if (request.getPublished() != null) {
            lesson.setPublished(request.getPublished());
        }

        lesson = lessonRepository.save(lesson);
        return LessonResponse.from(lesson);
    }

    /**
     * レッスン削除（ADMIN）
     */
    @Transactional
    public void deleteLesson(Long courseId, Long lessonId) {
        findCourseById(courseId);
        Lesson lesson = findLessonByCourseIdAndId(courseId, lessonId);
        lessonRepository.delete(lesson);
    }

    /**
     * レッスン並び替え（ADMIN）
     */
    @Transactional
    public List<LessonResponse> reorderLessons(Long courseId, ReorderLessonsRequest request) {
        findCourseById(courseId);

        for (ReorderLessonsRequest.LessonOrderItem item : request.getLessonOrders()) {
            Lesson lesson = findLessonByCourseIdAndId(courseId, item.getLessonId());
            lesson.setSortOrder(item.getSortOrder());
            lessonRepository.save(lesson);
        }

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        return lessons.stream()
                .map(LessonResponse::from)
                .toList();
    }

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("コースが見つかりません: " + courseId));
    }

    private Lesson findLessonByCourseIdAndId(Long courseId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("レッスンが見つかりません: " + lessonId));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("レッスンが見つかりません: " + lessonId);
        }

        return lesson;
    }
}
