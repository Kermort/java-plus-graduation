package ru.yandex.practicum.ewm.core.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.core.comment.model.Comment;
import ru.yandex.practicum.ewm.api.comment.dto.CommentFullDto;

@UtilityClass
public class CommentFullDtoMapper {
    public static CommentFullDto toDto(Comment comment) {
        return CommentFullDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthorId())
                .text(comment.getText())
                .createdOn(comment.getCreatedOn())
                .status(comment.getStatus())
                .build();
    }
}
