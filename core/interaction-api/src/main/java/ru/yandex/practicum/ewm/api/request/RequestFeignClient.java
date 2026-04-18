package ru.yandex.practicum.ewm.api.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestFeignClient {

    @GetMapping("/confirmed-many")
    ResponseEntity<Map<Long, Long>> countConfirmedRequestsByEventIds(@RequestParam List<Long> eventIds);

    @GetMapping("/confirmed-single")
    ResponseEntity<Long> countConfirmedRequestsByEventId(@RequestParam Long eventId);
}
