package ru.yandex.practicum.ewm.core.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.ewm.api.request.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.ewm.api.request.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.ewm.api.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.ewm.core.request.service.ParticipationRequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class ParticipationRequestController {
    private final ParticipationRequestService requestService;

    @PostMapping("/requests")
    public ResponseEntity<ParticipationRequestDto> add(@PathVariable Long userId,
                                                       @NotNull @RequestParam Long eventId) {
        log.info("[request-service controller] add userId={}, eventId={}", userId, eventId);
        ParticipationRequestDto result = requestService.add(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestId) {
        log.info("[request-service controller] cancel request userId={}, requestId={}", userId, requestId);
        ParticipationRequestDto result = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ParticipationRequestDto>> findByRequesterId(@PathVariable Long userId) {
        log.info("[request-service controller] find by requesterId={}", userId);
        List<ParticipationRequestDto> result = requestService.findByRequesterId(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        log.info("[request-service controller] GET /users/{}/events/{}/requests", userId, eventId);
        return requestService.findEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult rejectingRequest(@PathVariable Long userId,
                                                           @PathVariable Long eventId,
                                                           @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        log.info("[request-service controller] PATCH /users/{}/events/{}/requests/{}", userId, eventId, updateRequest);
        return requestService.changeRequestStatus(userId, eventId, updateRequest);
    }
}
