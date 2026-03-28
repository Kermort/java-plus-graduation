package ru.yandex.practicum.ewm.main.service.user;

import ru.yandex.practicum.ewm.main.model.user.NewUserRequest;
import ru.yandex.practicum.ewm.main.model.user.User;
import ru.yandex.practicum.ewm.main.model.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUser);

    void deleteUser(Long userId);

    List<UserDto> findUsers(List<Long> ids, int from, int size);

    User findUserById(Long userId);
}
