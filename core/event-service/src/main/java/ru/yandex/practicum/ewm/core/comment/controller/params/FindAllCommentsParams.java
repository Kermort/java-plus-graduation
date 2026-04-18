package ru.yandex.practicum.ewm.core.comment.controller.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.ewm.api.comment.enums.CommentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class FindAllCommentsParams {
    Long eventId;

    Long userId;

    CommentStatus status;

    LocalDateTime rangeStart;

    LocalDateTime rangeEnd;

    Long from;

    Long size;

    String sort;
}
