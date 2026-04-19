package ru.yandex.practicum.ewm.core.event.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.yandex.practicum.ewm.core.location.model.Location;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;
import ru.yandex.practicum.ewm.api.event.dto.NewEventDto;
import ru.yandex.practicum.ewm.api.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.ewm.api.event.enums.EventState;
import ru.yandex.practicum.ewm.api.event.enums.StateActionUserUpdateEvent;
import ru.yandex.practicum.ewm.core.event.repository.EventsRepository;
import ru.yandex.practicum.ewm.core.category.service.CategoryService;
import ru.yandex.practicum.ewm.core.event.service.EventAuthorizedService;
import ru.yandex.practicum.ewm.core.location.service.LocationService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventAuthorizedServiceImpl implements EventAuthorizedService {
    private final EventsMapper mapper;
    private final EventsRepository eventsRepository;
    private final LocationService locationService;
    private final CategoryService categoryService;
    private final RequestFeignClient requestClient;
    private final UserFeignClient userClient;


    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Получение событий пользователя userId={}, from={}, size={}", userId, from, size);
        UserShortDto userDto = userClient.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Events> page = eventsRepository.findAllByInitiatorId(userId, pageable);

        return page.getContent().stream()
                .map(e -> mapper.toShortDto(e, userDto))
                .toList();
    }

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события: userId={}, payload={}", userId, newEventDto);

        UserShortDto userDto = userClient.getUserById(userId);
        if (userDto == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        LocalDateTime newDate = newEventDto.getEventDate();
        if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Нарушено ограничение по дате при добавлении нового события userId={}, newDate={}",
                    userId, newDate);
            throw new ValidationException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
            );
        }

        Category category = categoryService.findCategoryEntityById(newEventDto.getCategory());

        Location location = locationService.saveLocation(newEventDto.getLocation());
        log.debug("Сохранена локация id={}, lat={}, lon={}",
                location.getId(), location.getLat(), location.getLon());

        Events events = mapper.toEntity(newEventDto);

        events.setInitiatorId(userDto.id());
        events.setCategory(category);
        events.setLocation(location);
        events.setState(EventState.PENDING);
        events.setCreatedOn(LocalDateTime.now());
        log.info(
                "Готовим событие к сохранению: userId={}, title='{}', eventDate={}, categoryId={}, locationId={}, paid={}, participantLimit={}",
                userId,
                events.getTitle(),
                events.getEventDate(),
                events.getCategory() != null ? events.getCategory().getId() : null,
                events.getLocation() != null ? events.getLocation().getId() : null,
                events.getPaid(),
                events.getParticipantLimit()
        );
        Events saved = eventsRepository.save(events);
        log.info("Событие создано успешно: eventId={}, userId={}", saved.getId(), userId);
        return mapper.toFullDto(saved, userDto);
    }

    @Transactional
    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Обновление события eventId={} пользователем userId={}, body={}", eventId, userId, updateRequest);

        UserShortDto userDto = userClient.getUserById(userId);
        if (userDto == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        Events event = checkEvent(eventId);
        checkInitiator(userId, eventId, event);

        if (updateRequest.getStateAction() != StateActionUserUpdateEvent.SEND_TO_REVIEW &&
                event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Не удается обновить опубликованное событие,уже PUBLISHED");
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime newDate = updateRequest.getEventDate();
            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("Нарушено ограничение по дате при обновлении события id={}, userId={}, newDate={}",
                        eventId, userId, newDate);
                throw new ValidationException(
                        "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
                );
            }
        }

        mapper.updateEventFromUserRequest(updateRequest, event);

        if (updateRequest.getCategory() != null) {
            Category newCategory = categoryService.findCategoryEntityById(updateRequest.getCategory());
            event.setCategory(newCategory);
        }

        if (updateRequest.getLocation() != null) {
            Location newLocation = locationService.saveLocation(updateRequest.getLocation());
            log.debug("Обновлена локация для события eventId={}: locationId={}, lat={}, lon={}",
                    eventId, newLocation.getId(), newLocation.getLat(), newLocation.getLon());
            event.setLocation(newLocation);
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW -> {
                    event.setState(EventState.PENDING);
                    log.info("Событие id={} отправлено на модерацию пользователем id={}", eventId, userId);
                }
                case CANCEL_REVIEW -> {
                    event.setState(EventState.CANCELED);
                    log.info("Событие id={} отменено пользователем id={}", eventId, userId);
                }
            }
        }
        Events saved = eventsRepository.save(event);
        log.info("Событие id={} успешно обновлено пользователем id={}, новое состояние={}",
                saved.getId(), userId, saved.getState());
        return mapper.toFullDto(saved, userDto);
    }


    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Получение события eventId={} пользователем userId={}", eventId, userId);

        Events event = checkEvent(eventId);
        checkInitiator(userId, eventId, event);
        UserShortDto userDto = userClient.getUserById(userId);
        return mapper.toFullDto(event, userDto);
    }

    private Events checkEvent(Long eventId) {
        return eventsRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие с id={} не найдено", eventId);
                    return new NotFoundException("Event with id=" + eventId + " not found");
                });
    }

    private static void checkInitiator(Long userId, Long eventId, Events event) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("User " + userId + " is not initiator of event " + eventId);
        }
    }
}
