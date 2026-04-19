package ru.yandex.practicum.ewm.core.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.api.event.EventFeignClient;
import ru.yandex.practicum.ewm.api.exception.ConflictException;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.core.request.repository.ParticipationRequestRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipationRequestValidator {
    private final ParticipationRequestRepository requestRepository;
    private final EventFeignClient eventClient;

    public void checkEventForInitiator(Long userId, Long eventId) {
        EventFullDto eventDto = eventClient.findByIdInternal(eventId);
        if (eventDto == null) {
            throw new NotFoundException("не найдено событие с id " + eventId);
        }

        if (!eventDto.getInitiator().id().equals(userId)) {
            log.warn("Пользователь id={} не инициатор события id={}", userId, eventId);
            throw new ConflictException("Only event initiator can change request status");
        }
    }
}
