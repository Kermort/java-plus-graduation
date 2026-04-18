package ru.yandex.practicum.ewm.core.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.ewm.api.user.dto.NewUserRequest;
import ru.yandex.practicum.ewm.api.user.dto.UserDto;
import ru.yandex.practicum.ewm.core.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserAdminController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid NewUserRequest newUser) {
        return userService.createUser(newUser);
    }

    @GetMapping
    public List<UserDto> find(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(defaultValue = "0") int from,
                              @RequestParam(defaultValue = "10") int size) {
        return userService.findUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
