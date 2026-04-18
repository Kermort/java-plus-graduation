package ru.yandex.practicum.ewm.core.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;
import ru.yandex.practicum.ewm.core.event.model.params.PublicEventSearchParams;
import ru.yandex.practicum.ewm.core.event.service.EventPublicService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventsPublicController {
    private final EventPublicService eventPublicService;

    @GetMapping
    public List<EventShortDto> getEvents(@ModelAttribute @Valid PublicEventSearchParams params,
                                         HttpServletRequest request) {
        log.info("GET /events params={}", params);
        return eventPublicService.getEvents(params, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable Long id,
                                HttpServletRequest request) {
        log.info("GET /events/{}", id);
        return eventPublicService.getById(id, request);
    }
}
