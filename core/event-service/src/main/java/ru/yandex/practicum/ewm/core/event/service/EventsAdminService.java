package ru.yandex.practicum.ewm.core.event.service;

import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.ewm.core.event.model.params.AdminEventSearchParams;

import java.util.List;

public interface EventsAdminService {
    List<EventFullDto> getEvents(AdminEventSearchParams params);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest);
}
