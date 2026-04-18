package ru.yandex.practicum.ewm.core.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.ewm.core.comment.controller.params.FindAllCommentsParams;
import ru.yandex.practicum.ewm.api.comment.enums.CommentStatus;
import ru.yandex.practicum.ewm.api.comment.dto.CommentFullDto;
import ru.yandex.practicum.ewm.core.comment.service.CommentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentAdminController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/comments/{commentId}")
    public void removeComment(@PathVariable Long commentId) {
        log.info("Admin: удаление комментария с ID = {}", commentId);
        commentService.adminRemoveCommentById(commentId);
    }

    @GetMapping("/admin/comments")
    public List<CommentFullDto> findAllComments(@RequestParam(required = false) Long userId,
                                                @RequestParam(required = false) Long eventId,
                                                @RequestParam(required = false) CommentStatus status,
                                                @RequestParam(required = false) String rangeStart,
                                                @RequestParam(required = false) String rangeEnd,
                                                @RequestParam(defaultValue = "0") Long from,
                                                @RequestParam(defaultValue = "10") Long size,
                                                @RequestParam(defaultValue = "desc") String sort) {
        log.info("Admin: запрос на получение комментариев");

        LocalDateTime rangeStartParsed = null;
        LocalDateTime rangeEndParsed = null;

        if (rangeStart != null && !rangeStart.isBlank()) {
            String decodedRangeStart = rangeStart.replace("%20", " ").replace("%3A", ":");
            rangeStartParsed = LocalDateTime.parse(
                    decodedRangeStart,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
        }

        if (rangeEnd != null && !rangeEnd.isBlank()) {
            String decodedRangeEnd = rangeEnd.replace("%20", " ").replace("%3A", ":");
            rangeEndParsed = LocalDateTime.parse(
                    decodedRangeEnd,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
        }

        FindAllCommentsParams params = FindAllCommentsParams.builder()
                .userId(userId)
                .eventId(eventId)
                .status(status)
                .rangeStart(rangeStartParsed)
                .rangeEnd(rangeEndParsed)
                .from(from)
                .size(size)
                .sort(sort)
                .build();

        return commentService.findAllComments(params);
    }

    @PatchMapping("/admin/comments/{commentId}")
    public CommentFullDto moderateComment(@PathVariable Long commentId, @RequestParam(required = true) CommentStatus status) {
        log.info("Admin: модерация комментария с ID = {}, новый статус = {}", commentId, status);
        return commentService.moderateComment(commentId, status);
    }
}
