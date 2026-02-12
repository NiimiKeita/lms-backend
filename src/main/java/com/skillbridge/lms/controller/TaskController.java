package com.skillbridge.lms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.skillbridge.lms.dto.request.CreateTaskRequest;
import com.skillbridge.lms.dto.request.UpdateTaskRequest;
import com.skillbridge.lms.dto.response.MessageResponse;
import com.skillbridge.lms.dto.response.TaskResponse;
import com.skillbridge.lms.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses/{courseId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "課題管理 API")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable Long courseId) {
        List<TaskResponse> response = taskService.getTasks(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long courseId,
            @PathVariable Long taskId) {
        TaskResponse response = taskService.getTask(courseId, taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long courseId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(courseId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<MessageResponse> deleteTask(
            @PathVariable Long courseId,
            @PathVariable Long taskId) {
        taskService.deleteTask(courseId, taskId);
        return ResponseEntity.ok(new MessageResponse("課題を削除しました"));
    }
}
