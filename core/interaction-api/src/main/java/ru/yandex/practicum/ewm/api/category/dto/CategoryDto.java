package ru.yandex.practicum.ewm.api.category.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    Long id;

    String name;
}
