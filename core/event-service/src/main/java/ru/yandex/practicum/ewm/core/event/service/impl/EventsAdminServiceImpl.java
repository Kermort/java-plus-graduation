package ru.yandex.practicum.ewm.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.ewm.api.exception.ConflictException;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.api.exception.ValidationException;
import ru.yandex.practicum.ewm.api.request.RequestFeignClient;
import ru.yandex.practicum.ewm.api.user.UserFeignClient;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.event.mapper.EventsMapper;
import ru.yandex.practicum.ewm.core.category.model.Category;
import ru.yandex.practicum.ewm.core.event.model.Events;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.ewm.api.event.enums.EventState;
import ru.yandex.practicum.ewm.api.event.enums.StateActionAdminUpdateEvent;
import ru.yandex.practicum.ewm.core.event.model.params.AdminEventSearchParams;
import ru.yandex.practicum.ewm.core.category.repository.CategoryRepository;
import ru.yandex.practicum.ewm.core.event.repository.EventsRepository;
import ru.yandex.practicum.ewm.core.event.service.EventsAdminService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsAdminServiceImpl implements EventsAdminService {
    private final EventsMapper mapper;
    private final EventsRepository eventsRepository;
    private final CategoryRepository categoryRepository;
    private final RequestFeignClient requestClient;
    private final UserFeignClient userClient;

    @Override
    public List<EventFullDto> getEvents(AdminEventSearchParams params) {
        log.info("[event admin service] get events");
        var pageable = params.toPageable();

        List<Events> found = eventsRepository.findAdminEvents(params, pageable);
        Map<Long, UserShortDto> userDtos = collectUsers(found);

        List<EventFullDto> dtos = found.stream()
                .map(e -> mapper.toFullDto(e, userDtos.get(e.getInitiatorId())))
                .toList();

        fillConfirmedRequests(dtos);
        return dtos;
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {

        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие с id={} не найдено", eventId);
                    return new NotFoundException("Event with id=" + eventId + " not found");
                });

        mapper.updateEventFromAdminRequest(updateRequest, event);

        if (updateRequest.getEventDate() != null) {
            LocalDateTime newDate = updateRequest.getEventDate();
            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("Нарушено ограничение по дате при обновлении события id={}, newDate={}",
                        eventId, newDate);
                throw new ValidationException(
                        "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
                );
            }
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Category with id=" + updateRequest.getCategory() + " not found"));
            event.setCategory(category);
        }

        if (updateRequest.getLocation() != null && event.getLocation() != null) {
            event.getLocation().setLat(updateRequest.getLocation().lat());
            event.getLocation().setLon(updateRequest.getLocation().lon());
        }

        if (updateRequest.getStateAction() != null) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Можно опубликовать только событие в статусе PENDING");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Нельзя отклонить уже опубликованное событие");
            }
            event.setState(EventState.CANCELED);
        }
        Events saved = eventsRepository.save(event);
        UserShortDto userDto = userClient.getUserById(saved.getInitiatorId());
        EventFullDto result = mapper.toFullDto(saved, userDto);
        fillConfirmedRequest(result);
        return result;
    }

    private void fillConfirmedRequest(EventFullDto dto) {
        if (dto == null || dto.getId() == null) {
            log.warn("fillConfirmedRequests: dto или dto.id == null, пропускаем");
            return;
        }
        Long confirmed = requestClient.countConfirmedRequestsByEventId(dto.getId()).getBody();
        dto.setConfirmedRequests(confirmed);
        log.debug("Для события id={} установлено confirmedRequests={}", dto.getId(), confirmed);
    }

    private void fillConfirmedRequests(List<EventFullDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        List<Long> eventIds = dtos.stream()
                .map(EventFullDto::getId)
                .toList();
        Map<Long, Long> confirmedEventsById = requestClient.countConfirmedRequestsByEventIds(eventIds).getBody();
        if (confirmedEventsById == null) {
            throw new RuntimeException("ошибка при подсчете количества подтвержденных запросов");
        }
        for (EventFullDto dto : dtos) {
            dto.setConfirmedRequests(confirmedEventsById.get(dto.getId()));
        }
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
