package ru.yandex.practicum.ewm.core.comment.repository;

import org.springframework.data.domain.Page;
import ru.yandex.practicum.ewm.core.comment.controller.params.FindAllCommentsParams;
import ru.yandex.practicum.ewm.core.comment.model.Comment;

public interface QCommentRepository {
    Page<Comment> findAllComments(FindAllCommentsParams params);
}
