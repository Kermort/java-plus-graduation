package ru.yandex.practicum.ewm.main.service.events;

import jakarta.servlet.http.HttpServletRequest;
import ru.yandex.practicum.ewm.main.model.events.dto.EventFullDto;
import ru.yandex.practicum.ewm.main.model.events.dto.EventShortDto;
import ru.yandex.practicum.ewm.main.model.events.params.PublicEventSearchParams;

import java.util.List;

public interface EventPublicService {
    List<EventShortDto> getEvents(PublicEventSearchParams params,
                                  HttpServletRequest request);

    EventFullDto getById(Long eventId,
                         HttpServletRequest request);
}
