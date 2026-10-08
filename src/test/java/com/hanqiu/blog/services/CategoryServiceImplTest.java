package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.repositories.CategoryRepository;
import com.hanqiu.blog.services.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category1;
    private Category category2;
    private List<Category> categories;

    @BeforeEach
    void setup() {
        category1 = Category.builder()
                .id(UUID.randomUUID())
                .name("Category 1")
                .posts(new ArrayList<>())
                .build();

        category2 = Category.builder()
                .id(UUID.randomUUID())
                .name("Category 2")
                .build();

        categories = List.of(category1, category2);
    }

    @Test
    void listCategories() {

        when(categoryRepository.findAllWithPostCount())
                .thenReturn(categories);

        List<Category> result = categoryService.listCategories();

        assertEquals(categories, result);

    }

    @Test
    void createCategory() {
        Category newCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("New Category")
                .build();

        when(categoryRepository.existsByNameIgnoreCase(newCategory.getName()))
                .thenReturn(false);
        when(categoryRepository.save(newCategory))
                .thenReturn(newCategory);

        Category result = categoryService.createCategory(newCategory);

        assertEquals(newCategory, result);
    }

    @Test
    void deleteCategory() {
        when(categoryRepository.findById(category1.getId()))
                .thenReturn(Optional.of(category1));

        categoryService.deleteCategory(category1.getId());

        verify(categoryRepository).deleteById(category1.getId());
    }

    @Test
    void getCategoryById() {
        when(categoryRepository.findById(category1.getId()))
                .thenReturn(Optional.of(category1));

        Category result = categoryService.getCategoryById(category1.getId());

        assertEquals(category1, result);

    }
}
