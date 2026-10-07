package com.hanqiu.blog.controller;

import com.hanqiu.blog.domain.dtos.CategoryDto;
import com.hanqiu.blog.domain.dtos.CreateCategoryRequest;
import com.hanqiu.blog.domain.dtos.CreatePostRequestDto;
import com.hanqiu.blog.domain.entities.Category;
import com.hanqiu.blog.mappers.CategoryMapper;
import com.hanqiu.blog.services.CategoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

//@WebMvcTest + MockMvc + Mockito

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    private CreateCategoryRequest request;
    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        request = CreateCategoryRequest.builder()
                .name("Test")
                .build();

        category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Test");

        categoryDto = CategoryDto.builder()
                .id(category.getId())
                .name("Test")
                .postCount(0)
                .build();
    }

    @Test
    void listCategories_shouldReturn200() throws Exception {
        when(categoryService.listCategories())
                .thenReturn(List.of(category));

        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].postCount").value(0));
    }

    @Test
    void createCategory_shouldReturn201() throws Exception {

        when(categoryMapper.toEntity(request))
                .thenReturn(category);

        when(categoryService.createCategory(category))
                .thenReturn(category);

        when(categoryMapper.toDto(category))
                .thenReturn(categoryDto);


        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.postCount").value(0));;
    }

    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        CreateCategoryRequest invalidRequest =
                CreateCategoryRequest.builder()
                        .name("")
                        .build();

        mockMvc.perform(
                post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/v1/categories/{id}", id)
                )
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(id);

    }
}
