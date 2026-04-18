package ru.yandex.practicum.ewm.api.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserFeignClient {
    @GetMapping("/{userId}")
    UserShortDto getUserById(@PathVariable Long userId);

    @GetMapping()
    List<UserShortDto> getUsers(@RequestParam List<Long> ids);
}
