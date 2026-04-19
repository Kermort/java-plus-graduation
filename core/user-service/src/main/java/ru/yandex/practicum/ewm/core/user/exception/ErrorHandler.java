package ru.yandex.practicum.ewm.core.user.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.ewm.api.exception.ApiError;
import ru.yandex.practicum.ewm.api.exception.ConflictException;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflictHandler(ConflictException e, HttpServletRequest request) {
        log.warn("409 CONFLICT: {} {} -> {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage());
        return new ApiError(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundHandler(NotFoundException e, HttpServletRequest request) {
        log.warn("404 NOT_FOUND: {} {} -> {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage());
        return new ApiError(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage(), LocalDateTime.now());
    }
}
