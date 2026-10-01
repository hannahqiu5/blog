package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.entities.Category;

import java.util.List;

public interface CategoryService {
    List<Category> listCategories();

    Category createCategory(Category category);
}
