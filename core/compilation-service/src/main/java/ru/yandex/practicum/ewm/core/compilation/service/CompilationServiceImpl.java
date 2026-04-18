package ru.yandex.practicum.ewm.core.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.ewm.api.compilation.dto.CompilationDto;
import ru.yandex.practicum.ewm.api.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.ewm.api.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.ewm.api.event.EventFeignClient;
import ru.yandex.practicum.ewm.api.event.dto.EventInternalDto;
import ru.yandex.practicum.ewm.api.exception.ConflictException;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.api.exception.ValidationException;
import ru.yandex.practicum.ewm.api.user.UserFeignClient;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.ewm.core.compilation.model.Compilation;
import ru.yandex.practicum.ewm.core.compilation.model.CompilationEvent;
import ru.yandex.practicum.ewm.core.compilation.model.params.PublicCompilationSearchParams;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;
import ru.yandex.practicum.ewm.core.compilation.repository.CompilationEventRepository;
import ru.yandex.practicum.ewm.core.compilation.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationEventRepository compilationEventRepository;
    private final UserFeignClient userClient;
    private final EventFeignClient eventClient;

    @Override
    @Transactional
    public CompilationDto save(NewCompilationDto newDto) {
        List<EventInternalDto> events = eventClient.findByIdInInternal(List.copyOf(newDto.getEvents()));
        if (newDto.getEvents().size() != events.size()) {
            throw new NotFoundException("Найдены не все события, добавляемые в подборку");
        }

        List<Long> userIds = events.stream()
                .map(EventInternalDto::initiatorId)
                .toList();

        List<UserShortDto> users = userClient.getUsers(userIds);
        Map<Long, UserShortDto> userDtos = users.stream()
                .collect(Collectors.toMap(UserShortDto::id, u -> u));

        try {
            Compilation savedCompilation = compilationRepository.save(CompilationMapper.toModel(newDto));
            List<CompilationEvent> list = events.stream().map(e -> new CompilationEvent(savedCompilation.getId(), e.id())).toList();
            compilationEventRepository.saveAll(list);

            return CompilationMapper.toDto(savedCompilation,
                    events.stream().map(e -> toEventShortDto(e, userDtos.get(e.initiatorId()))).toList());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public CompilationDto patch(UpdateCompilationRequest updateDto, Long compId) {
        if (updateDto.getTitle() != null && (updateDto.getTitle().length() > 50 || updateDto.getTitle().isEmpty())) {
            throw new ValidationException("заголовок подборки должен быть в диапазоне от 1 до 50 символов");
        }
        Compilation updateCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("не найдена подборка с id " + compId));

        if (updateDto.getPinned() != null) {
            updateCompilation.setPinned(updateDto.getPinned());
        }

        if (updateDto.getTitle() != null) {
            updateCompilation.setTitle(updateDto.getTitle());
        }

        List<EventInternalDto> events;
        if (updateDto.getEvents() != null) {
            events = eventClient.findByIdInInternal(List.copyOf(updateDto.getEvents()));
            if (updateDto.getEvents().size() != events.size()) {
                throw new NotFoundException("Найдены не все события, добавляемые в подборку");
            }

        } else {
            events = eventClient.findByIdInInternal(compilationEventRepository.findEventIdsByCompilationId(compId));
        }

        List<Long> userIds = events.stream()
                .map(EventInternalDto::initiatorId)
                .toList();

        List<UserShortDto> users = userClient.getUsers(userIds);
        Map<Long, UserShortDto> userDtos = users.stream()
                .collect(Collectors.toMap(UserShortDto::id, u -> u));

        try {
            Compilation savedCompilation = compilationRepository.save(updateCompilation);
            List<CompilationEvent> list = events.stream().map(e -> new CompilationEvent(savedCompilation.getId(), e.id())).toList();
            compilationEventRepository.saveAll(list);
            return CompilationMapper.toDto(savedCompilation,
                    events.stream().map(e -> toEventShortDto(e, userDtos.get(e.initiatorId()))).toList());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation result = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("не найдена подборка с id " + compId));
        compilationRepository.delete(result);
    }

    @Override
    public CompilationDto findById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("не найдена подборка с id " + compId));

        List<EventInternalDto> events = eventClient.findByIdInInternal(compilationEventRepository.findEventIdsByCompilationId(compId));
        List<Long> userIds = events.stream()
                .map(EventInternalDto::initiatorId)
                .toList();

        List<UserShortDto> users = userClient.getUsers(userIds);
        Map<Long, UserShortDto> userDtos = users.stream()
                .collect(Collectors.toMap(UserShortDto::id, u -> u));

        return CompilationMapper.toDto(compilation,
                events.stream().map(e -> toEventShortDto(e, userDtos.get(e.initiatorId()))).toList());
    }

    @Override
    public List<CompilationDto> findCompilations(PublicCompilationSearchParams params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Compilation> compilationPage = compilationRepository.findByPinned(params.getPinned(), pageable);

        List<Compilation> compilations = compilationPage.getContent();
        if (compilations.isEmpty()) {
            return List.of();
        }

        List<Long> cIds = compilations.stream()
                .map(Compilation::getId)
                .toList();

        List<CompilationEvent> compilationEvents =
                compilationEventRepository
                        .findByCompilationIds(cIds);

        Map<Long, List<Long>> eventIdsByCompilationId = compilationEvents.stream()
                .collect(Collectors.groupingBy(
                        CompilationEvent::getCompilationId,
                        Collectors.mapping(CompilationEvent::getEventId, Collectors.toList())
                ));


        List<Long> eventIdsList = eventIdsByCompilationId.values().stream()
                .flatMap(List::stream)
                .toList();

        List<EventInternalDto> eventsInAllCompilations = eventClient.findByIdInInternal(eventIdsList);
        Map<Long, EventInternalDto> allEventsById = eventsInAllCompilations.stream()
                .collect(Collectors.toMap(EventInternalDto::id, e -> e));

        Map<Long, List<EventInternalDto>> eventsByCompilationId = new HashMap<>();
        for (CompilationEvent ce: compilationEvents) {
            eventsByCompilationId.putIfAbsent(ce.getCompilationId(), new ArrayList<>());
            eventsByCompilationId.get(ce.getCompilationId()).add(allEventsById.get(ce.getEventId()));
        }

        List<Long> userIds = eventsInAllCompilations.stream()
                .map(EventInternalDto::initiatorId)
                .toList();

        List<UserShortDto> users = userClient.getUsers(userIds);
        Map<Long, UserShortDto> userDtoMap = users.stream()
                .collect(Collectors.toMap(UserShortDto::id, u -> u));

        return compilations.stream()
                .map(c -> {
                    List<EventInternalDto> eventsList = eventsByCompilationId
                            .getOrDefault(c.getId(), List.of());

                    List<EventShortDto> eventShortDtos = eventsList.stream()
                            .map(e -> toEventShortDto(e, userDtoMap.get(e.initiatorId())))
                            .toList();

                    return CompilationMapper.toDto(c, eventShortDtos);
                })
                .toList();
    }

    private EventShortDto toEventShortDto(EventInternalDto internalDto, UserShortDto userDto) {
        return new EventShortDto(
                internalDto.id(),
                internalDto.title(),
                internalDto.annotation(),
                internalDto.category(),
                userDto,
                internalDto.paid(),
                internalDto.eventDate(),
                internalDto.views(),
                internalDto.confirmedRequests()
        );
    }
}
