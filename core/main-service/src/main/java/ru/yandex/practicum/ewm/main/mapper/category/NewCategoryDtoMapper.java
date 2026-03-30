package ru.yandex.practicum.ewm.main.mapper.category;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.main.model.category.Category;
import ru.yandex.practicum.ewm.main.model.category.NewCategoryDto;

@UtilityClass
public class NewCategoryDtoMapper {
    public static Category toModel(NewCategoryDto newCategory) {
        return Category.builder()
                .name(newCategory.getName())
                .build();
    }
}
