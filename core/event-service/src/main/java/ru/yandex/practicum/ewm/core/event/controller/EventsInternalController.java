package ru.yandex.practicum.ewm.core.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventInternalDto;
import ru.yandex.practicum.ewm.core.event.service.EventInternalService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventsInternalController {
    private final EventInternalService eventService;

    @GetMapping("/{eventId}")
    public EventFullDto findByIdInternal(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("[event controller (internal)] find by id {} ", eventId);
        return eventService.getByIdInternal(eventId, request);
    }

    @GetMapping
    public List<EventInternalDto> findByIdInInternal(@RequestParam List<Long> ids) {
        log.info("[event controller (internal)] find by id in {} ", ids);
        return eventService.getByIdInInternal(ids);
    }
}
