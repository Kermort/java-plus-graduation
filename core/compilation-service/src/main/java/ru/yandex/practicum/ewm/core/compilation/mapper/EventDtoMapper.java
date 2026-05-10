package ru.yandex.practicum.ewm.core.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.api.event.dto.EventInternalDto;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;

@UtilityClass
public class EventDtoMapper {
    public static EventShortDto toEventShortDto(EventInternalDto internalDto, UserShortDto userDto) {
        return new EventShortDto(
                internalDto.id(),
                internalDto.title(),
                internalDto.annotation(),
                internalDto.category(),
                userDto,
                internalDto.paid(),
                internalDto.eventDate(),
                internalDto.confirmedRequests()
        );
    }
}
