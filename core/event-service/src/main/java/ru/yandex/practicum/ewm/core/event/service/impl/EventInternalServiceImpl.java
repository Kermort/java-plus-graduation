package ru.yandex.practicum.ewm.core.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventInternalDto;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.api.user.UserFeignClient;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.event.mapper.EventsMapper;
import ru.yandex.practicum.ewm.core.event.model.Events;
import ru.yandex.practicum.ewm.core.event.repository.EventsRepository;
import ru.yandex.practicum.ewm.core.event.service.EventInternalService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventInternalServiceImpl implements EventInternalService {
    private final EventsRepository eventsRepository;
    private final UserFeignClient userClient;
    private final EventsMapper eventsMapper;

    @Override
    public EventFullDto getByIdInternal(Long eventId, HttpServletRequest request) {
        log.info("Внутренний запрос события по id={}", eventId);

        Events events = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        UserShortDto userDto = userClient.getUserById(events.getInitiatorId());

        EventFullDto dto = eventsMapper.toFullDto(events, userDto);

        log.info("Событие отдано сервису: id={}", dto.getId());
        return dto;
    }

    @Override
    public List<EventInternalDto> getByIdInInternal(List<Long> ids) {
        log.info("внутренний запрос списка событий по ids={}", ids);
        List<Events> events = eventsRepository.findByIdIn(ids);

        return events.stream()
                .map(eventsMapper::toInternalDto)
                .toList();
    }
}
