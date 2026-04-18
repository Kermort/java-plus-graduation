package ru.yandex.practicum.ewm.core.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.StatsClient;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.api.exception.ValidationException;
import ru.yandex.practicum.ewm.api.request.RequestFeignClient;
import ru.yandex.practicum.ewm.api.user.UserFeignClient;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.event.mapper.EventsMapper;
import ru.yandex.practicum.ewm.core.event.model.Events;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;
import ru.yandex.practicum.ewm.api.event.enums.EventState;
import ru.yandex.practicum.ewm.core.event.model.params.PublicEventSearchParams;
import ru.yandex.practicum.ewm.core.event.repository.EventsRepository;
import ru.yandex.practicum.ewm.core.event.service.EventPublicService;
import ru.yandex.practicum.ewm.stats.dto.EndpointHitDto;
import ru.yandex.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublicServiceImpl implements EventPublicService {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventsRepository eventsRepository;
    private final EventsMapper eventsMapper;
    private final StatsClient statsClient;
    private final RequestFeignClient requestClient;
    private final UserFeignClient userClient;


    public List<EventShortDto> getEvents(PublicEventSearchParams params,
                                         HttpServletRequest request) {
        log.info("Поиск публичных событий params={}", params);
        validateSearchParams(params);
        saveHit(request);

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Events> page = eventsRepository.findPublicEvents(params, pageable);

        List<Events> events = page.getContent();
        if (events.isEmpty()) {
            return List.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();
        Map<String, Long> viewsByUri = getViewsForUris(uris);

        Map<Long, UserShortDto> userDtos = collectUsers(events);

        return events.stream()
                .map(e -> {
                    EventShortDto dto = eventsMapper.toShortDto(e, userDtos.get(e.getInitiatorId()));
                    String uri = "/events/" + e.getId();

                    long views = viewsByUri.getOrDefault(uri, 0L);
                    return new EventShortDto(
                            dto.id(),
                            dto.title(),
                            dto.annotation(),
                            dto.category(),
                            dto.initiator(),
                            dto.paid(),
                            dto.eventDate(),
                            views,
                            dto.confirmedRequests()
                    );
                })
                .toList();
    }


    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        log.info("Публичный запрос события по id={}", eventId);
        saveHit(request);
        Events events = eventsRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        UserShortDto userDto = userClient.getUserById(events.getInitiatorId());
        String uri = request.getRequestURI();
        long views = getViewsForUris(List.of(uri)).getOrDefault(uri, 0L);
        log.debug("Для события id={} по uri='{}' получено просмотров={}", eventId, uri, views);
        EventFullDto dto = eventsMapper.toFullDto(events, userDto);
        fillConfirmedRequests(dto);
        dto.setViews(views);

        log.info("Событие отдано клиенту: id={}, views={}", dto.getId(), dto.getViews());
        return dto;
    }

    private Map<String, Long> getViewsForUris(List<String> uris) {
        String start = "2000-01-01 00:00:00";
        String end = LocalDateTime.now().format(FORMATTER);

        log.info("Запрашиваем статистику: start={}, end={}, uris={}", start, end, uris);

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        Map<String, Long> result = new HashMap<>();
        for (ViewStatsDto stat : stats) {
            result.put(stat.getUri(), stat.getHits());
        }
        return result;
    }


    //Отправка хита в сервис статистики.
    private void saveHit(HttpServletRequest request) {
        EndpointHitDto hit = new EndpointHitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(FORMATTER)
        );
        log.info("Отправляем хит в stats-сервис: {}", hit);
        try {
            statsClient.saveHit(hit);
        } catch (Exception e) {
            log.error("Не удалось отправить хит в stats-сервис: {}", e.getMessage(), e);
        }
    }

    private void validateSearchParams(PublicEventSearchParams params) {
        LocalDateTime start = params.getRangeStart();
        LocalDateTime end = params.getRangeEnd();

        if (start != null && end != null && start.isAfter(end)) {
            log.warn("Некорректный диапазон дат при поиске событий: rangeStart={} > rangeEnd={}",
                    start, end);
            throw new ValidationException(
                    "rangeStart must not be after rangeEnd"
            );
        }
    }

    private void fillConfirmedRequests(EventFullDto dto) {
        if (dto == null || dto.getId() == null) {
            log.warn("fillConfirmedRequests: dto или dto.id == null, пропускаем");
            return;
        }

        log.info("[event public service] fill confirmed requests for event id = {} ", dto.getId());
        Long confirmed = requestClient.countConfirmedRequestsByEventId(dto.getId()).getBody();
        dto.setConfirmedRequests(confirmed);

        log.info("Для события id={} установлено confirmedRequests={}",
                dto.getId(), confirmed);
    }

    private Map<Long, UserShortDto> collectUsers(List<Events> events) {
        List<Long> userIds = events.stream()
                .map(Events::getInitiatorId)
                .toList();

        List<UserShortDto> users = userClient.getUsers(userIds);
        return users.stream()
                .collect(Collectors.toMap(UserShortDto::id, u -> u));
    }
}
