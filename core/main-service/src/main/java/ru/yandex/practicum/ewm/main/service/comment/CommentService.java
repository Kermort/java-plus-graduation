package ru.yandex.practicum.ewm.main.service.comment;

import ru.yandex.practicum.ewm.main.controller.comment.params.FindAllCommentsParams;
import ru.yandex.practicum.ewm.main.model.comment.CommentStatus;
import ru.yandex.practicum.ewm.main.model.comment.dto.CommentFullDto;
import ru.yandex.practicum.ewm.main.model.comment.dto.CommentShortDto;
import ru.yandex.practicum.ewm.main.model.comment.dto.NewCommentRequest;

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
