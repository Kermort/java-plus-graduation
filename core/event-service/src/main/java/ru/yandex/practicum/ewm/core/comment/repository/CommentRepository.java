package ru.yandex.practicum.ewm.core.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.ewm.core.comment.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>, QCommentRepository {
    void deleteCommentsByEventId(Long eventId);
}
