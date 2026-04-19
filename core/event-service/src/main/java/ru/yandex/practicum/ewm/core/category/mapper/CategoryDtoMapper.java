package ru.yandex.practicum.ewm.core.category.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.core.category.model.Category;
import ru.yandex.practicum.ewm.api.category.dto.CategoryDto;

@UtilityClass
public class CategoryDtoMapper {
    public static CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toModel(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
