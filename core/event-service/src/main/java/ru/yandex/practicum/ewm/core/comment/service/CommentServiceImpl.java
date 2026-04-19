package ru.yandex.practicum.ewm.core.comment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.ewm.api.user.UserFeignClient;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.comment.controller.params.FindAllCommentsParams;
import ru.yandex.practicum.ewm.api.exception.ConflictException;
import ru.yandex.practicum.ewm.api.exception.NotFoundException;
import ru.yandex.practicum.ewm.core.comment.mapper.CommentFullDtoMapper;
import ru.yandex.practicum.ewm.core.comment.mapper.CommentShortDtoMapper;
import ru.yandex.practicum.ewm.core.comment.mapper.NewCommentRequestMapper;
import ru.yandex.practicum.ewm.core.comment.model.Comment;
import ru.yandex.practicum.ewm.api.comment.enums.CommentStatus;
import ru.yandex.practicum.ewm.api.comment.dto.CommentFullDto;
import ru.yandex.practicum.ewm.api.comment.dto.CommentShortDto;
import ru.yandex.practicum.ewm.api.comment.dto.NewCommentRequest;
import ru.yandex.practicum.ewm.core.event.model.Events;
import ru.yandex.practicum.ewm.api.event.enums.EventState;
import ru.yandex.practicum.ewm.core.comment.repository.CommentRepository;
import ru.yandex.practicum.ewm.core.event.repository.EventsRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserFeignClient userClient;
    private final EventsRepository eventsRepository;

    @Override
    public List<CommentShortDto> findAllCommentsForEvent(FindAllCommentsParams params) {
        if (!eventsRepository.existsById(params.getEventId())) {
            throw new NotFoundException("Событие не найдено");
        }
        return commentRepository.findAllComments(params).getContent()
                .stream()
                .map(CommentShortDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentFullDto findCommentById(Long eventId, Long commentId) {
        if (!eventsRepository.existsById(eventId)) {
            throw new NotFoundException("Событие не найдено");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!comment.getStatus().equals(CommentStatus.PUBLISHED)) {
            throw new NotFoundException("Комментарий еще не опубликован");
        }
        if (!comment.getEvent().getId().equals(eventId)) {
            throw new ConflictException("Комментарий не относится к указанному событию");
        }
        return CommentFullDtoMapper.toDto(comment);
    }

    @Override
    public CommentFullDto addComment(Long eventId, Long authorId, NewCommentRequest request) {
        UserShortDto authorDto = userClient.getUserById(authorId);
        if (authorDto == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие еще не опубликовано");
        }
        Comment newComment = NewCommentRequestMapper.toEntity(request);
        newComment.setAuthorId(authorDto.id());
        newComment.setEvent(event);
        newComment.setStatus(CommentStatus.PENDING);
        return CommentFullDtoMapper.toDto(commentRepository.save(newComment));
    }

    @Override
    public List<CommentFullDto> findCommentsByAuthorId(FindAllCommentsParams params) {
        return commentRepository.findAllComments(params).getContent()
                .stream()
                .map(CommentFullDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeCommentById(Long userId, Long commentId) {
        UserShortDto userDto = userClient.getUserById(userId);
        if (userDto == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!userId.equals(comment.getAuthorId())) {
            throw new ConflictException("Пользователь не является автором комментария");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentFullDto> findAllComments(FindAllCommentsParams params) {
        return commentRepository.findAllComments(params).getContent()
                .stream()
                .map(CommentFullDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentFullDto moderateComment(Long commentId, CommentStatus status) {
        Comment updatingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        updatingComment.setStatus(status);
        return CommentFullDtoMapper.toDto(commentRepository.save(updatingComment));
    }

    @Override
    public void adminRemoveCommentById(Long commentId) {
        if (commentRepository.existsById(commentId)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }
}
