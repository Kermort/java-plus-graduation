package ru.yandex.practicum.ewm.core.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.ewm.api.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.ewm.core.request.service.ParticipationRequestService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalParticipationRequestController {
    private final ParticipationRequestService requestService;

    @GetMapping
    ResponseEntity<List<ParticipationRequestDto>> findEventRequests(@RequestParam Long userId,
                                                                    @RequestParam Long eventId) {
        log.info("[request controller (internal)] find event requests userId={}, eventId={}", userId, eventId);
        List<ParticipationRequestDto> result = requestService.findEventRequests(userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/confirmed-many")
    ResponseEntity<Map<Long, Long>> countConfirmedRequestsByEventIds(@RequestParam List<Long> eventIds) {
        log.info("[request controller (internal)] count confirmed requests, eventIds={}", eventIds);
        Map<Long, Long> result = requestService.countConfirmedRequestsByEventIds(eventIds);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/confirmed-single")
    ResponseEntity<Long> countConfirmedRequestsByEventId(@RequestParam Long eventId) {
        log.info("[request controller (internal)] count confirmed requests, eventId={}", eventId);
        Long result = requestService.countConfirmedRequestsByEventId(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
