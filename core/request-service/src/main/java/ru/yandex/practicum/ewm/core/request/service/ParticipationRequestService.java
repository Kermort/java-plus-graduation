package ru.yandex.practicum.ewm.core.request.service;

import ru.yandex.practicum.ewm.api.request.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.ewm.api.request.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.ewm.api.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestService {
    ParticipationRequestDto add(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> findByRequesterId(Long requesterId);

    List<ParticipationRequestDto> findEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                       Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    Long countConfirmedRequestsByEventId(Long eventId);

    Map<Long, Long> countConfirmedRequestsByEventIds(List<Long> eventIds);
}
