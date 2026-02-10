package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.skillbridge.lms.dto.request.CreateTaskRequest;
import com.skillbridge.lms.dto.request.UpdateTaskRequest;
import com.skillbridge.lms.dto.response.TaskResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Task;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Course course;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .id(1L)
                .title("Test Course")
                .build();

        task1 = Task.builder()
                .id(1L)
                .course(course)
                .title("Task 1")
                .description("Description 1")
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        task2 = Task.builder()
                .id(2L)
                .course(course)
                .title("Task 2")
                .description("Description 2")
                .sortOrder(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getTasks - 課題一覧取得")
    void getTasks_returnsTasks() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findByCourseIdOrderBySortOrderAsc(1L)).thenReturn(List.of(task1, task2));

        List<TaskResponse> result = taskService.getTasks(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Task 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Task 2");
    }

    @Test
    @DisplayName("getTasks - 存在しないコース - ResourceNotFoundException")
    void getTasks_courseNotFound_throwsException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTasks(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTask - 課題詳細取得")
    void getTask_returnsTask() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));

        TaskResponse result = taskService.getTask(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Task 1");
    }

    @Test
    @DisplayName("getTask - 存在しない課題 - ResourceNotFoundException")
    void getTask_taskNotFound_throwsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTask - 別コースの課題 - ResourceNotFoundException")
    void getTask_differentCourse_throwsException() {
        Course otherCourse = Course.builder().id(2L).build();
        Task otherTask = Task.builder().id(3L).course(otherCourse).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(otherTask));

        assertThatThrownBy(() -> taskService.getTask(1L, 3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createTask - 成功")
    void createTask_success() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findMaxSortOrderByCourseId(1L)).thenReturn(2);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(3L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        TaskResponse result = taskService.createTask(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getSortOrder()).isEqualTo(3);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask - sortOrder指定あり")
    void createTask_withSortOrder() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setSortOrder(5);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(3L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        TaskResponse result = taskService.createTask(1L, request);

        assertThat(result.getSortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("updateTask - 成功")
    void updateTask_success() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse result = taskService.updateTask(1L, 1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("updateTask - 存在しない課題 - ResourceNotFoundException")
    void updateTask_notFound_throwsException() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(1L, 999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteTask - 成功")
    void deleteTask_success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));

        taskService.deleteTask(1L, 1L);

        verify(taskRepository).delete(task1);
    }

    @Test
    @DisplayName("deleteTask - 存在しない課題 - ResourceNotFoundException")
    void deleteTask_notFound_throwsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
