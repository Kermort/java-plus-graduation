package ru.yandex.practicum.ewm.core.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventInternalDto;

import java.util.List;

public interface EventInternalService {
    EventFullDto getByIdInternal(Long eventId, HttpServletRequest request);

    List<EventInternalDto> getByIdInInternal(List<Long> ids);
}
