package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.skillbridge.lms.dto.request.CreateCategoryRequest;
import com.skillbridge.lms.dto.response.CategoryResponse;
import com.skillbridge.lms.entity.Category;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CategoryRepository;
import com.skillbridge.lms.repository.CourseRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Java")
                .description("Java programming")
                .build();
    }

    @Test
    @DisplayName("getAllCategories - 全カテゴリ取得")
    void getAllCategories_returnsAll() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("getCategory - カテゴリ取得成功")
    void getCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getCategory(1L);

        assertThat(result.getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("getCategory - 存在しないカテゴリ")
    void getCategory_notFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createCategory - 作成成功")
    void createCategory_success() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Python");
        request.setDescription("Python programming");

        when(categoryRepository.existsByName("Python")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(
                Category.builder().id(2L).name("Python").description("Python programming").build());

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("Python");
    }

    @Test
    @DisplayName("createCategory - 重複名称でエラー")
    void createCategory_duplicate() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Java");

        when(categoryRepository.existsByName("Java")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("updateCategory - 更新成功")
    void updateCategory_success() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Java SE");
        request.setDescription("Updated");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Java SE")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("deleteCategory - 削除成功")
    void deleteCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("setCourseCategories - コースにカテゴリ設定")
    void setCourseCategories_success() {
        Course course = Course.builder().id(1L).title("Test").categories(new HashSet<>()).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        categoryService.setCourseCategories(1L, List.of(1L));

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("getCourseCategories - コースのカテゴリ取得")
    void getCourseCategories_success() {
        Course course = Course.builder().id(1L).title("Test").categories(new HashSet<>(List.of(category))).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        List<CategoryResponse> result = categoryService.getCourseCategories(1L);

        assertThat(result).hasSize(1);
    }
}
