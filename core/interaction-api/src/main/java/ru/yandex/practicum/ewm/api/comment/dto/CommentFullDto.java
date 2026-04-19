package ru.yandex.practicum.ewm.api.comment.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.ewm.api.comment.enums.CommentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentFullDto {
    Long id;
    Long eventId;
    Long authorId;
    String text;
    LocalDateTime createdOn;
    CommentStatus status;
}
