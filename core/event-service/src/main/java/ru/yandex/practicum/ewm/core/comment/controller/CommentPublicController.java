package ru.yandex.practicum.ewm.core.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.ewm.core.comment.controller.params.FindAllCommentsParams;
import ru.yandex.practicum.ewm.api.comment.enums.CommentStatus;
import ru.yandex.practicum.ewm.api.comment.dto.CommentFullDto;
import ru.yandex.practicum.ewm.api.comment.dto.CommentShortDto;
import ru.yandex.practicum.ewm.core.comment.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentPublicController {
    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments/{commentId}")
    public CommentFullDto findCommentById(@PathVariable Long eventId, @PathVariable Long commentId) {
        return commentService.findCommentById(eventId, commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentShortDto> findAllComments(@PathVariable Long eventId,
                                                 @RequestParam(defaultValue = "10") Long size,
                                                 @RequestParam(defaultValue = "0") Long from,
                                                 @RequestParam(defaultValue = "desc") String sort) {
        FindAllCommentsParams params = FindAllCommentsParams.builder()
                .eventId(eventId)
                .status(CommentStatus.PUBLISHED)
                .size(size)
                .from(from)
                .sort(sort)
                .build();
        return commentService.findAllCommentsForEvent(params);
    }
}
