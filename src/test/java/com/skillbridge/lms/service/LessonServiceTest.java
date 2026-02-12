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

import com.skillbridge.lms.dto.request.CreateLessonRequest;
import com.skillbridge.lms.dto.request.ReorderLessonsRequest;
import com.skillbridge.lms.dto.request.UpdateLessonRequest;
import com.skillbridge.lms.dto.response.LessonResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Lesson;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.LessonRepository;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonService lessonService;

    private Course testCourse;
    private Lesson publishedLesson;
    private Lesson unpublishedLesson;

    @BeforeEach
    void setUp() {
        testCourse = Course.builder()
                .id(1L)
                .title("Test Course")
                .description("Test Description")
                .sortOrder(0)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lessons(new ArrayList<>())
                .build();

        publishedLesson = Lesson.builder()
                .id(1L)
                .course(testCourse)
                .title("Published Lesson")
                .contentPath("/lessons/1/content.md")
                .sortOrder(1)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        unpublishedLesson = Lesson.builder()
                .id(2L)
                .course(testCourse)
                .title("Unpublished Lesson")
                .contentPath("/lessons/2/content.md")
                .sortOrder(2)
                .published(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getLessons - ADMIN - 全件取得")
    void getLessons_admin_returnsAllLessons() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findByCourseIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(publishedLesson, unpublishedLesson));

        // When
        List<LessonResponse> response = lessonService.getLessons(1L, true);

        // Then
        assertThat(response).hasSize(2);
        verify(lessonRepository).findByCourseIdOrderBySortOrderAsc(1L);
    }

    @Test
    @DisplayName("getLessons - LEARNER - publishedのみ")
    void getLessons_learner_returnsPublishedOnly() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(1L))
                .thenReturn(List.of(publishedLesson));

        // When
        List<LessonResponse> response = lessonService.getLessons(1L, false);

        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getPublished()).isTrue();
        verify(lessonRepository).findByCourseIdAndPublishedTrueOrderBySortOrderAsc(1L);
    }

    @Test
    @DisplayName("getLesson - 成功")
    void getLesson_success() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(publishedLesson));

        // When
        LessonResponse response = lessonService.getLesson(1L, 1L, true);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Published Lesson");
    }

    @Test
    @DisplayName("getLesson - 存在しないID - ResourceNotFoundException")
    void getLesson_nonExistingId_throwsResourceNotFoundException() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lessonService.getLesson(1L, 999L, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("createLesson - 成功 - sortOrder自動採番")
    void createLesson_success_autoSortOrder() {
        // Given
        CreateLessonRequest request = new CreateLessonRequest();
        request.setTitle("New Lesson");
        request.setContentPath("/lessons/new/content.md");
        request.setSortOrder(null);
        request.setPublished(false);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findMaxSortOrderByCourseId(1L)).thenReturn(2);
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(3L);
            lesson.setCreatedAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
            return lesson;
        });

        // When
        LessonResponse response = lessonService.createLesson(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("New Lesson");
        assertThat(response.getSortOrder()).isEqualTo(3);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("createLesson - 成功 - sortOrder指定")
    void createLesson_success_specifiedSortOrder() {
        // Given
        CreateLessonRequest request = new CreateLessonRequest();
        request.setTitle("New Lesson");
        request.setContentPath("/lessons/new/content.md");
        request.setSortOrder(10);
        request.setPublished(true);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(3L);
            lesson.setCreatedAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
            return lesson;
        });

        // When
        LessonResponse response = lessonService.createLesson(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSortOrder()).isEqualTo(10);
        assertThat(response.getPublished()).isTrue();
    }

    @Test
    @DisplayName("updateLesson - 成功")
    void updateLesson_success() {
        // Given
        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle("Updated Lesson");
        request.setContentPath("/lessons/updated/content.md");
        request.setPublished(true);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(publishedLesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        // When
        LessonResponse response = lessonService.updateLesson(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Updated Lesson");
        assertThat(response.getContentPath()).isEqualTo("/lessons/updated/content.md");
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("deleteLesson - 成功")
    void deleteLesson_success() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(publishedLesson));

        // When
        lessonService.deleteLesson(1L, 1L);

        // Then
        verify(lessonRepository).delete(publishedLesson);
    }

    @Test
    @DisplayName("reorderLessons - 成功")
    void reorderLessons_success() {
        // Given
        ReorderLessonsRequest request = new ReorderLessonsRequest();
        ReorderLessonsRequest.LessonOrderItem item1 = new ReorderLessonsRequest.LessonOrderItem();
        item1.setLessonId(1L);
        item1.setSortOrder(2);
        ReorderLessonsRequest.LessonOrderItem item2 = new ReorderLessonsRequest.LessonOrderItem();
        item2.setLessonId(2L);
        item2.setSortOrder(1);
        request.setLessonOrders(List.of(item1, item2));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(publishedLesson));
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(unpublishedLesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        // After reorder, return the reordered list
        Lesson reorderedLesson1 = Lesson.builder()
                .id(2L).course(testCourse).title("Unpublished Lesson")
                .contentPath("/lessons/2/content.md").sortOrder(1).published(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        Lesson reorderedLesson2 = Lesson.builder()
                .id(1L).course(testCourse).title("Published Lesson")
                .contentPath("/lessons/1/content.md").sortOrder(2).published(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(lessonRepository.findByCourseIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(reorderedLesson1, reorderedLesson2));

        // When
        List<LessonResponse> response = lessonService.reorderLessons(1L, request);

        // Then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getSortOrder()).isEqualTo(1);
        assertThat(response.get(1).getSortOrder()).isEqualTo(2);
    }
}
