package ru.yandex.practicum.ewm.api.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.ewm.api.event.dto.EventFullDto;
import ru.yandex.practicum.ewm.api.event.dto.EventInternalDto;

import java.util.List;

@FeignClient(name = "event-service", path = "/internal/events")
public interface EventFeignClient {
    @GetMapping("/{eventId}")
    EventFullDto findByIdInternal(@PathVariable Long eventId);

    @GetMapping()
    List<EventInternalDto> findByIdInInternal(@RequestParam List<Long> ids);
}
