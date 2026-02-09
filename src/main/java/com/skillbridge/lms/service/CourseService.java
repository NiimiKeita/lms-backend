package com.skillbridge.lms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.skillbridge.lms.dto.request.CreateCourseRequest;
import com.skillbridge.lms.dto.request.UpdateCourseRequest;
import com.skillbridge.lms.dto.response.CourseResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    /**
     * コース一覧取得（LEARNER向け: publishedのみ / ADMIN向け: 全件）
     */
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCourses(String keyword, boolean isAdmin, Pageable pageable) {
        Page<Course> page;

        if (StringUtils.hasText(keyword)) {
            page = isAdmin
                    ? courseRepository.searchAllByKeyword(keyword, pageable)
                    : courseRepository.searchPublishedByKeyword(keyword, pageable);
        } else {
            page = isAdmin
                    ? courseRepository.findAll(pageable)
                    : courseRepository.findByPublishedTrue(pageable);
        }

        List<CourseResponse> content = page.getContent().stream()
                .map(CourseResponse::from)
                .toList();

        return PageResponse.from(page, content);
    }

    /**
     * コース詳細取得
     */
    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id, boolean isAdmin) {
        Course course = findCourseById(id);

        if (!isAdmin && !course.getPublished()) {
            throw new ResourceNotFoundException("コースが見つかりません: " + id);
        }

        return CourseResponse.from(course);
    }

    /**
     * コース新規作成（ADMIN）
     */
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .published(false)
                .sortOrder(0)
                .build();

        course = courseRepository.save(course);
        return CourseResponse.from(course);
    }

    /**
     * コース更新（ADMIN）
     */
    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = findCourseById(id);

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());

        course = courseRepository.save(course);
        return CourseResponse.from(course);
    }

    /**
     * コース削除（ADMIN）
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = findCourseById(id);
        courseRepository.delete(course);
    }

    /**
     * コース公開/非公開切替（ADMIN）
     */
    @Transactional
    public CourseResponse togglePublish(Long id) {
        Course course = findCourseById(id);
        course.setPublished(!course.getPublished());
        course = courseRepository.save(course);
        return CourseResponse.from(course);
    }

    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("コースが見つかりません: " + id));
    }
}
