package ru.yandex.practicum.ewm.core.event.service;

import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;
import ru.yandex.practicum.ewm.api.event.dto.NewEventDto;
import ru.yandex.practicum.ewm.api.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventAuthorizedService {
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    EventFullDto getUserEvent(Long userId, Long eventId);
}
