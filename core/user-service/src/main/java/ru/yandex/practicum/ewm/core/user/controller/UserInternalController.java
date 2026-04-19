package ru.yandex.practicum.ewm.core.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.user.mapper.UserMapper;
import ru.yandex.practicum.ewm.core.user.model.User;
import ru.yandex.practicum.ewm.core.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    UserShortDto getUserById(@PathVariable Long userId) {
        log.info("[user-service] internal get user by id {}", userId);
        User result = userService.findUserById(userId);
        return userMapper.toShortDto(result);
    }

    @GetMapping
    List<UserShortDto> getUsers(@RequestParam List<Long> ids) {
        log.info("[user-service] internal get users by ids {}", ids);
        return userService.findUsers(ids);
    }
}
