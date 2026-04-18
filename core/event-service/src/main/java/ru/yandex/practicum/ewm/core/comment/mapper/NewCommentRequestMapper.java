package ru.yandex.practicum.ewm.core.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.core.comment.model.Comment;
import ru.yandex.practicum.ewm.api.comment.dto.NewCommentRequest;

import java.time.LocalDateTime;

@UtilityClass
public class NewCommentRequestMapper {
    public static Comment toEntity(NewCommentRequest comment) {
        return Comment.builder()
                .text(comment.getText())
                .createdOn(LocalDateTime.now())
                .build();
    }
}
