package ru.yandex.practicum.ewm.core.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.grpc.ActionTypeProto;
import ru.practicum.ewm.stats.grpc.UserActionProto;
import ru.yandex.practicum.CollectorClient;
import ru.yandex.practicum.ewm.api.event.EventFeignClient;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.user.UserFeignClient;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.api.request.enums.ParticipationRequestStatus;
import ru.yandex.practicum.ewm.api.exception.ConflictException;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.core.request.mapper.ParticipationRequestMapper;
import ru.yandex.practicum.ewm.core.request.model.ParticipationRequest;
import ru.yandex.practicum.ewm.api.request.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.ewm.api.request.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.ewm.api.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.ewm.core.request.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestValidator participationRequestValidator;
    private final UserFeignClient userClient;
    private final EventFeignClient eventClient;
    private final CollectorClient collectorClient;

    @Override
    public ParticipationRequestDto add(Long requesterId, Long eventId) {
        UserShortDto requester = userClient.getUserById(requesterId);
        EventFullDto eventDto = eventClient.findByIdInternal(eventId);
        if (eventDto == null) {
            throw new NotFoundException("не найдено событие с id " + eventId);
        }

        if (requester == null) {
            throw new NotFoundException("не найден пользователь с id " + requesterId);
        }

        Optional<ParticipationRequest> existing = requestRepository.findByRequesterIdAndEventId(requesterId, eventId);
        if (existing.isPresent()) {
            log.warn("Попытка добавить повторный запрос: requesterId={}, eventId={}", requesterId, eventId);
            throw new ConflictException("нельзя добавить повторный запрос");
        }

        if (requesterId.equals(eventDto.getInitiator().id())) {
            log.warn("Инициатор пытается подать запрос на своё событие: userId={}, eventId={}",
                    requesterId, eventId);
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии");
        }

        if (eventDto.getPublishedOn() == null) {
            log.warn("Попытка подать заявку на неопубликованное событие: eventId={}, requesterId={}",
                    eventId, requesterId);
            throw new ConflictException("нельзя участвовать в неопубликованном событии");
        }

        long limit = eventDto.getParticipantLimit() == null ? 0L : eventDto.getParticipantLimit();
        if (limit != 0L) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(
                    eventId,
                    ParticipationRequestStatus.CONFIRMED
            );
            if (confirmedCount >= limit) {
                log.warn("Достигнут лимит участников: eventId={}, limit={}, confirmed={}",
                        eventId, limit, confirmedCount);
                throw new ConflictException("достигнут лимит запросов на участие");
            }
        }

        ParticipationRequest newRequest = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .eventId(eventDto.getId())
                .requesterId(requester.id())
                .status(ParticipationRequestStatus.PENDING)
                .build();

        if (Boolean.FALSE.equals(eventDto.getRequestModeration()) || limit == 0) {
            newRequest.setStatus(ParticipationRequestStatus.CONFIRMED);
            log.debug("Премодерация отключена либо нет ограничений на количество участников, заявка будет сразу CONFIRMED: eventId={}, requesterId={}",
                    eventId, requesterId);
        }

        ParticipationRequest saved = requestRepository.save(newRequest);
        log.info("Запрос на участие создан: requestId={}, requesterId={}, eventId={}",
                saved.getId(), requesterId, eventId);
        collectorClient.collectUserAction(UserActionProto.newBuilder()
                .setUserId(requesterId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .build());
        return ParticipationRequestMapper.toDto(saved);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long requesterId, Long requestId) {
        UserShortDto requester = userClient.getUserById(requesterId);

        if (requester == null) {
            throw new NotFoundException("не найден пользователь с id " + requesterId);
        }

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("не найден запрос на событие с id " + requestId));
        request.setStatus(ParticipationRequestStatus.CANCELED);
        ParticipationRequest result = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(result);
    }

    @Override
    public List<ParticipationRequestDto> findByRequesterId(Long requesterId) {
        UserShortDto requester = userClient.getUserById(requesterId);

        if (requester == null) {
            throw new NotFoundException("не найден пользователь с id " + requesterId);
        }
        List<ParticipationRequest> result = requestRepository.findByRequesterId(requesterId);

        return result.stream().map(ParticipationRequestMapper::toDto).toList();
    }

    @Override
    public List<ParticipationRequestDto> findEventRequests(Long userId, Long eventId) {
        List<ParticipationRequest> result = requestRepository.findByEventId(eventId);
        participationRequestValidator.checkEventForInitiator(userId, eventId);
        return result.stream().map(ParticipationRequestMapper::toDto).toList();
    }


    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Изменение статуса заявок: userId={}, eventId={}, body={}",
                userId, eventId, updateRequest);

        UserShortDto user = userClient.getUserById(userId);

        if (user == null) {
            throw new NotFoundException("не найден пользователь с id " + userId);
        }

        EventFullDto eventDto = eventClient.findByIdInternal(eventId);
        if (eventDto == null) {
            throw new NotFoundException("не найдено событие с id " + eventId);
        }

        participationRequestValidator.checkEventForInitiator(userId, eventId);
        List<Long> requestIds = updateRequest.getRequestIds();
        if (requestIds == null || requestIds.isEmpty()) {
            log.warn("Пустой список requestIds при изменении статуса заявок для eventId={}", eventId);
            throw new ConflictException("RequestIds must not be empty");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);
        if (requests.size() != requestIds.size()) {
            log.warn("Не все заявки найдены: eventId={}, ожидалось {}, найдено {}",
                    eventId, requestIds.size(), requests.size());
            throw new NotFoundException("Some participation requests were not found");
        }

        for (ParticipationRequest r : requests) {
            if (!r.getEventId().equals(eventId)) {
                log.warn("Заявка id={} не принадлежит событию id={}", r.getId(), eventId);
                throw new ConflictException("Request does not belong to this event");
            }
        }

        for (ParticipationRequest r : requests) {
            if (r.getStatus() != ParticipationRequestStatus.PENDING) {
                log.warn("Попытка изменить статус заявки id={} со статусом {} (ожидался PENDING)",
                        r.getId(), r.getStatus());
                throw new ConflictException("Request must have status PENDING");
            }
        }

        ParticipationRequestStatus action = updateRequest.getStatus();
        if (action == null) {
            log.warn("Статус в EventRequestStatusUpdateRequest == null");
            throw new ConflictException("Status must not be null");
        }

        List<ParticipationRequestDto> confirmedDtos = new java.util.ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new java.util.ArrayList<>();


        if (action == ParticipationRequestStatus.REJECTED) {
            for (ParticipationRequest r : requests) {
                r.setStatus(ParticipationRequestStatus.REJECTED);
                rejectedDtos.add(ParticipationRequestMapper.toDto(r));
            }
            requestRepository.saveAll(requests);

            EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
            result.setConfirmedRequests(confirmedDtos);
            result.setRejectedRequests(rejectedDtos);

            log.info("Отклонены заявки {} для eventId={} пользователем id={}",
                    requests.stream().map(ParticipationRequest::getId).toList(),
                    eventId, userId);

            return result;
        }

        if (action == ParticipationRequestStatus.CONFIRMED) {
            long limit = eventDto.getParticipantLimit() != null ? eventDto.getParticipantLimit() : 0L;

            long confirmedCount = requestRepository.countByEventIdAndStatus(
                    eventId,
                    ParticipationRequestStatus.CONFIRMED
            );

            if (limit != 0 && confirmedCount >= limit) {
                log.warn("Лимит участников уже достигнут для eventId={}", eventId);
                throw new ConflictException("The participant limit has been reached");
            }

            for (ParticipationRequest r : requests) {
                if (limit == 0) {
                    r.setStatus(ParticipationRequestStatus.CONFIRMED);
                    confirmedDtos.add(ParticipationRequestMapper.toDto(r));
                } else {
                    // есть лимит
                    if (confirmedCount < limit) {
                        r.setStatus(ParticipationRequestStatus.CONFIRMED);
                        confirmedCount++;
                        confirmedDtos.add(ParticipationRequestMapper.toDto(r));
                    } else {
                        r.setStatus(ParticipationRequestStatus.REJECTED);
                        rejectedDtos.add(ParticipationRequestMapper.toDto(r));
                    }
                }
            }

            requestRepository.saveAll(requests);

            if (limit != 0 && confirmedCount >= limit) {
                List<ParticipationRequest> pending = requestRepository
                        .findAllByEventIdAndStatus(eventId, ParticipationRequestStatus.PENDING);

                for (ParticipationRequest p : pending) {
                    p.setStatus(ParticipationRequestStatus.REJECTED);
                    rejectedDtos.add(ParticipationRequestMapper.toDto(p));
                }

                if (!pending.isEmpty()) {
                    requestRepository.saveAll(pending);
                }

                log.info("После подтверждения достигнут лимит для eventId={}. " +
                                "Все оставшиеся PENDING-заявки отклонены, ids={}",
                        eventId,
                        pending.stream().map(ParticipationRequest::getId).toList());
            }

            EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
            result.setConfirmedRequests(confirmedDtos);
            result.setRejectedRequests(rejectedDtos);

            log.info("Изменены статусы заявок для eventId={}: confirmed={}, rejected={}",
                    eventId,
                    confirmedDtos.stream().map(ParticipationRequestDto::getId).toList(),
                    rejectedDtos.stream().map(ParticipationRequestDto::getId).toList());

            return result;
        }

        log.warn("Некорректный статус={} в EventRequestStatusUpdateRequest", action);
        throw new ConflictException("Unknown status: " + action);
    }

    public Long countConfirmedRequestsByEventId(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
    }

    public Map<Long, Long> countConfirmedRequestsByEventIds(List<Long> eventIds) {
        List<Object[]> result = requestRepository.countByEventIdsAndStatus(eventIds, ParticipationRequestStatus.CONFIRMED);
        Map<Long, Long> map =  result.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return eventIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> map.getOrDefault(id, 0L)
                ));
    }

    @Override
    public boolean checkParticipation(long userId, long eventId) {
        ParticipationRequest request = requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("request not found"));

        return request.getStatus().equals(ParticipationRequestStatus.CONFIRMED);
    }
}
