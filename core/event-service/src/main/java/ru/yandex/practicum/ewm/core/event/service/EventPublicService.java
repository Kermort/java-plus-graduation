package ru.yandex.practicum.ewm.core.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;
import ru.yandex.practicum.ewm.core.event.model.params.PublicEventSearchParams;

import java.util.List;

public interface EventPublicService {
    List<EventShortDto> getEvents(PublicEventSearchParams params,
                                  HttpServletRequest request);

    EventFullDto getById(Long eventId,
                         HttpServletRequest request);
}
