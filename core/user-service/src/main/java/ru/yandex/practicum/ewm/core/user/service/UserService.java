package ru.yandex.practicum.ewm.core.user.service;

import ru.yandex.practicum.ewm.api.user.dto.NewUserRequest;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.user.model.User;
import ru.yandex.practicum.ewm.api.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUser);

    void deleteUser(Long userId);

    List<UserDto> findUsers(List<Long> ids, int from, int size);

    List<UserShortDto> findUsers(List<Long> ids);

    User findUserById(Long userId);
}
