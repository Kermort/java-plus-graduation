package ru.yandex.practicum.ewm.core.comment.service;

import ru.yandex.practicum.ewm.core.comment.controller.params.FindAllCommentsParams;
import ru.yandex.practicum.ewm.api.comment.enums.CommentStatus;
import ru.yandex.practicum.ewm.api.comment.dto.CommentFullDto;
import ru.yandex.practicum.ewm.api.comment.dto.CommentShortDto;
import ru.yandex.practicum.ewm.api.comment.dto.NewCommentRequest;

import java.util.List;

public interface CommentService {
    List<CommentShortDto> findAllCommentsForEvent(FindAllCommentsParams params);

    CommentFullDto findCommentById(Long eventId, Long commentId);

    CommentFullDto addComment(Long eventId, Long authorId, NewCommentRequest comment);

    List<CommentFullDto> findCommentsByAuthorId(FindAllCommentsParams params);

    void removeCommentById(Long userId, Long commentId);

    List<CommentFullDto> findAllComments(FindAllCommentsParams params);

    CommentFullDto moderateComment(Long commentId, CommentStatus status);

    void adminRemoveCommentById(Long commentId);
}
