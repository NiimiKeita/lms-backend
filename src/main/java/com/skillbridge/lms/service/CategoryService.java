package com.skillbridge.lms.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.request.CreateCategoryRequest;
import com.skillbridge.lms.dto.response.CategoryResponse;
import com.skillbridge.lms.entity.Category;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CategoryRepository;
import com.skillbridge.lms.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse getCategory(Long id) {
        Category category = findCategoryById(id);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("同名のカテゴリが既に存在します: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        Category category = findCategoryById(id);

        Category existing = categoryRepository.findByName(request.getName()).orElse(null);
        if (existing != null && !existing.getId().equals(id)) {
            throw new BadRequestException("同名のカテゴリが既に存在します: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    @Transactional
    public void setCourseCategories(Long courseId, List<Long> categoryIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("コースが見つかりません: " + courseId));

        Set<Category> categories = categoryIds.stream()
                .map(this::findCategoryById)
                .collect(Collectors.toSet());

        course.setCategories(categories);
        courseRepository.save(course);
    }

    public List<CategoryResponse> getCourseCategories(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("コースが見つかりません: " + courseId));

        return course.getCategories().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("カテゴリが見つかりません: " + id));
    }
}
