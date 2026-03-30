package ru.yandex.practicum.ewm.main.service.events;

import ru.yandex.practicum.ewm.main.model.events.dto.EventFullDto;
import ru.yandex.practicum.ewm.main.model.events.dto.EventShortDto;
import ru.yandex.practicum.ewm.main.model.events.dto.NewEventDto;
import ru.yandex.practicum.ewm.main.model.events.dto.UpdateEventUserRequest;
import ru.yandex.practicum.ewm.main.model.request.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.ewm.main.model.request.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.ewm.main.model.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventAuthorizedService {
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    EventFullDto getUserEvent(Long userId, Long eventId);

    List<ParticipationRequestDto> findEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult rejectingRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);
}
