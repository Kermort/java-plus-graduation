package ru.yandex.practicum.ewm.core.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.ewm.api.exception.ConflictException;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.user.mapper.UserMapper;
import ru.yandex.practicum.ewm.api.user.dto.NewUserRequest;
import ru.yandex.practicum.ewm.core.user.model.User;
import ru.yandex.practicum.ewm.api.user.dto.UserDto;
import ru.yandex.practicum.ewm.core.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(NewUserRequest newUser) {
        log.info("Создание пользователя: name={}, email={}",
                newUser.getName(), newUser.getEmail());
        if (userRepository.existsByEmail(newUser.getEmail())) {
            log.warn("Попытка создания пользователя с уже занятым email={}",
                    newUser.getEmail());
            throw new ConflictException(
                    String.format("User with email=%s already exists", newUser.getEmail())
            );
        }
        User user = userMapper.toModel(newUser);
        User save = userRepository.save(user);

        return userMapper.toDto(save);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> findUsers(List<Long> ids, int from, int size) {
        Pageable page = PageRequest.of(from / size, size);

        return (ids == null || ids.isEmpty()
                ? userRepository.findAll(page)
                : userRepository.findAllByIdIn(ids, page)
        )
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public List<UserShortDto> findUsers(List<Long> ids) {
        return userRepository.findAllByIdIn(ids).stream()
                .map(userMapper::toShortDto)
                .toList();
    }

    @Override
    public User findUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден при создании события", userId);
                    return new NotFoundException("User with id=" + userId + " not found");
                });

    }
}
