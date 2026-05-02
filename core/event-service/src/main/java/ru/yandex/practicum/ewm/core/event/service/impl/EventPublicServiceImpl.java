package ru.yandex.practicum.ewm.core.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.grpc.ActionTypeProto;
import ru.practicum.ewm.stats.grpc.RecommendedEventProto;
import ru.practicum.ewm.stats.grpc.UserActionProto;
import ru.yandex.practicum.AnalyzerClient;
import ru.yandex.practicum.CollectorClient;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublicServiceImpl implements EventPublicService {
    @Value("${event-service.default-max-results}")
    private int defaultMaxResultsValue;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventsRepository eventsRepository;
    private final EventsMapper eventsMapper;
    private final StatsClient statsClient;
    private final RequestFeignClient requestClient;
    private final UserFeignClient userClient;
    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;


    public List<EventShortDto> getEvents(PublicEventSearchParams params,
                                         HttpServletRequest request) {
        log.info("Поиск публичных событий params={}", params);
        validateSearchParams(params);


        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Events> page = eventsRepository.findPublicEvents(params, pageable);

        List<Events> events = page.getContent();
        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, UserShortDto> userDtos = collectUsers(events);

        return events.stream()
                .map(e -> {
                    EventShortDto dto = eventsMapper.toShortDto(e, userDtos.get(e.getInitiatorId()));

                    return new EventShortDto(
                            dto.id(),
                            dto.title(),
                            dto.annotation(),
                            dto.category(),
                            dto.initiator(),
                            dto.paid(),
                            dto.eventDate(),
                            dto.confirmedRequests()
                    );
                })
                .toList();
    }


    @Override
    public EventFullDto getById(Long eventId, Long userId, HttpServletRequest request) {
        log.info("Публичный запрос события по id={}", eventId);

        Events events = eventsRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        UserShortDto userDto = userClient.getUserById(events.getInitiatorId());



        EventFullDto dto = eventsMapper.toFullDto(events, userDto);
        fillConfirmedRequests(dto);

        collectorClient.collectUserAction(UserActionProto.newBuilder()
                        .setUserId(userId)
                        .setEventId(eventId)
                        .setActionType(ActionTypeProto.ACTION_VIEW)
                .build());

        log.info("Событие отдано клиенту: id={}", dto.getId());
        return dto;
    }

    @Override
    public List<EventShortDto> getRecommendations(long userId) {
        log.info("[Event public service] get recommendations for user {}", userId);
        Map<Long, Double> recommendedEvents = analyzerClient.getRecommendationsForUser(userId, defaultMaxResultsValue);
        List<Events> events = eventsRepository.findByIdIn(recommendedEvents.keySet().stream().toList());

        Map<Long, UserShortDto> userDtos = collectUsers(events);

        return events.stream()
                .map(e -> {
                    EventShortDto dto = eventsMapper.toShortDto(e, userDtos.get(e.getInitiatorId()));

                    return new EventShortDto(
                            dto.id(),
                            dto.title(),
                            dto.annotation(),
                            dto.category(),
                            dto.initiator(),
                            dto.paid(),
                            dto.eventDate(),
                            dto.confirmedRequests()
                    );
                })
                .toList();
    }

    @Override
    public void sendLike(long userId, long eventId) {
        Events event = eventsRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
        Boolean check = requestClient.checkParticipation(userId, eventId).getBody();
        if (check != null && check) {
            collectorClient.collectUserAction(UserActionProto.newBuilder()
                            .setUserId(userId)
                            .setEventId(eventId)
                            .setActionType(ActionTypeProto.ACTION_LIKE)
                    .build());
        } else {
            throw new ValidationException("пользователь может лайкать только почещенные мероприятия");
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

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
