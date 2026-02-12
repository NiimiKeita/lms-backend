package com.skillbridge.lms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.request.CreateTaskRequest;
import com.skillbridge.lms.dto.request.UpdateTaskRequest;
import com.skillbridge.lms.dto.response.TaskResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Task;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;

    public List<TaskResponse> getTasks(Long courseId) {
        findCourseById(courseId);
        return taskRepository.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse getTask(Long courseId, Long taskId) {
        findCourseById(courseId);
        Task task = findTaskByCourseIdAndId(courseId, taskId);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse createTask(Long courseId, CreateTaskRequest request) {
        Course course = findCourseById(courseId);

        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = taskRepository.findMaxSortOrderByCourseId(courseId) + 1;
        }

        Task task = Task.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .sortOrder(sortOrder)
                .build();

        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTask(Long courseId, Long taskId, UpdateTaskRequest request) {
        findCourseById(courseId);
        Task task = findTaskByCourseIdAndId(courseId, taskId);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    @Transactional
    public void deleteTask(Long courseId, Long taskId) {
        findCourseById(courseId);
        Task task = findTaskByCourseIdAndId(courseId, taskId);
        taskRepository.delete(task);
    }

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("コースが見つかりません: " + courseId));
    }

    private Task findTaskByCourseIdAndId(Long courseId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("課題が見つかりません: " + taskId));

        if (!task.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("課題が見つかりません: " + taskId);
        }

        return task;
    }
}
