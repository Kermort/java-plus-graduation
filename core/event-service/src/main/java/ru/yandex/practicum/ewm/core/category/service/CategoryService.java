package ru.yandex.practicum.ewm.core.category.service;

import ru.yandex.practicum.ewm.core.category.model.Category;
import ru.yandex.practicum.ewm.api.category.dto.CategoryDto;
import ru.yandex.practicum.ewm.api.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategory);

    void removeCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, NewCategoryDto updateCategory);

    List<CategoryDto> findAllCategories(Long from, Long size);

    CategoryDto findCategoryById(Long categoryId);

    Category findCategoryEntityById(Long categoryId);
}
