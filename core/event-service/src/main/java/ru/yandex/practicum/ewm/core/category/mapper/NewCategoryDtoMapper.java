package ru.yandex.practicum.ewm.core.category.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.core.category.model.Category;
import ru.yandex.practicum.ewm.api.category.dto.NewCategoryDto;

@UtilityClass
public class NewCategoryDtoMapper {
    public static Category toModel(NewCategoryDto newCategory) {
        return Category.builder()
                .name(newCategory.getName())
                .build();
    }
}
