package ru.yandex.practicum.ewm.api.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.ewm.api.category.dto.CategoryDto;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;

import java.time.LocalDateTime;

public record EventShortDto(
        Long id,
        String title,
        String annotation,
        CategoryDto category,
        UserShortDto initiator,
        Boolean paid,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,
        Long confirmedRequests
) {
}
