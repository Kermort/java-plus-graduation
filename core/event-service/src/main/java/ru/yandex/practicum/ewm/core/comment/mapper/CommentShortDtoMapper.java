package ru.yandex.practicum.ewm.core.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.core.comment.model.Comment;
import ru.yandex.practicum.ewm.api.comment.dto.CommentShortDto;

@UtilityClass
public class CommentShortDtoMapper {
    public static CommentShortDto toDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthorId())
                .text(comment.getText())
                .createdOn(comment.getCreatedOn())
                .build();
    }
}
