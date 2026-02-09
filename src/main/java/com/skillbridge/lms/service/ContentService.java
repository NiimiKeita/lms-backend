package com.skillbridge.lms.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.LessonContentResponse;
import com.skillbridge.lms.entity.Lesson;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.LessonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    @Value("${content.base-path:content}")
    private String basePath;

    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public LessonContentResponse getContent(Long courseId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "レッスンが見つかりません: courseId=" + courseId + ", lessonId=" + lessonId));

        String content = readContentFile(courseId, lessonId);
        if (content == null) {
            content = "# " + lesson.getTitle() + "\n\nコンテンツは準備中です。";
        }

        return LessonContentResponse.builder()
                .lessonId(lesson.getId())
                .title(lesson.getTitle())
                .content(content)
                .orderIndex(lesson.getSortOrder())
                .build();
    }

    private String readContentFile(Long courseId, Long lessonId) {
        Path filePath = Path.of(basePath, "courses", String.valueOf(courseId),
                "lessons", lessonId + ".md");

        if (!Files.exists(filePath)) {
            log.debug("Content file not found: {}", filePath);
            return null;
        }

        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Failed to read content file: {}", filePath, e);
            return null;
        }
    }
}
