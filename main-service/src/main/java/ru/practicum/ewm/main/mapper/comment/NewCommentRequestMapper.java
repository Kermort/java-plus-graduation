package ru.practicum.ewm.main.mapper.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.model.comment.Comment;
import ru.practicum.ewm.main.model.comment.dto.NewCommentRequest;

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
