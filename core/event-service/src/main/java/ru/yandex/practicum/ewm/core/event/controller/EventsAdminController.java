package ru.yandex.practicum.ewm.core.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.ewm.core.event.model.params.AdminEventSearchParams;
import ru.yandex.practicum.ewm.core.event.service.EventsAdminService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventsAdminController {
    private final EventsAdminService eventsAdminService;

    @GetMapping
    public List<EventFullDto> getEvents(@Valid @ModelAttribute AdminEventSearchParams params) {
        log.info("GET /admin/events params={}", params);
        return eventsAdminService.getEvents(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateRequest) {
        log.info("PATCH /admin/events/{} body={}", eventId, updateRequest);
        return eventsAdminService.updateEvent(eventId, updateRequest);
    }
}
