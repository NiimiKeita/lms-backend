package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.skillbridge.lms.dto.request.CreateCourseRequest;
import com.skillbridge.lms.dto.request.UpdateCourseRequest;
import com.skillbridge.lms.dto.response.CourseResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private Course publishedCourse;
    private Course unpublishedCourse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        publishedCourse = Course.builder()
                .id(1L)
                .title("Published Course")
                .description("Published Description")
                .sortOrder(0)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lessons(new ArrayList<>())
                .build();

        unpublishedCourse = Course.builder()
                .id(2L)
                .title("Unpublished Course")
                .description("Unpublished Description")
                .sortOrder(1)
                .published(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lessons(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("getCourses - ADMIN - 全件取得")
    void getCourses_admin_returnsAllCourses() {
        // Given
        List<Course> courses = List.of(publishedCourse, unpublishedCourse);
        Page<Course> page = new PageImpl<>(courses, pageable, courses.size());
        when(courseRepository.findAll(pageable)).thenReturn(page);

        // When
        PageResponse<CourseResponse> response = courseService.getCourses(null, true, pageable);

        // Then
        assertThat(response.getContent()).hasSize(2);
        verify(courseRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getCourses - LEARNER - publishedのみ取得")
    void getCourses_learner_returnsPublishedOnly() {
        // Given
        List<Course> courses = List.of(publishedCourse);
        Page<Course> page = new PageImpl<>(courses, pageable, courses.size());
        when(courseRepository.findByPublishedTrue(pageable)).thenReturn(page);

        // When
        PageResponse<CourseResponse> response = courseService.getCourses(null, false, pageable);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getPublished()).isTrue();
        verify(courseRepository).findByPublishedTrue(pageable);
    }

    @Test
    @DisplayName("getCourse - 存在するID - 成功")
    void getCourse_existingId_success() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));

        // When
        CourseResponse response = courseService.getCourse(1L, true);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Published Course");
    }

    @Test
    @DisplayName("getCourse - 存在しないID - ResourceNotFoundException")
    void getCourse_nonExistingId_throwsResourceNotFoundException() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> courseService.getCourse(999L, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("createCourse - 成功")
    void createCourse_success() {
        // Given
        CreateCourseRequest request = new CreateCourseRequest();
        request.setTitle("New Course");
        request.setDescription("New Description");

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(3L);
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());
            course.setLessons(new ArrayList<>());
            return course;
        });

        // When
        CourseResponse response = courseService.createCourse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("New Course");
        assertThat(response.getDescription()).isEqualTo("New Description");
        assertThat(response.getPublished()).isFalse();
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("updateCourse - 成功")
    void updateCourse_success() {
        // Given
        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        // When
        CourseResponse response = courseService.updateCourse(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getDescription()).isEqualTo("Updated Description");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("updateCourse - 存在しないID - ResourceNotFoundException")
    void updateCourse_nonExistingId_throwsResourceNotFoundException() {
        // Given
        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> courseService.updateCourse(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deleteCourse - 成功")
    void deleteCourse_success() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));

        // When
        courseService.deleteCourse(1L);

        // Then
        verify(courseRepository).delete(publishedCourse);
    }

    @Test
    @DisplayName("deleteCourse - 存在しないID - ResourceNotFoundException")
    void deleteCourse_nonExistingId_throwsResourceNotFoundException() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> courseService.deleteCourse(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("togglePublish - 公開から非公開へ")
    void togglePublish_publishedToUnpublished() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        // When
        CourseResponse response = courseService.togglePublish(1L);

        // Then
        assertThat(response.getPublished()).isFalse();
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("togglePublish - 非公開から公開へ")
    void togglePublish_unpublishedToPublished() {
        // Given
        when(courseRepository.findById(2L)).thenReturn(Optional.of(unpublishedCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        // When
        CourseResponse response = courseService.togglePublish(2L);

        // Then
        assertThat(response.getPublished()).isTrue();
        verify(courseRepository).save(any(Course.class));
    }
}
